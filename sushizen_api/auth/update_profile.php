<?php

header("Content-Type: application/json; charset=UTF-8");

ini_set("display_errors", 0);
error_reporting(E_ALL);

require_once "../config/database.php";


function sendResponse($success, $message) {

    echo json_encode([
        "success" => $success,
        "message" => $message
    ], JSON_UNESCAPED_UNICODE);

    exit;
}


try {

    // =========================================
    // NHẬN DATA TỪ ANDROID
    // =========================================

    $user_id =
        intval($_POST["user_id"] ?? 0);

    $full_name =
        trim($_POST["full_name"] ?? "");

    $email =
        trim($_POST["email"] ?? "");

    $phone =
        trim($_POST["phone"] ?? "");


    // =========================================
    // VALIDATE
    // =========================================

    if ($user_id <= 0) {

        sendResponse(
            false,
            "user_id không hợp lệ"
        );
    }


    if ($full_name == "") {

        sendResponse(
            false,
            "Họ tên không được để trống"
        );
    }


    if ($email == "") {

        sendResponse(
            false,
            "Email không được để trống"
        );
    }


    if ($phone == "") {

        sendResponse(
            false,
            "Số điện thoại không được để trống"
        );
    }


    // =========================================
    // KIỂM TRA USER
    // =========================================

    $stmt = $conn->prepare(
        "SELECT user_id
         FROM users
         WHERE user_id = ?
         LIMIT 1"
    );

    $stmt->bind_param(
        "i",
        $user_id
    );

    $stmt->execute();

    $result =
        $stmt->get_result();


    if ($result->num_rows == 0) {

        $stmt->close();

        sendResponse(
            false,
            "Không tìm thấy người dùng"
        );
    }

    $stmt->close();


    // =========================================
    // KIỂM TRA EMAIL TRÙNG
    // =========================================

    $stmt = $conn->prepare(
        "SELECT user_id
         FROM users
         WHERE email = ?
         AND user_id != ?
         LIMIT 1"
    );

    $stmt->bind_param(
        "si",
        $email,
        $user_id
    );

    $stmt->execute();

    $result =
        $stmt->get_result();


    if ($result->num_rows > 0) {

        $stmt->close();

        sendResponse(
            false,
            "Email đã được sử dụng"
        );
    }

    $stmt->close();


    // =========================================
    // UPDATE USER
    // =========================================

    $stmt = $conn->prepare(
        "UPDATE users
         SET full_name = ?,
             email = ?,
             phone = ?
         WHERE user_id = ?"
    );

    $stmt->bind_param(
        "sssi",
        $full_name,
        $email,
        $phone,
        $user_id
    );


    if (!$stmt->execute()) {

        throw new Exception(
            $stmt->error
        );
    }


    $stmt->close();


    sendResponse(
        true,
        "Cập nhật thông tin thành công"
    );


} catch (Throwable $e) {

    sendResponse(
        false,
        "Lỗi server: " . $e->getMessage()
    );
}


if (isset($conn)) {

    $conn->close();
}

?>