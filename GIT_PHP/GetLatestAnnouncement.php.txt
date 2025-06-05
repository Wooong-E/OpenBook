<?php
header("Content-Type: application/json; charset=UTF-8");

$host = 'localhost';
$user = 'whdnd5725';
$pass = 'PASSWORD';
$db = 'whdnd5725';

$conn = new mysqli($host, $user, $pass, $db);
mysqli_set_charset($conn, "utf8");

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "DB 연결 실패"]);
    exit();
}

$result = $conn->query("SELECT content FROM ANNOUNCEMENT WHERE id = 1");

if ($row = $result->fetch_assoc()) {
    echo json_encode(["success" => true, "content" => $row['content']]);
} else {
    echo json_encode(["success" => false, "message" => "공지사항 없음"]);
}

$conn->close();
?>
