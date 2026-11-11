
<?php

header("Content-Type: application/json; charset=UTF-8");

require_once "../config/database.php";

function sendResponse($status, $success, $message)
{
    http_response_code($status);

    echo json_encode([
        "success" => $success,
        "message" => $message
    ], JSON_UNESCAPED_UNICODE);

    exit;
}

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    sendResponse(405, false, "Phương thức không hợp lệ");
}

$authorization = $_SERVER["HTTP_AUTHORIZATION"] ?? "";

if (empty($authorization) && function_exists("getallheaders")) {
    foreach (getallheaders() as $key => $value) {
        if (strcasecmp($key, "Authorization") === 0) {
            $authorization = $value;
            break;
        }
    }
}

if (!preg_match('/^Bearer\s+(.+)$/i', $authorization, $matches)) {
    sendResponse(401, false, "Thiếu Bearer Token");
}

$token = trim($matches[1]);
$tokenHash = hash("sha256", $token);

// XÁC THỰC NGƯỜI ĐĂNG NHẬP
$stmt = $conn->prepare("
    SELECT u.user_id, u.role
    FROM user_tokens t
    JOIN users u ON u.user_id = t.user_id
    WHERE t.token_hash = ?
      AND t.expires_at > NOW()
    LIMIT 1
");

$stmt->bind_param("s", $tokenHash);
$stmt->execute();

$user = $stmt->get_result()->fetch_assoc();
$stmt->close();

if (!$user) {
    sendResponse(401, false, "Token không hợp lệ hoặc đã hết hạn");
}

$userId = (int) $user["user_id"];
$role = strtoupper($user["role"]);

$orderId = filter_var(
    $_POST["order_id"] ?? null,
    FILTER_VALIDATE_INT
);

$message = trim($_POST["message"] ?? "");

if (!$orderId || $orderId <= 0) {
    sendResponse(422, false, "order_id không hợp lệ");
}

if ($message === "") {
    sendResponse(422, false, "Vui lòng nhập tin nhắn");
}

if (mb_strlen($message, "UTF-8") > 2000) {
    sendResponse(422, false, "Tin nhắn tối đa 2000 ký tự");
}

if ($role !== "CUSTOMER" && $role !== "DRIVER") {
    sendResponse(403, false, "Vai trò không được phép chat");
}

// KIỂM TRA QUYỀN TRUY CẬP ĐƠN HÀNG
$stmt = $conn->prepare("
    SELECT o.order_id
    FROM orders o
    WHERE o.order_id = ?
      AND (
          (
              ? = 'CUSTOMER'
              AND o.user_id = ?
          )
          OR
          (
              ? = 'DRIVER'
              AND EXISTS (
                  SELECT 1
                  FROM order_assignments a
                  JOIN drivers d ON d.driver_id = a.driver_id
                  WHERE a.order_id = o.order_id
                    AND d.user_id = ?
                    AND a.assignment_status IN (
                        'ACCEPTED',
                        'PICKED_UP',
                        'DELIVERED'
                    )
              )
          )
      )
    LIMIT 1
");

$stmt->bind_param(
    "isisi",
    $orderId,
    $role,
    $userId,
    $role,
    $userId
);

$stmt->execute();
$allowed = $stmt->get_result()->fetch_assoc();
$stmt->close();

if (!$allowed) {
    sendResponse(
        403,
        false,
        "Bạn không có quyền chat trong đơn hàng này"
    );
}

// LƯU TIN NHẮN VÀO MYSQL
$stmt = $conn->prepare("
    INSERT INTO order_messages (
        order_id,
        sender_id,
        message
    )
    VALUES (?, ?, ?)
");

$stmt->bind_param(
    "iis",
    $orderId,
    $userId,
    $message
);

if (!$stmt->execute()) {
    $stmt->close();
    sendResponse(500, false, "Không thể lưu tin nhắn");
}

$messageId = $conn->insert_id;
$stmt->close();

http_response_code(201);

echo json_encode([
    "success" => true,
    "message" => "Gửi tin nhắn thành công",
    "message_id" => $messageId,
    "order_id" => $orderId,
    "sender_id" => $userId
], JSON_UNESCAPED_UNICODE);
