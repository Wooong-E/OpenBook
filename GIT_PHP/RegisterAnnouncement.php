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

$content = isset($_POST['content']) ? trim($_POST['content']) : '';

if ($content === '') {
    echo json_encode(["success" => false, "message" => "공지내용이 비어있습니다."]);
    exit();
}

// UPDATE 시도
$stmt = $conn->prepare("UPDATE ANNOUNCEMENT SET content = ? WHERE id = 1");
$stmt->bind_param("s", $content);
$stmt->execute();

if ($conn->affected_rows > 0) {
    echo json_encode(["success" => true, "message" => "공지사항 수정 완료"]);
} else {
    // 행이 없으면 INSERT 시도
    $stmt = $conn->prepare("INSERT INTO ANNOUNCEMENT (id, content) VALUES (1, ?)");
    $stmt->bind_param("s", $content);
    if ($stmt->execute()) {
        echo json_encode(["success" => true, "message" => "공지사항 새로 등록됨"]);
    } else {
        echo json_encode(["success" => false, "message" => "등록 실패: " . $conn->error]);
    }
}

$stmt->close();
$conn->close();
?>
