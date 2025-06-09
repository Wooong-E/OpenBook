<?php
$userID = $_POST['userID'];
$isbn = $_POST['isbn'];

$conn = mysqli_connect("localhost", "whdnd5725", "PASSWORD", "whdnd5725");

// 대출 권수 확인
$result = mysqli_query($conn, "SELECT COUNT(*) AS cnt FROM LOAN WHERE userID='$userID' AND returned=FALSE");
$row = mysqli_fetch_assoc($result);
if ($row['cnt'] >= 5) {
    echo json_encode(["success"=>false, "message"=>"대출 가능 권수(5권)를 초과했습니다."]);
    exit;
}

// 같은 도서를 이미 대출 중인지 확인
$dupCheck = mysqli_query($conn, "SELECT COUNT(*) AS cnt FROM LOAN WHERE userID='$userID' AND isbn='$isbn' AND returned=FALSE");
$dupRow = mysqli_fetch_assoc($dupCheck);
if ($dupRow['cnt'] > 0) {
    echo json_encode(["success"=>false, "message"=>"이미 대출 중인 도서입니다."]);
    exit;
}

$book = mysqli_query($conn, "SELECT availableCount FROM BOOK WHERE isbn='$isbn'");
$b = mysqli_fetch_assoc($book);
if ($b['availableCount'] <= 0) {
    echo json_encode(["success"=>false, "message"=>"대출 가능한 수량이 없습니다."]);
    exit;
}

mysqli_query($conn, "INSERT INTO LOAN (userID, isbn, loan_date, return_date, returned) 
VALUES ('$userID', '$isbn', NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY), FALSE)");
mysqli_query($conn, "UPDATE BOOK SET availableCount = availableCount - 1 WHERE isbn='$isbn'");

mysqli_query($conn, "UPDATE USER SET userTotalBorrow = userTotalBorrow + 1 WHERE userID='$userID'"); // 총 대출 권수증가.

echo json_encode(["success"=>true, "message"=>"대출이 완료되었습니다."]);
?>
