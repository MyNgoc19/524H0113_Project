<?php

header("Content-Type: application/json; charset=UTF-8");

require_once "../config/database.php";

$sql = "
    SELECT
        food_id,
        category_id,
        food_name,
        price,
        description,
        image_url,
        is_featured
    FROM foods
    ORDER BY food_id ASC
";

$result = $conn->query($sql);

$foods = [];

while ($row = $result->fetch_assoc()) {
    $foods[] = $row;
}

echo json_encode([
    "success" => true,
    "data" => $foods
], JSON_UNESCAPED_UNICODE);

$conn->close();

?>