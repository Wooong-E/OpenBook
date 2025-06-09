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

$userID = $_POST['userID'] ?? '';
$isbn = $_POST['isbn'] ?? '';

if (empty($userID) || empty($isbn)) {
    echo json_encode(["success" => false, "message" => "필수 정보 누락"]);
    exit();
}

$sql = "SELECT id FROM LOAN WHERE userID = ? AND isbn = ? AND returned = FALSE";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ss", $userID, $isbn);
$stmt->execute();
$result = $stmt->get_result();

if ($row = $result->fetch_assoc()) {
    $loanId = $row['id'];

    // 해당 loan 반납 처리
    $updateLoan = $conn->prepare("UPDATE LOAN SET returned = TRUE, return_date = NOW() WHERE id = ?");
    $updateLoan->bind_param("i", $loanId);
    $updateLoan->execute();

    if ($updateLoan->affected_rows > 0) {
        $updateBook = $conn->prepare("UPDATE BOOK SET availableCount = availableCount + 1 WHERE isbn = ?");
        $updateBook->bind_param("s", $isbn);
        $updateBook->execute();
        $updateBook->close();

        echo json_encode(["success" => true, "message" => "도서 반납 완료"]);
    } else {
        echo json_encode(["success" => false, "message" => "반납 처리 실패"]);
    }

    $updateLoan->close();
} else {
    echo json_encode(["success" => false, "message" => "반납할 도서를 찾을 수 없습니다"]);
}

$stmt->close();
$conn->close();
?>
