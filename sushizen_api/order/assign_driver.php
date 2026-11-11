
<?php

header("Content-Type: application/json; charset=UTF-8");

require_once __DIR__ . "/../config/database.php";

function respond(int $status, array $data): void
{
    http_response_code($status);

    echo json_encode(
        $data,
        JSON_UNESCAPED_UNICODE
    );

    exit;
}

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    respond(405, [
        "success" => false,
        "message" => "Chỉ hỗ trợ POST"
    ]);
}

$order_id = filter_var(
    $_POST["order_id"] ?? null,
    FILTER_VALIDATE_INT
);

if (!$order_id || $order_id <= 0) {
    respond(400, [
        "success" => false,
        "message" => "order_id không hợp lệ"
    ]);
}

try {
    $conn->begin_transaction();

    // Khóa đơn hàng để tránh phân công trùng
    $stmt = $conn->prepare("
        SELECT
            o.order_id,
            o.order_status,
            o.branch_id,
            b.branch_name,
            b.latitude,
            b.longitude
        FROM orders o
        INNER JOIN branches b
            ON o.branch_id = b.branch_id
        WHERE o.order_id = ?
          AND b.is_active = 1
        FOR UPDATE
    ");

    $stmt->bind_param("i", $order_id);
    $stmt->execute();

    $order = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    if (!$order) {
        $conn->rollback();

        respond(404, [
            "success" => false,
            "message" => "Không tìm thấy đơn hoặc chi nhánh"
        ]);
    }

    // Kiểm tra đơn đã có tài xế đang được phân công chưa
    $stmt = $conn->prepare("
        SELECT assignment_id
        FROM order_assignments
        WHERE order_id = ?
          AND assignment_status IN (
              'OFFERED',
              'ACCEPTED',
              'PICKED_UP'
          )
        LIMIT 1
    ");

    $stmt->bind_param("i", $order_id);
    $stmt->execute();

    $existing = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    if ($existing) {
        $conn->rollback();

        respond(409, [
            "success" => false,
            "message" => "Đơn hàng đã có phân công đang hoạt động"
        ]);
    }

    // Tìm tài xế gần chi nhánh nhất
    // Chỉ dùng vị trí được cập nhật trong 5 phút gần đây
    $sql = "
        SELECT
            d.driver_id,
            u.full_name,

            (
                6371 * ACOS(
                    LEAST(
                        1,
                        GREATEST(
                            -1,
                            COS(RADIANS(?))
                            * COS(RADIANS(d.current_latitude))
                            * COS(
                                RADIANS(d.current_longitude)
                                - RADIANS(?)
                            )
                            + SIN(RADIANS(?))
                            * SIN(RADIANS(d.current_latitude))
                        )
                    )
                )
            ) AS distance_km

        FROM drivers d
        INNER JOIN users u
            ON d.user_id = u.user_id

        WHERE d.is_online = 1
          AND d.availability_status = 'AVAILABLE'
          AND u.role = 'DRIVER'
          AND d.current_latitude IS NOT NULL
          AND d.current_longitude IS NOT NULL
          AND d.last_location_update >=
              DATE_SUB(NOW(), INTERVAL 5 MINUTE)

          AND NOT EXISTS (
              SELECT 1
              FROM order_assignments oa
              WHERE oa.driver_id = d.driver_id
                AND oa.assignment_status IN (
                    'OFFERED',
                    'ACCEPTED',
                    'PICKED_UP'
                )
          )

        ORDER BY distance_km ASC
        LIMIT 1
    ";

    $latitude = (float) $order["latitude"];
    $longitude = (float) $order["longitude"];

    $stmt = $conn->prepare($sql);

    $stmt->bind_param(
        "ddd",
        $latitude,
        $longitude,
        $latitude
    );

    $stmt->execute();

    $driver = $stmt->get_result()->fetch_assoc();
    $stmt->close();

    if (!$driver) {
        $conn->rollback();

        respond(404, [
            "success" => false,
            "message" => "Không tìm thấy tài xế đang rảnh"
        ]);
    }

    $driver_id = (int) $driver["driver_id"];

    // Tạo phân công cho tài xế
    $stmt = $conn->prepare("
        INSERT INTO order_assignments
        (order_id, driver_id, assignment_status)
        VALUES (?, ?, 'OFFERED')
    ");

    $stmt->bind_param(
        "ii",
        $order_id,
        $driver_id
    );

    $stmt->execute();

    $assignment_id = $conn->insert_id;
    $stmt->close();

    $conn->commit();

    respond(200, [
        "success" => true,
        "message" => "Đã đề nghị đơn cho tài xế gần nhất",
        "assignment_id" => $assignment_id,
        "order_id" => $order_id,
        "driver_id" => $driver_id,
        "driver_name" => $driver["full_name"],
        "distance_km" => round(
            (float) $driver["distance_km"],
            2
        ),
        "assignment_status" => "OFFERED"
    ]);

} catch (Throwable $e) {
    $conn->rollback();

    error_log($e->getMessage());

    respond(500, [
        "success" => false,
        "message" => "Lỗi server"
    ]);
}
