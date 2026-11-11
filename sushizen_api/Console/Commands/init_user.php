
<?php

// Tao tai khoan SushiZen bang Terminal
// Su dung ket noi MySQL hien tai

require_once __DIR__ . '/../../config/database.php';

if (PHP_SAPI !== 'cli') {
    exit("This command must run in Terminal.\n");
}

function ask(string $message): string
{
    echo $message;
    return trim(fgets(STDIN));
}

echo "\n===== SUSHIZEN - CREATE USER =====\n\n";

$fullName = ask("Full name: ");
$email = ask("Email: ");
$phone = ask("Phone: ");
$password = ask("Password: ");
$confirmPassword = ask("Confirm password: ");
$role = strtoupper(ask("Role (CUSTOMER/DRIVER/ADMIN): "));

if (
    $fullName === '' ||
    $email === '' ||
    $phone === '' ||
    $password === ''
) {
    exit("Error: All fields are required.\n");
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    exit("Error: Invalid email.\n");
}

if ($password !== $confirmPassword) {
    exit("Error: Passwords do not match.\n");
}

if (!in_array($role, ['CUSTOMER', 'DRIVER', 'ADMIN'], true)) {
    exit("Error: Invalid role.\n");
}

$vehicleType = null;
$licensePlate = null;

if ($role === 'DRIVER') {
    echo "\n===== DRIVER INFORMATION =====\n";

    $vehicleType = ask("Vehicle type: ");
    $licensePlate = ask("License plate: ");

    if ($vehicleType === '' || $licensePlate === '') {
        exit("Error: Driver information is required.\n");
    }
}

try {
    $conn->begin_transaction();

    $check = $conn->prepare(
        "SELECT user_id FROM users WHERE email = ?"
    );

    $check->bind_param("s", $email);
    $check->execute();

    if ($check->get_result()->num_rows > 0) {
        throw new Exception("Email already exists.");
    }

    $check->close();

    $passwordHash = password_hash(
        $password,
        PASSWORD_DEFAULT
    );

    $stmt = $conn->prepare(
        "INSERT INTO users
        (full_name, email, phone, password_hash, role)
        VALUES (?, ?, ?, ?, ?)"
    );

    $stmt->bind_param(
        "sssss",
        $fullName,
        $email,
        $phone,
        $passwordHash,
        $role
    );

    $stmt->execute();

    $userId = $conn->insert_id;
    $stmt->close();

    if ($role === 'DRIVER') {
        $stmt = $conn->prepare(
            "INSERT INTO drivers
            (user_id, vehicle_type, license_plate)
            VALUES (?, ?, ?)"
        );

        $stmt->bind_param(
            "iss",
            $userId,
            $vehicleType,
            $licensePlate
        );

        $stmt->execute();
        $stmt->close();
    }

    $conn->commit();

    echo "\n===== SUCCESS =====\n";
    echo "User ID: $userId\n";
    echo "Full name: $fullName\n";
    echo "Email: $email\n";
    echo "Role: $role\n";

    if ($role === 'DRIVER') {
        echo "Driver profile created successfully!\n";
    }

} catch (Throwable $e) {
    $conn->rollback();
    echo "\nError: " . $e->getMessage() . "\n";
}

$conn->close();
