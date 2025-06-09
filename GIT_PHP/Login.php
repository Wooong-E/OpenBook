<?php
$con = mysqli_connect("localhost", "whdnd5725", "PASSWORD", "whdnd5725");
mysqli_set_charset($con, "utf8");

$userID = $_POST["userID"] ?? "";
$userPassword = $_POST["userPassword"] ?? "";

$response = ["success" => false];

$sql = "SELECT userName, userType FROM USER WHERE userID = ? AND userPassword = ?";
$stmt = mysqli_prepare($con, $sql);
mysqli_stmt_bind_param($stmt, "ss", $userID, $userPassword);
mysqli_stmt_execute($stmt);
mysqli_stmt_store_result($stmt);

if (mysqli_stmt_num_rows($stmt) > 0) {
    mysqli_stmt_bind_result($stmt, $userName, $userType);
    mysqli_stmt_fetch($stmt);
    
    $response["success"] = true;
    $response["userID"] = $userID;
    $response["userName"] = $userName;
    $response["userType"] = $userType;  // 추가
}

echo json_encode($response);
?>
