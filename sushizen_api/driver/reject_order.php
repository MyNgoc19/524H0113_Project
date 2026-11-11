
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

$reason = trim($_POST["reason"] ?? "");

if (!$assignment_id || !$driver_id) {
    respond(400, [
        "success" => false,
        "message" => "assignment_id hoặc driver_id không hợp lệ"
    ]);
}

if (mb_strlen($reason, "UTF-8") > 255) {
    respond(400, [
        "success" => false,
        "message" => "Lý do không được quá 255 ký tự"
    ]);
}

try {
    $conn->begin_transaction();

    // Kiểm tra đơn hàng có được phân công cho tài xế không
    $stmt = $conn->prepare("
        SELECT assignment_status, order_id
        FROM order_assignments
        WHERE assignment_id = ?
          AND driver_id = ?
        FOR UPDATE
    ");

    $stmt->bind_param(
        "ii",
        $assignment_id,
        $driver_id
    );

    $stmt->execute();

    $assignment = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    if (!$assignment) {
        $conn->rollback();

        respond(404, [
            "success" => false,
            "message" => "Không tìm thấy đơn hàng được phân công"
        ]);
    }

    // Chỉ được từ chối đơn chưa nhận
    if ($assignment["assignment_status"] !== "OFFERED") {
        $conn->rollback();

        respond(409, [
            "success" => false,
            "message" => "Chỉ có thể từ chối đơn hàng đang chờ nhận"
        ]);
    }

    // Cập nhật trạng thái phân công thành REJECTED
    $stmt = $conn->prepare("
        UPDATE order_assignments
        SET assignment_status = 'REJECTED',
            cancel_reason = ?,
            cancelled_at = NOW(),
            active_order_id = NULL
        WHERE assignment_id = ?
          AND driver_id = ?
          AND assignment_status = 'OFFERED'
    ");

    $stmt->bind_param(
        "sii",
        $reason,
        $assignment_id,
        $driver_id
    );

    $stmt->execute();

    if ($stmt->affected_rows !== 1) {
        throw new Exception("Không thể từ chối đơn hàng");
    }

    $stmt->close();

    $conn->commit();

    respond(200, [
        "success" => true,
        "message" => "Đã từ chối đơn hàng thành công",
        "assignment_id" => $assignment_id,
        "order_id" => (int) $assignment["order_id"],
        "assignment_status" => "REJECTED"
    ]);

} catch (Throwable $e) {

    try {
        $conn->rollback();
    } catch (Throwable $rollbackError) {
        error_log($rollbackError->getMessage());
    }

    error_log($e->getMessage());

    respond(500, [
        "success" => false,
        "message" => "Lỗi server khi từ chối đơn hàng"
    ]);
}
