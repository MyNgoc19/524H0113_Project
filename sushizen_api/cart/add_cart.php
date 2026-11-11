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
    // NHẬN DATA
    // =========================================

    $user_id =
        intval($_POST["user_id"] ?? 0);

    $food_id =
        intval($_POST["food_id"] ?? 0);


    if ($user_id <= 0 || $food_id <= 0) {

        sendResponse(
            false,
            "Thiếu user_id hoặc food_id"
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

        sendResponse(
            false,
            "Người dùng không tồn tại"
        );
    }

    $stmt->close();


    // =========================================
    // KIỂM TRA FOOD
    // =========================================

    $stmt = $conn->prepare(
        "SELECT food_id
         FROM foods
         WHERE food_id = ?
         LIMIT 1"
    );

    $stmt->bind_param(
        "i",
        $food_id
    );

    $stmt->execute();

    $result =
        $stmt->get_result();


    if ($result->num_rows == 0) {

        sendResponse(
            false,
            "Món ăn không tồn tại"
        );
    }

    $stmt->close();


    // =========================================
    // TÌM CART CỦA USER
    // =========================================

    $stmt = $conn->prepare(
        "SELECT cart_id
         FROM carts
         WHERE user_id = ?
         ORDER BY cart_id DESC
         LIMIT 1"
    );

    $stmt->bind_param(
        "i",
        $user_id
    );

    $stmt->execute();

    $result =
        $stmt->get_result();


    // =========================================
    // ĐÃ CÓ CART
    // =========================================

    if ($result->num_rows > 0) {

        $row =
            $result->fetch_assoc();

        $cart_id =
            intval($row["cart_id"]);

        $stmt->close();

    } else {

        // =====================================
        // CHƯA CÓ CART → TẠO
        // =====================================

        $stmt->close();


        $stmt = $conn->prepare(
            "INSERT INTO carts (user_id)
             VALUES (?)"
        );

        $stmt->bind_param(
            "i",
            $user_id
        );

        $stmt->execute();


        $cart_id =
            $conn->insert_id;


        $stmt->close();
    }


    // =========================================
    // KIỂM TRA MÓN TRONG ĐÚNG CART
    // =========================================

    $stmt = $conn->prepare(
        "SELECT
            cart_item_id,
            quantity
         FROM cart_items
         WHERE cart_id = ?
         AND food_id = ?
         LIMIT 1"
    );

    $stmt->bind_param(
        "ii",
        $cart_id,
        $food_id
    );

    $stmt->execute();

    $result =
        $stmt->get_result();


    // =========================================
    // ĐÃ CÓ → QUANTITY + 1
    // =========================================

    if ($result->num_rows > 0) {

        $row =
            $result->fetch_assoc();


        $cart_item_id =
            intval(
                $row["cart_item_id"]
            );


        $stmt->close();


        $stmt = $conn->prepare(
            "UPDATE cart_items
             SET quantity = quantity + 1
             WHERE cart_item_id = ?"
        );


        $stmt->bind_param(
            "i",
            $cart_item_id
        );


        $stmt->execute();

        $stmt->close();


        sendResponse(
            true,
            "Đã tăng số lượng món"
        );
    }


    // =========================================
    // CHƯA CÓ → INSERT MÓN MỚI
    // =========================================

    $stmt->close();


    $stmt = $conn->prepare(
        "INSERT INTO cart_items
        (
            cart_id,
            food_id,
            quantity
        )
        VALUES (?, ?, 1)"
    );


    $stmt->bind_param(
        "ii",
        $cart_id,
        $food_id
    );


    $stmt->execute();

    $stmt->close();


    sendResponse(
        true,
        "Đã thêm vào giỏ hàng"
    );


} catch (Throwable $e) {

    sendResponse(
        false,
        "Lỗi server: "
        . $e->getMessage()
    );
}
?>