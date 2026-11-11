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


    $quantity =
        intval(
            $_POST["quantity"] ?? 0
        );


    if ($cart_item_id <= 0) {

        sendResponse(
            false,
            "cart_item_id không hợp lệ"
        );
    }


    if ($quantity <= 0) {

        sendResponse(
            false,
            "Số lượng phải lớn hơn 0"
        );
    }


    $stmt = $conn->prepare(
        "UPDATE cart_items
         SET quantity = ?
         WHERE cart_item_id = ?"
    );


    $stmt->bind_param(
        "ii",
        $quantity,
        $cart_item_id
    );


    $stmt->execute();


    if ($stmt->affected_rows >= 0) {

        sendResponse(
            true,
            "Cập nhật số lượng thành công"
        );

    } else {

        sendResponse(
            false,
            "Không thể cập nhật số lượng"
        );
    }


} catch (Throwable $e) {

    sendResponse(
        false,
        "Lỗi server: "
        . $e->getMessage()
    );
}
?>