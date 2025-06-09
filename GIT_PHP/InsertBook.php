<?php
header("Content-Type: application/json; charset=UTF-8");

$conn = new mysqli("localhost", "whdnd5725", "PASSWORD", "whdnd5725");
mysqli_set_charset($conn, "utf8");

if ($conn->connect_error) {
    echo json_encode(["success" => false, "message" => "DB 연결 실패"]);
    exit();
}

$title = trim($_POST['title']);
$author = trim($_POST['author']);
$isbn = trim($_POST['isbn']);
$krc = trim($_POST['krc']);

if (strlen($isbn) !== 13) {
    echo json_encode(["success" => false, "message" => "ISBN은 13자리여야 합니다."]);
    exit();
}

$check_sql = "SELECT COUNT(*) AS cnt FROM BOOK WHERE isbn = ?";
$check_stmt = $conn->prepare($check_sql);
$check_stmt->bind_param("s", $isbn);
$check_stmt->execute();
$check_result = $check_stmt->get_result();
$check_row = $check_result->fetch_assoc();
$check_stmt->close();

if ($check_row['cnt'] > 0) {
    $update_sql = "UPDATE BOOK SET totalCount = totalCount + 1, availableCount = availableCount + 1 WHERE isbn = ?";
    $update_stmt = $conn->prepare($update_sql);
    $update_stmt->bind_param("s", $isbn);

    if ($update_stmt->execute()) {
        echo json_encode(["success" => true, "message" => "기존 도서 수량이 증가되었습니다."]);
    } else {
        echo json_encode(["success" => false, "message" => "도서 수량 증가 실패: " . $conn->error]);
    }

    $update_stmt->close();
} else {
    $insert_sql = "INSERT INTO BOOK (title, author, isbn, krc, totalCount, availableCount) VALUES (?, ?, ?, ?, 1, 1)";
    $insert_stmt = $conn->prepare($insert_sql);
    $insert_stmt->bind_param("ssss", $title, $author, $isbn, $krc);

    if ($insert_stmt->execute()) {
        echo json_encode(["success" => true, "message" => "도서가 새로 등록되었습니다."]);
    } else {
        echo json_encode(["success" => false, "message" => "도서 등록 실패: " . $conn->error]);
    }

    $insert_stmt->close();
}

$conn->close();
?>
