
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

if ($_SERVER["REQUEST_METHOD"] !== "GET") {
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

$tokenHash = hash("sha256", trim($matches[1]));

// XÁC THỰC TOKEN
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
    $_GET["order_id"] ?? null,
    FILTER_VALIDATE_INT
);

if (!$orderId || $orderId <= 0) {
    sendResponse(422, false, "order_id không hợp lệ");
}

if ($role !== "CUSTOMER" && $role !== "DRIVER") {
    sendResponse(403, false, "Vai trò không được phép chat");
}

// KIỂM TRA QUYỀN ĐỌC TIN NHẮN
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
        "Bạn không có quyền xem tin nhắn đơn hàng này"
    );
}

// LẤY TIN NHẮN
$stmt = $conn->prepare("
    SELECT
        m.message_id,
        m.order_id,
        m.sender_id,
        u.full_name AS sender_name,
        m.message,
        m.created_at
    FROM order_messages m
    JOIN users u ON u.user_id = m.sender_id
    WHERE m.order_id = ?
    ORDER BY m.created_at ASC, m.message_id ASC
");

$stmt->bind_param("i", $orderId);
$stmt->execute();

$result = $stmt->get_result();
$messages = [];

while ($row = $result->fetch_assoc()) {
    $messages[] = [
        "message_id" => (int) $row["message_id"],
        "order_id" => (int) $row["order_id"],
        "sender_id" => (int) $row["sender_id"],
        "sender_name" => $row["sender_name"],
        "message" => $row["message"],
        "created_at" => $row["created_at"]
    ];
}

$stmt->close();

echo json_encode([
    "success" => true,
    "order_id" => $orderId,
    "messages" => $messages
], JSON_UNESCAPED_UNICODE);
