<?php
$conn = new mysqli('localhost', 'whdnd5725', 'PASSWORD', 'whdnd5725');
if ($conn->connect_error) {
    http_response_code(500);
    exit("DB 연결 실패");
}

$userID = $_POST['userID'] ?? '';
if (empty($userID)) {
    http_response_code(400);
    exit("userID 없음");
}

// 토큰 삭제
$stmt = $conn->prepare("DELETE FROM FCM_TOKEN WHERE userID = ?");
$stmt->bind_param("s", $userID);
$stmt->execute();
$stmt->close();
$conn->close();

echo "삭제 완료";
?>
