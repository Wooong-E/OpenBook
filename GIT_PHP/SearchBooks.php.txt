<?php
$type = $_GET['type'] ?? '';
$keyword = $_GET['keyword'] ?? '';

$conn = mysqli_connect("localhost", "whdnd5725", "PASSWORD", "whdnd5725");

$type_whitelist = ['title', 'author', 'isbn']; 
if (!in_array($type, $type_whitelist)) {
    echo json_encode([]);
    exit;
}

$sql = "SELECT title, author, isbn FROM BOOK WHERE $type LIKE '%$keyword%'";
$result = mysqli_query($conn, $sql);

$books = array();
while ($row = mysqli_fetch_assoc($result)) {
    $books[] = [
        "title" => $row["title"],
        "author" => $row["author"],
        "isbn" => $row["isbn"]
    ];
}

echo json_encode($books);
?>
