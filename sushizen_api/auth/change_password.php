<?php
header("Content-Type: application/json; charset=UTF-8");
require_once __DIR__ . "/../config/database.php";

function reply($status, $message) {
    http_response_code($status);
    echo json_encode(["success" => $status === 200, "message" => $message], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    reply(405, "Chỉ hỗ trợ POST");
}

$authorization = $_SERVER["HTTP_AUTHORIZATION"] ?? $_SERVER["REDIRECT_HTTP_AUTHORIZATION"] ?? "";
if (!$authorization && function_exists("getallheaders")) {
    foreach (getallheaders() as $key => $value) {
        if (strcasecmp($key, "Authorization") === 0) {
            $authorization = $value;
            break;
        }
    }
}
if (!preg_match('/^Bearer\s+(\S+)$/i', $authorization, $matches)) {
    reply(401, "Thiếu Bearer Token");
}

$current_password = $_POST["current_password"] ?? "";
$new_password = $_POST["new_password"] ?? "";
if ($current_password === "" || $new_password === "") {
    reply(400, "Vui lòng nhập đầy đủ mật khẩu cũ và mới");
}
if ($current_password === $new_password) {
    reply(400, "Mật khẩu mới phải khác mật khẩu hiện tại");
}

try {
    $token_hash = hash("sha256", $matches[1]);
    $stmt = $conn->prepare("SELECT u.user_id, u.password_hash FROM user_tokens t JOIN users u ON u.user_id = t.user_id WHERE t.token_hash = ? AND t.expires_at > NOW() LIMIT 1");
    $stmt->bind_param("s", $token_hash);
    $stmt->execute();
    $user = $stmt->get_result()->fetch_assoc();
    $stmt->close();
    if (!$user) {
        reply(401, "Phiên đăng nhập không hợp lệ hoặc đã hết hạn");
    }
    if (!password_verify($current_password, $user["password_hash"])) {
        reply(400, "Mật khẩu hiện tại không đúng");
    }
    $new_hash = password_hash($new_password, PASSWORD_DEFAULT);
    $stmt = $conn->prepare("UPDATE users SET password_hash = ? WHERE user_id = ?");
    $user_id = (int) $user["user_id"];
    $stmt->bind_param("si", $new_hash, $user_id);
    $stmt->execute();
    $stmt->close();
    reply(200, "Đổi mật khẩu thành công");
} catch (Throwable $e) {
    error_log("Change password error: " . $e->getMessage());
    reply(500, "Lỗi hệ thống khi đổi mật khẩu");
}
