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

    $cart_item_id =
        intval(
            $_POST["cart_item_id"] ?? 0
        );


    if ($cart_item_id <= 0) {

        sendResponse(
            false,
            "cart_item_id không hợp lệ"
        );
    }


    $stmt = $conn->prepare(
        "DELETE FROM cart_items
         WHERE cart_item_id = ?"
    );


    $stmt->bind_param(
        "i",
        $cart_item_id
    );


    $stmt->execute();


    sendResponse(
        true,
        "Đã xóa món khỏi giỏ hàng"
    );


} catch (Throwable $e) {

    sendResponse(
        false,
        "Lỗi server: "
        . $e->getMessage()
    );
}
?>




