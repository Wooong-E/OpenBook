<?php
header("Content-Type: application/json; charset=UTF-8");

$conn = new mysqli("localhost", "whdnd5725", "PASSWORD", "whdnd5725");
mysqli_set_charset($conn, "utf8");

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "DB 연결 실패"]);
    exit();
}

$isbn = $_GET['isbn'] ?? '';

$stmt = $conn->prepare("SELECT AVG(rating) AS avg_rating FROM REVIEW WHERE isbn = ?");
$stmt->bind_param("s", $isbn);
$stmt->execute();
$result = $stmt->get_result();
$row = $result->fetch_assoc();

$average = $row['avg_rating'] ?? 0;

echo json_encode(["success" => true, "average" => round((float)$average, 1)]);

$stmt->close();
$conn->close();
?>
