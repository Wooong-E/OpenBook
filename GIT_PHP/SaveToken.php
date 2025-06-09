<?php
$conn = new mysqli('localhost', 'whdnd5725', 'PASSWORD', 'whdnd5725');
if ($conn->connect_error) {
    die("DB 연결 실패: " . $conn->connect_error);
}

$userID = $_POST['userID'] ?? '';
$token = $_POST['token'] ?? '';

if (empty($userID) || empty($token)) {
    echo json_encode(["success" => false, "message" => "Missing userID or token"]);
    exit;
}

$stmt = $conn->prepare("REPLACE INTO FCM_TOKEN (userID, token) VALUES (?, ?)");
$stmt->bind_param("ss", $userID, $token);

if ($stmt->execute()) {
    echo json_encode(["success" => true]);
} else {
    echo json_encode(["success" => false, "message" => "DB 저장 실패"]);
}

$stmt->close();
$conn->close();
?>
