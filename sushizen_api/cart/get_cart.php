<?php

header("Content-Type: application/json; charset=UTF-8");

ini_set("display_errors", 0);
error_reporting(E_ALL);

try {

    require_once "../config/database.php";


    // ============================================
    // NHẬN USER_ID
    // ============================================

    $user_id = isset($_POST["user_id"])
        ? intval($_POST["user_id"])
        : 0;


    // ============================================
    // KIỂM TRA USER_ID
    // ============================================

    if ($user_id <= 0) {

        echo json_encode([
            "success" => false,
            "message" => "Thiếu user_id",
            "data" => []
        ], JSON_UNESCAPED_UNICODE);

        exit;
    }


    // ============================================
    // LẤY GIỎ HÀNG CỦA USER
    // ============================================

    $sql = "
        SELECT
            ci.cart_item_id,
            ci.food_id,
            ci.quantity,

            f.food_name,
            f.price,
            f.description,
            f.image_url

        FROM cart_items ci

        INNER JOIN carts c
            ON ci.cart_id = c.cart_id

        INNER JOIN foods f
            ON ci.food_id = f.food_id

        WHERE c.user_id = ?

        ORDER BY ci.cart_item_id DESC
    ";


    $stmt = $conn->prepare($sql);


    if (!$stmt) {

        throw new Exception(
            "Prepare get cart lỗi: "
            . $conn->error
        );
    }


    $stmt->bind_param(
        "i",
        $user_id
    );


    $stmt->execute();


    $result =
        $stmt->get_result();


    // ============================================
    // TẠO DANH SÁCH
    // ============================================

    $data = [];


    while ($row = $result->fetch_assoc()) {

        $data[] = [
            "cart_item_id" =>
                intval($row["cart_item_id"]),

            "food_id" =>
                intval($row["food_id"]),

            "quantity" =>
                intval($row["quantity"]),

            "food_name" =>
                $row["food_name"],

            "price" =>
                floatval($row["price"]),

            "description" =>
                $row["description"],

            "image_url" =>
                $row["image_url"]
        ];
    }


    // ============================================
    // TRẢ JSON
    // ============================================

    echo json_encode([
        "success" => true,
        "message" => "Lấy giỏ hàng thành công",
        "data" => $data
    ], JSON_UNESCAPED_UNICODE);


    $stmt->close();


} catch (Throwable $e) {

    echo json_encode([
        "success" => false,
        "message" =>
            "Lỗi server: "
            . $e->getMessage(),

        "data" => []
    ], JSON_UNESCAPED_UNICODE);
}


if (isset($conn)) {

    $conn->close();
}

?>