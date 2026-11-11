
<?php

header("Content-Type: application/json; charset=UTF-8");

require_once "../config/database.php";


// Hỗ trợ dữ liệu JSON và form-data
$data = json_decode(file_get_contents("php://input"), true);

if (!is_array($data)) {
    $data = $_POST;
}

$email = trim($data["email"] ?? "");
$password = $data["password"] ?? "";


if (empty($email) || empty($password)) {

    echo json_encode([
        "success" => false,
        "message" => "Vui lòng nhập email và mật khẩu"
    ], JSON_UNESCAPED_UNICODE);

    exit;
}


try {

    $sql = "
        SELECT
            user_id,
            full_name,
            email,
            phone,
            password_hash,
            role
        FROM users
        WHERE email = ?
        LIMIT 1
    ";


    $stmt = $conn->prepare($sql);

    $stmt->bind_param(
        "s",
        $email
    );

    $stmt->execute();


    $result =
        $stmt->get_result();


    // Không tìm thấy email
    if ($result->num_rows == 0) {

        echo json_encode([
            "success" => false,
            "message" => "Email hoặc mật khẩu không đúng"
        ], JSON_UNESCAPED_UNICODE);

        exit;
    }


    $user =
        $result->fetch_assoc();


    // Kiểm tra password
    if (!password_verify(
        $password,
        $user["password_hash"]
    )) {

        echo json_encode([
            "success" => false,
            "message" => "Email hoặc mật khẩu không đúng"
        ], JSON_UNESCAPED_UNICODE);

        exit;
    }


    // Lấy driver_id nếu tài khoản là tài xế
    $driver_id = null;

    if ($user["role"] === "DRIVER") {

        $driverSql = "
            SELECT driver_id
            FROM drivers
            WHERE user_id = ?
            LIMIT 1
        ";

        $driverStmt = $conn->prepare($driverSql);

        $driverStmt->bind_param(
            "i",
            $user["user_id"]
        );

        $driverStmt->execute();

        $driverResult = $driverStmt->get_result();

        $driver = $driverResult->fetch_assoc();

        if ($driver) {
            $driver_id = (int) $driver["driver_id"];
        }

        $driverStmt->close();
    }


    // Tạo access token đăng nhập
    $token = bin2hex(random_bytes(32));

    // Chỉ lưu mã băm token trong database
    $token_hash = hash("sha256", $token);

    // Token có thời hạn 24 giờ
    $expires_in = 24 * 60 * 60;

    $expires_at = date(
        "Y-m-d H:i:s",
        time() + $expires_in
    );


    // Lưu token vào bảng user_tokens
    $tokenSql = "
        INSERT INTO user_tokens (
            user_id,
            token_hash,
            expires_at
        )
        VALUES (?, ?, ?)
    ";

    $tokenStmt = $conn->prepare($tokenSql);

    $tokenStmt->bind_param(
        "iss",
        $user["user_id"],
        $token_hash,
        $expires_at
    );

    $tokenStmt->execute();
    $tokenStmt->close();


    // Đăng nhập thành công
    echo json_encode([
        "success" => true,
        "message" => "Đăng nhập thành công",

        "access_token" => $token,
        "token_type" => "Bearer",
        "expires_in" => $expires_in,

        "user" => [

            "user_id" =>
                (int) $user["user_id"],

            "full_name" =>
                $user["full_name"],

            "email" =>
                $user["email"],

            "phone" =>
                $user["phone"],

            "role" =>
                $user["role"],

            "driver_id" =>
                $driver_id
        ]

    ], JSON_UNESCAPED_UNICODE);


    $stmt->close();


} catch (Throwable $e) {

    http_response_code(500);

    error_log($e->getMessage());

    echo json_encode([
        "success" => false,
        "message" => "Lỗi hệ thống, vui lòng thử lại"
    ], JSON_UNESCAPED_UNICODE);
}


$conn->close();

?>
