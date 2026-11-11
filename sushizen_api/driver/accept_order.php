
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

if (!$assignment_id || !$driver_id) {
    respond(400, [
        "success" => false,
        "message" => "assignment_id hoặc driver_id không hợp lệ"
    ]);
}

try {
    $conn->begin_transaction();

    $stmt = $conn->prepare("
        SELECT
            oa.order_id,
            oa.assignment_status,
            o.order_status,
            d.availability_status,
            d.is_online
        FROM order_assignments oa
        INNER JOIN orders o
            ON oa.order_id = o.order_id
        INNER JOIN drivers d
            ON oa.driver_id = d.driver_id
        WHERE oa.assignment_id = ?
          AND oa.driver_id = ?
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
            "message" => "Không tìm thấy phân công của tài xế"
        ]);
    }

    if ($assignment["assignment_status"] !== "OFFERED") {
        $conn->rollback();
        respond(409, [
            "success" => false,
            "message" => "Đơn hàng không còn ở trạng thái chờ nhận"
        ]);
    }

    if (
        (int) $assignment["is_online"] !== 1 ||
        $assignment["availability_status"] !== "AVAILABLE"
    ) {
        $conn->rollback();
        respond(409, [
            "success" => false,
            "message" => "Tài xế không ở trạng thái sẵn sàng"
        ]);
    }

    $stmt = $conn->prepare("
        UPDATE order_assignments
        SET assignment_status = 'ACCEPTED',
            accepted_at = NOW()
        WHERE assignment_id = ?
          AND driver_id = ?
          AND assignment_status = 'OFFERED'
    ");

    $stmt->bind_param("ii", $assignment_id, $driver_id);
    $stmt->execute();

    if ($stmt->affected_rows !== 1) {
        throw new Exception("Không thể cập nhật phân công");
    }

    $stmt->close();

    $stmt = $conn->prepare("
        UPDATE drivers
        SET availability_status = 'BUSY'
        WHERE driver_id = ?
    ");

    $stmt->bind_param("i", $driver_id);
    $stmt->execute();
    $stmt->close();

    $conn->commit();

    respond(200, [
        "success" => true,
        "message" => "Tài xế đã chấp nhận đơn hàng",
        "assignment_id" => $assignment_id,
        "order_id" => (int) $assignment["order_id"],
        "assignment_status" => "ACCEPTED"
    ]);

} catch (Throwable $e) {
    $conn->rollback();
    error_log($e->getMessage());

    respond(500, [
        "success" => false,
        "message" => "Lỗi server"
    ]);
}
