
<?php

header("Content-Type: application/json; charset=UTF-8");

require_once __DIR__ . "/../config/database.php";

$driver_id = filter_input(
    INPUT_GET,
    "driver_id",
    FILTER_VALIDATE_INT
);

if (!$driver_id || $driver_id <= 0) {

    http_response_code(400);

    echo json_encode([
        "success" => false,
        "message" => "driver_id không hợp lệ"
    ], JSON_UNESCAPED_UNICODE);

    exit;
}

try {

    $sql = "
        SELECT
            oa.assignment_id,
            oa.order_id,
            oa.assignment_status,
            oa.assigned_at,

            o.receiver_name,
            o.receiver_phone,
            o.delivery_address,
            o.total_amount,
            o.payment_method,
            o.order_status,

            b.branch_name,
            b.address AS branch_address

        FROM order_assignments oa

        INNER JOIN orders o
            ON oa.order_id = o.order_id

        LEFT JOIN branches b
            ON o.branch_id = b.branch_id

        WHERE oa.driver_id = ?

        ORDER BY oa.assigned_at DESC
    ";

    $stmt = $conn->prepare($sql);

    $stmt->bind_param("i", $driver_id);

    $stmt->execute();

    $result = $stmt->get_result();

    $orders = [];

    while ($row = $result->fetch_assoc()) {
        $orders[] = $row;
    }

    echo json_encode([
        "success" => true,
        "message" => "Lấy danh sách đơn hàng thành công",
        "orders" => $orders
    ], JSON_UNESCAPED_UNICODE);

    $stmt->close();

} catch (Throwable $e) {

    http_response_code(500);

    error_log($e->getMessage());

    echo json_encode([
        "success" => false,
        "message" => "Lỗi server"
    ], JSON_UNESCAPED_UNICODE);
}

$conn->close();

?>
