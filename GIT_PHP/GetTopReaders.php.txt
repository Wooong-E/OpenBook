<?php
header('Content-Type: application/json');
$con = mysqli_connect("localhost", "whdnd5725", "PASSWORD", "whdnd5725");

if (!isset($_GET['userID'])) {
    echo json_encode(["success" => false, "message" => "userID 없음"]);
    exit;
}
$userID = $_GET['userID'];

// 대출 수 조회
$borrowResult = mysqli_query($con, "SELECT userTotalBorrow FROM USER WHERE userID = '$userID'");
if (!$borrowResult || mysqli_num_rows($borrowResult) === 0) {
    echo json_encode(["success" => false, "message" => "해당 userID 없음"]);
    exit;
}
$row = mysqli_fetch_assoc($borrowResult);
$userBorrowCount = $row['userTotalBorrow'];

// 상위 10명
$sql = "SELECT userName, userTotalBorrow FROM USER ORDER BY userTotalBorrow DESC LIMIT 10";
$result = mysqli_query($con, $sql);
$response = [];
while ($row = mysqli_fetch_assoc($result)) {
    $response[] = $row;
}

// 등수 계산
$rankSql = "SELECT COUNT(*) + 1 AS userRank FROM USER WHERE userTotalBorrow > $userBorrowCount";
$rankRes = mysqli_query($con, $rankSql);
$rankRow = mysqli_fetch_assoc($rankRes);
$rank = $rankRow['userRank'];

// 전체 사용자 수
$totalSql = "SELECT COUNT(*) AS total FROM USER";
$totalRes = mysqli_query($con, $totalSql);
$totalRow = mysqli_fetch_assoc($totalRes);
$total = $totalRow['total'];

$percent = round(($rank / $total) * 100, 2);

echo json_encode([
    "success" => true,
    "topUsers" => $response,
    "yourRank" => $rank,
    "yourPercent" => $percent
]);
?>
