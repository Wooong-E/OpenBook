<?php
$isbn = $_GET['isbn'] ?? '';

$conn = mysqli_connect("localhost", "whdnd5725", "PASSWORD", "whdnd5725");
$sql = "SELECT title, author, totalCount, availableCount FROM BOOK WHERE isbn = '$isbn'";
$result = mysqli_query($conn, $sql);

if ($row = mysqli_fetch_assoc($result)) {
    echo json_encode([
        "success" => true,
        "title" => $row['title'],
        "author" => $row['author'],
        "totalCount" => (int)$row['totalCount'],
        "availableCount" => (int)$row['availableCount']
    ]);
} else {
    echo json_encode(["success" => false]);
}
?>
