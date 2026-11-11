
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

$driver_id = filter_var(
    $_POST["driver_id"] ?? null,
    FILTER_VALIDATE_INT
);

$latitude = filter_var(
    $_POST["latitude"] ?? null,
    FILTER_VALIDATE_FLOAT
);

$longitude = filter_var(
    $_POST["longitude"] ?? null,
    FILTER_VALIDATE_FLOAT
);

if (
    !$driver_id ||
    $latitude === false ||
    $longitude === false ||
    $latitude < -90 ||
    $latitude > 90 ||
    $longitude < -180 ||
    $longitude > 180
) {
    respond(400, [
        "success" => false,
        "message" => "Thông tin vị trí không hợp lệ"
    ]);
}

try {
    $stmt = $conn->prepare("
        UPDATE drivers
        SET
            current_latitude = ?,
            current_longitude = ?,
            last_location_update = NOW()
        WHERE driver_id = ?
    ");

    $stmt->bind_param(
        "ddi",
        $latitude,
        $longitude,
        $driver_id
    );

    $stmt->execute();

    if ($stmt->affected_rows === 0) {
        $stmt->close();

        respond(404, [
            "success" => false,
            "message" => "Không tìm thấy tài xế hoặc vị trí chưa thay đổi"
        ]);
    }

    $stmt->close();

    respond(200, [
        "success" => true,
        "message" => "Cập nhật vị trí thành công",
        "driver_id" => $driver_id,
        "latitude" => $latitude,
        "longitude" => $longitude
    ]);

} catch (Throwable $e) {
    error_log($e->getMessage());

    respond(500, [
        "success" => false,
        "message" => "Lỗi server"
    ]);
}
