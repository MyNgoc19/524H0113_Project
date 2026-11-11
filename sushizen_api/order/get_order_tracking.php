
<?php

header("Content-Type: application/json; charset=UTF-8");

require_once "../config/database.php";

function responseJson($status, $data) {
    http_response_code($status);
    echo json_encode($data, JSON_UNESCAPED_UNICODE);
    exit;
}

// Chỉ chấp nhận GET
if ($_SERVER["REQUEST_METHOD"] !== "GET") {
    responseJson(405, [
        "success" => false,
        "message" => "Chỉ hỗ trợ GET"
    ]);
}

// Lấy mã đơn hàng
$order_id = intval($_GET["order_id"] ?? 0);

if ($order_id <= 0) {
    responseJson(400, [
        "success" => false,
        "message" => "order_id không hợp lệ"
    ]);
}

// Lấy Bearer Token
$authorization = $_SERVER["HTTP_AUTHORIZATION"]
    ?? $_SERVER["REDIRECT_HTTP_AUTHORIZATION"]
    ?? "";

if ($authorization === "" && function_exists("getallheaders")) {
    foreach (getallheaders() as $key => $value) {
        if (strcasecmp($key, "Authorization") === 0) {
            $authorization = $value;
            break;
        }
    }
}

if (!preg_match('/^Bearer\s+(\S+)$/i', $authorization, $matches)) {
    responseJson(401, [
        "success" => false,
        "message" => "Thiếu Bearer Token"
    ]);
}

$token_hash = hash("sha256", $matches[1]);

try {

    // Xác thực tài khoản CUSTOMER
    $sql = "
        SELECT u.user_id
        FROM user_tokens t
        JOIN users u ON u.user_id = t.user_id
        WHERE t.token_hash = ?
          AND t.expires_at > NOW()
          AND u.role = 'CUSTOMER'
        LIMIT 1
    ";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("s", $token_hash);
    $stmt->execute();

    $customer = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    if (!$customer) {
        responseJson(401, [
            "success" => false,
            "message" => "Token không hợp lệ hoặc đã hết hạn"
        ]);
    }

    $user_id = intval($customer["user_id"]);

    // Lấy đơn hàng thuộc CUSTOMER
    $sql = "
        SELECT order_id, user_id, order_status
        FROM orders
        WHERE order_id = ?
          AND user_id = ?
        LIMIT 1
    ";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ii", $order_id, $user_id);
    $stmt->execute();

    $order = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    if (!$order) {
        responseJson(404, [
            "success" => false,
            "message" => "Không tìm thấy đơn hàng của bạn"
        ]);
    }

    // Lấy phân công tài xế còn hiệu lực
    $sql = "
        SELECT
            oa.driver_id,
            oa.assignment_status,
            u.full_name AS driver_name,
            u.phone AS driver_phone
        FROM order_assignments oa
        JOIN drivers d ON d.driver_id = oa.driver_id
        JOIN users u ON u.user_id = d.user_id
        WHERE oa.order_id = ?
          AND oa.assignment_status IN (
              'OFFERED',
              'ACCEPTED',
              'PICKED_UP',
              'DELIVERED'
          )
        ORDER BY oa.assignment_id DESC
        LIMIT 1
    ";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $order_id);
    $stmt->execute();

    $assignment = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    $assignment_status = $assignment["assignment_status"] ?? null;

    $driver = null;

    if ($assignment && in_array(
        $assignment_status,
        ["ACCEPTED", "PICKED_UP", "DELIVERED"],
        true
    )) {
        $driver = [
            "driver_id" => intval($assignment["driver_id"]),
            "full_name" => $assignment["driver_name"],
            "phone" => $assignment["driver_phone"]
        ];
    }

    responseJson(200, [
        "success" => true,
        "order_id" => intval($order["order_id"]),
        "order_status" => $order["order_status"],
        "assignment_status" => $assignment_status,
        "driver" => $driver
    ]);

} catch (Throwable $e) {

    error_log("Tracking API error: " . $e->getMessage());

    responseJson(500, [
        "success" => false,
        "message" => "Lỗi hệ thống khi lấy trạng thái đơn hàng"
    ]);
}
