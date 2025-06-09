<?php
header("Content-Type: application/json; charset=UTF-8");
$conn = new mysqli("localhost", "whdnd5725", "PASSWORD", "whdnd5725");
mysqli_set_charset($conn, "utf8");

$isbn = $_GET['isbn'];
$order = $_GET['order']; // "latest" or "rating"

$orderBy = ($order === 'rating') ? 'rating DESC, created_at DESC' : 'created_at DESC';

$stmt = $conn->prepare("SELECT userID, content, rating, created_at FROM REVIEW WHERE isbn = ? ORDER BY $orderBy");
$stmt->bind_param("s", $isbn);
$stmt->execute();
$result = $stmt->get_result();

$reviews = [];
while ($row = $result->fetch_assoc()) {
    $reviews[] = $row;
}

echo json_encode(["success" => true, "reviews" => $reviews]);
?>
