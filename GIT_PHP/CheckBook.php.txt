<?php
header("Content-Type: application/json; charset=UTF-8");
$conn = new mysqli("localhost", "whdnd5725", "PASSWORD", "whdnd5725");
mysqli_set_charset($conn, "utf8");

$isbn = $_POST['isbn'];
$stmt = $conn->prepare("SELECT COUNT(*) AS cnt FROM BOOK WHERE isbn = ?");
$stmt->bind_param("s", $isbn);
$stmt->execute();
$result = $stmt->get_result();
$row = $result->fetch_assoc();

if ($row["cnt"] > 0) {
    echo json_encode(["exists" => true]);
} else {
    echo json_encode(["exists" => false]);
}
$stmt->close();
$conn->close();
?>
