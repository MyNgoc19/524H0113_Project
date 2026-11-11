<?php

header("Content-Type: application/json; charset=UTF-8");

ini_set("display_errors", 0);
error_reporting(E_ALL);


// =====================================================
// DATABASE
// =====================================================

require_once "../config/database.php";


// =====================================================
// RESPONSE
// =====================================================

function sendResponse(
    $success,
    $message,
    $order_id = 0,
    $subtotal = 0,
    $shipping_fee = 0,
    $total_amount = 0
) {

    echo json_encode([

        "success" => $success,

        "message" => $message,

        "order_id" => intval($order_id),

        "subtotal" => floatval($subtotal),

        "shipping_fee" => floatval($shipping_fee),

        "total_amount" => floatval($total_amount)

    ], JSON_UNESCAPED_UNICODE);

    exit;
}


// =====================================================
// MAIN
// =====================================================

try {

    // =================================================
    // CHỈ CHẤP NHẬN POST
    // =================================================

    if ($_SERVER["REQUEST_METHOD"] !== "POST") {

        sendResponse(
            false,
            "Phương thức không hợp lệ"
        );
    }


    // =================================================
    // NHẬN DATA TỪ ANDROID
    // =================================================

    $user_id =
        intval(
            $_POST["user_id"] ?? 0
        );


    $receiver_name =
        trim(
            $_POST["receiver_name"] ?? ""
        );


    $receiver_phone =
        trim(
            $_POST["receiver_phone"] ?? ""
        );


    $delivery_address =
        trim(
            $_POST["delivery_address"] ?? ""
        );


    $shipping_fee =
        floatval(
            $_POST["shipping_fee"] ?? 0
        );


    $payment_method =
        trim(
            $_POST["payment_method"] ?? ""
        );


    // =================================================
    // VALIDATE USER
    // =================================================

    if ($user_id <= 0) {

        sendResponse(
            false,
            "Thiếu user_id"
        );
    }


    // =================================================
    // VALIDATE NAME
    // =================================================

    if ($receiver_name === "") {

        sendResponse(
            false,
            "Thiếu tên người nhận"
        );
    }


    // =================================================
    // VALIDATE PHONE
    // =================================================

    if ($receiver_phone === "") {

        sendResponse(
            false,
            "Thiếu số điện thoại"
        );
    }


    // =================================================
    // VALIDATE ADDRESS
    // =================================================

    if ($delivery_address === "") {

        sendResponse(
            false,
            "Thiếu địa chỉ giao hàng"
        );
    }


    // =================================================
    // VALIDATE PAYMENT
    // =================================================

    if ($payment_method === "") {

        sendResponse(
            false,
            "Chưa chọn phương thức thanh toán"
        );
    }


    // =================================================
    // KIỂM TRA USER TỒN TẠI
    // =================================================

    $stmt =
        $conn->prepare(
            "SELECT user_id
             FROM users
             WHERE user_id = ?
             LIMIT 1"
        );


    if (!$stmt) {

        throw new Exception(
            "Prepare user lỗi: "
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


    if ($result->num_rows == 0) {

        $stmt->close();

        sendResponse(
            false,
            "Người dùng không tồn tại"
        );
    }


    $stmt->close();


    // =================================================
    // TÌM CART CỦA USER
    // =================================================

    $stmt =
        $conn->prepare(
            "SELECT cart_id
             FROM carts
             WHERE user_id = ?
             ORDER BY cart_id DESC
             LIMIT 1"
        );


    if (!$stmt) {

        throw new Exception(
            "Prepare cart lỗi: "
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


    if ($result->num_rows == 0) {

        $stmt->close();

        sendResponse(
            false,
            "Không tìm thấy giỏ hàng"
        );
    }


    $cart =
        $result->fetch_assoc();


    $cart_id =
        intval(
            $cart["cart_id"]
        );


    $stmt->close();


    // =================================================
    // LẤY CART ITEMS
    // =================================================

    $stmt =
        $conn->prepare(
            "SELECT

                ci.food_id,
                ci.quantity,

                f.price

             FROM cart_items ci

             INNER JOIN foods f
                ON ci.food_id = f.food_id

             WHERE ci.cart_id = ?"
        );


    if (!$stmt) {

        throw new Exception(
            "Prepare cart items lỗi: "
            . $conn->error
        );
    }


    $stmt->bind_param(
        "i",
        $cart_id
    );


    $stmt->execute();


    $result =
        $stmt->get_result();


    // =================================================
    // CART EMPTY
    // =================================================

    if ($result->num_rows == 0) {

        $stmt->close();

        sendResponse(
            false,
            "Giỏ hàng đang trống"
        );
    }


    // =================================================
    // LƯU ITEMS TẠM
    // =================================================

    $items = [];


    // =================================================
    // SUBTOTAL
    // =================================================

    $subtotal = 0;


    while (
        $row = $result->fetch_assoc()
    ) {

        $food_id =
            intval(
                $row["food_id"]
            );


        $quantity =
            intval(
                $row["quantity"]
            );


        $unit_price =
            floatval(
                $row["price"]
            );


        // =============================================
        // TÍNH SUBTOTAL
        // =============================================

        $subtotal +=
            $quantity
            * $unit_price;


        // =============================================
        // LƯU ITEM
        // =============================================

        $items[] = [

            "food_id" =>
                $food_id,

            "quantity" =>
                $quantity,

            "unit_price" =>
                $unit_price
        ];
    }


    $stmt->close();


    // =================================================
    // TOTAL
    // =================================================

    $total_amount =
        $subtotal
        + $shipping_fee;


    // =================================================
    // PAYMENT STATUS
    // =================================================

    if ($payment_method === "COD") {

        $payment_status =
            "UNPAID";

    } else {

        $payment_status =
            "PENDING";
    }


    // =================================================
    // ORDER STATUS
    // =================================================

    $order_status =
        "PENDING";


    // =================================================
    // BẮT ĐẦU TRANSACTION
    // =================================================

    $conn->begin_transaction();


    // =================================================
    // INSERT ORDERS
    // =================================================

    $stmt =
        $conn->prepare(
            "INSERT INTO orders
            (
                user_id,
                receiver_name,
                receiver_phone,
                delivery_address,

                subtotal,
                shipping_fee,
                total_amount,

                payment_method,
                payment_status,
                order_status,

                created_at
            )

            VALUES
            (
                ?, ?, ?, ?,
                ?, ?, ?,
                ?, ?, ?,
                NOW()
            )"
        );


    if (!$stmt) {

        throw new Exception(
            "Prepare order lỗi: "
            . $conn->error
        );
    }


    $stmt->bind_param(

        "isssdddsss",

        $user_id,

        $receiver_name,

        $receiver_phone,

        $delivery_address,

        $subtotal,

        $shipping_fee,

        $total_amount,

        $payment_method,

        $payment_status,

        $order_status
    );


    // =================================================
    // EXECUTE ORDER
    // =================================================

    if (!$stmt->execute()) {

        throw new Exception(
            "Không thể tạo đơn hàng: "
            . $stmt->error
        );
    }


    // =================================================
    // ORDER ID
    // =================================================

    $order_id =
        intval(
            $conn->insert_id
        );


    $stmt->close();


    // =================================================
    // INSERT ORDER ITEMS
    // =================================================

    $stmt =
        $conn->prepare(
            "INSERT INTO order_items
            (
                order_id,
                food_id,
                quantity,
                unit_price
            )

            VALUES
            (
                ?, ?, ?, ?
            )"
        );


    if (!$stmt) {

        throw new Exception(
            "Prepare order_items lỗi: "
            . $conn->error
        );
    }


    // =================================================
    // LOOP ITEMS
    // =================================================

    foreach ($items as $item) {

        $food_id =
            intval(
                $item["food_id"]
            );


        $quantity =
            intval(
                $item["quantity"]
            );


        $unit_price =
            floatval(
                $item["unit_price"]
            );


        $stmt->bind_param(

            "iiid",

            $order_id,

            $food_id,

            $quantity,

            $unit_price
        );


        if (!$stmt->execute()) {

            throw new Exception(
                "Không thể lưu món ăn: "
                . $stmt->error
            );
        }
    }


    $stmt->close();


    // =================================================
    // XÓA CART ITEMS SAU KHI ĐẶT THÀNH CÔNG
    // =================================================

    $stmt =
        $conn->prepare(
            "DELETE FROM cart_items
             WHERE cart_id = ?"
        );


    if (!$stmt) {

        throw new Exception(
            "Prepare delete cart lỗi: "
            . $conn->error
        );
    }


    $stmt->bind_param(
        "i",
        $cart_id
    );


    if (!$stmt->execute()) {

        throw new Exception(
            "Không thể xóa giỏ hàng: "
            . $stmt->error
        );
    }


    $stmt->close();


    // =================================================
    // COMMIT
    // =================================================

    $conn->commit();


    // =================================================
    // SUCCESS
    // =================================================

    sendResponse(

        true,

        "Đặt hàng thành công",

        $order_id,

        $subtotal,

        $shipping_fee,

        $total_amount
    );


} catch (Throwable $e) {

    // =================================================
    // ROLLBACK
    // =================================================

    if (
        isset($conn)
        && $conn instanceof mysqli
    ) {

        try {

            $conn->rollback();

        } catch (Throwable $ignored) {

        }
    }


    // =================================================
    // JSON ERROR
    // =================================================

    sendResponse(

        false,

        "Lỗi server: "
        . $e->getMessage()
    );
}

?>