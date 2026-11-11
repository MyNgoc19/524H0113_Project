
<?php

header("Content-Type: application/json; charset=UTF-8");

require_once __DIR__ . "/../config/database.php";

function respond(int $status, array $data): void
{
    http_response_code($status);
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit;
}

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    respond(405, [
        "success" => false,
        "message" => "Chỉ hỗ trợ POST"
    ]);
}

$assignment_id = filter_var(
    $_POST["assignment_id"] ?? null,
    FILTER_VALIDATE_INT
);

$driver_id = filter_var(
    $_POST["driver_id"] ?? null,
    FILTER_VALIDATE_INT
);

$cod_collected = $_POST["cod_collected"] ?? null;

if (!$assignment_id || !$driver_id) {
    respond(400, [
        "success" => false,
        "message" => "assignment_id hoặc driver_id không hợp lệ"
    ]);
}

try {
    $conn->begin_transaction();

    $stmt = $conn->prepare("
        SELECT assignment_status, order_id
        FROM order_assignments
        WHERE assignment_id = ?
          AND driver_id = ?
        FOR UPDATE
    ");

    $stmt->bind_param("ii", $assignment_id, $driver_id);
    $stmt->execute();

    $assignment = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    if (!$assignment) {
        $conn->rollback();
        respond(404, [
            "success" => false,
            "message" => "Không tìm thấy phân công"
        ]);
    }

    if ($assignment["assignment_status"] !== "PICKED_UP") {
        $conn->rollback();
        respond(409, [
            "success" => false,
            "message" => "Đơn hàng chưa được lấy để giao"
        ]);
    }

    $order_id = (int) $assignment["order_id"];

    $stmt = $conn->prepare("
        SELECT payment_method, payment_status
        FROM orders
        WHERE order_id = ?
        FOR UPDATE
    ");

    $stmt->bind_param("i", $order_id);
    $stmt->execute();

    $order = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    if (!$order) {
        $conn->rollback();
        respond(404, [
            "success" => false,
            "message" => "Không tìm thấy đơn hàng"
        ]);
    }

    if (
        $order["payment_method"] === "COD"
        && $order["payment_status"] !== "PAID"
        && $cod_collected !== "1"
    ) {
        $conn->rollback();
        respond(400, [
            "success" => false,
            "message" => "Tài xế phải xác nhận đã thu tiền COD"
        ]);
    }

    $stmt = $conn->prepare("
        UPDATE order_assignments
        SET assignment_status = 'DELIVERED',
            delivered_at = NOW()
        WHERE assignment_id = ?
          AND driver_id = ?
          AND assignment_status = 'PICKED_UP'
    ");

    $stmt->bind_param("ii", $assignment_id, $driver_id);
    $stmt->execute();

    if ($stmt->affected_rows !== 1) {
        throw new Exception("Không thể hoàn thành phân công");
    }

    $stmt->close();

    $stmt = $conn->prepare("
        UPDATE orders
        SET order_status = 'DELIVERED',
            payment_status = CASE
                WHEN payment_method = 'COD'
                     AND payment_status = 'UNPAID'
                     AND ? = '1'
                THEN 'PAID'
                ELSE payment_status
            END
        WHERE order_id = ?
    ");

    $stmt->bind_param("si", $cod_collected, $order_id);
    $stmt->execute();
    $stmt->close();

    $stmt = $conn->prepare("
        UPDATE drivers
        SET availability_status =
            CASE
                WHEN is_online = 1 THEN 'AVAILABLE'
                ELSE 'INACTIVE'
            END
        WHERE driver_id = ?
    ");

    $stmt->bind_param("i", $driver_id);
    $stmt->execute();
    $stmt->close();

    $conn->commit();

    respond(200, [
        "success" => true,
        "message" => "Giao hàng thành công",
        "assignment_id" => $assignment_id,
        "order_id" => $order_id,
        "assignment_status" => "DELIVERED",
        "order_status" => "DELIVERED",
        "payment_status" => (
            $order["payment_method"] === "COD"
            && $cod_collected === "1"
        ) ? "PAID" : $order["payment_status"]
    ]);

} catch (Throwable $e) {
    $conn->rollback();
    error_log($e->getMessage());

    respond(500, [
        "success" => false,
        "message" => "Lỗi server"
    ]);
}
