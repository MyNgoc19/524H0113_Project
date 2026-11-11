<?php

header("Content-Type: application/json; charset=UTF-8");

require_once "../config/database.php";


// =============================================
// NHẬN DATA TỪ ANDROID
// =============================================

$full_name = $_POST["full_name"] ?? "";
$email = $_POST["email"] ?? "";
$phone = $_POST["phone"] ?? "";
$password = $_POST["password"] ?? "";


// =============================================
// KIỂM TRA DATA
// =============================================

if (
    empty($full_name) ||
    empty($email) ||
    empty($phone) ||
    empty($password)
) {

    echo json_encode([
        "success" => false,
        "message" => "Vui lòng nhập đầy đủ thông tin"
    ], JSON_UNESCAPED_UNICODE);

    exit;
}


// =============================================
// KIỂM TRA EMAIL ĐÃ TỒN TẠI
// =============================================

$sqlCheck = "
    SELECT user_id
    FROM users
    WHERE email = ?
";

$stmtCheck = $conn->prepare($sqlCheck);

$stmtCheck->bind_param(
    "s",
    $email
);

$stmtCheck->execute();

$result =
    $stmtCheck->get_result();


if ($result->num_rows > 0) {

    echo json_encode([
        "success" => false,
        "message" => "Email đã được đăng ký"
    ], JSON_UNESCAPED_UNICODE);

    $conn->close();

    exit;
}


// =============================================
// HASH PASSWORD
// =============================================

$password_hash =
    password_hash(
        $password,
        PASSWORD_DEFAULT
    );


// =============================================
// INSERT USER
// =============================================

$sql = "
    INSERT INTO users
    (
        full_name,
        email,
        phone,
        password_hash
    )
    VALUES (?, ?, ?, ?)
";

$stmt =
    $conn->prepare($sql);

$stmt->bind_param(
    "ssss",
    $full_name,
    $email,
    $phone,
    $password_hash
);


if ($stmt->execute()) {

    echo json_encode([
        "success" => true,
        "message" => "Đăng ký thành công"
    ], JSON_UNESCAPED_UNICODE);

} else {

    echo json_encode([
        "success" => false,
        "message" => "Đăng ký thất bại"
    ], JSON_UNESCAPED_UNICODE);
}


$conn->close();

?>