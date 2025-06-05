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

$userID = isset($_GET['userID']) ? $_GET['userID'] : '';

$sql = "SELECT B.title, B.author, B.isbn, L.loan_date, L.return_date 
        FROM LOAN L
        JOIN BOOK B ON L.isbn = B.isbn
        WHERE L.userID = ? AND L.returned = false";

$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $userID);
$stmt->execute();
$result = $stmt->get_result();

$loans = [];
while ($row = $result->fetch_assoc()) {
    $loans[] = [
        "title" => $row["title"],
        "author" => $row["author"],
        "isbn" => $row["isbn"],
        "loan_date" => $row["loan_date"],
        "return_date" => $row["return_date"]
    ];
}

echo json_encode(["success" => true, "loans" => $loans]);

$stmt->close();
$conn->close();
?>
