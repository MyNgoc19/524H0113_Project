
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

    if ($assignment["assignment_status"] !== "ACCEPTED") {
        $conn->rollback();
        respond(409, [
            "success" => false,
            "message" => "Tài xế phải nhận đơn trước khi lấy món"
        ]);
    }

    $stmt = $conn->prepare("
        UPDATE order_assignments
        SET assignment_status = 'PICKED_UP',
            picked_up_at = NOW()
        WHERE assignment_id = ?
          AND driver_id = ?
          AND assignment_status = 'ACCEPTED'
    ");

    $stmt->bind_param("ii", $assignment_id, $driver_id);
    $stmt->execute();

    if ($stmt->affected_rows !== 1) {
        throw new Exception("Không thể cập nhật trạng thái lấy món");
    }

    $stmt->close();

    $conn->commit();

    respond(200, [
        "success" => true,
        "message" => "Tài xế đã lấy món và bắt đầu giao hàng",
        "assignment_id" => $assignment_id,
        "order_id" => (int) $assignment["order_id"],
        "assignment_status" => "PICKED_UP"
    ]);

} catch (Throwable $e) {
    $conn->rollback();
    error_log($e->getMessage());

    respond(500, [
        "success" => false,
        "message" => "Lỗi server"
    ]);
}
