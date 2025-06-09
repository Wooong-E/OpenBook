<?php
header("Content-Type: application/json; charset=UTF-8");

$conn = new mysqli("localhost", "whdnd5725", "PASSWORD", "whdnd5725");
mysqli_set_charset($conn, "utf8");

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "DB 연결 실패"]);
    exit();
}

$isbn = trim($_POST['isbn']);
$userID = trim($_POST['userID']);
$content = trim($_POST['content']);
$rating = intval($_POST['rating']);

if (strlen($content) < 10) {
    echo json_encode(["success" => false, "message" => "리뷰는 10자 이상이어야 합니다."]);
    exit();
}

if ($rating < 0 || $rating > 5) {
    echo json_encode(["success" => false, "message" => "평점은 0~5점 사이여야 합니다."]);
    exit();
}

$sql = "INSERT INTO REVIEW (isbn, userID, content, rating) VALUES (?, ?, ?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("sssi", $isbn, $userID, $content, $rating);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "리뷰가 성공적으로 등록되었습니다."]);
} else {
    echo json_encode(["success" => false, "message" => "리뷰 등록 실패: " . $conn->error]);
}

$stmt->close();
$conn->close();
?>
