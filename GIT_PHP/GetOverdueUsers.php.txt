<?php
$conn = mysqli_connect("localhost", "whdnd5725", "PASSWORD", "whdnd5725");
mysqli_set_charset($conn, "utf8");

$query = "
    SELECT USER.userID, userName, COUNT(*) as overdueCount
    FROM LOAN
    JOIN USER ON USER.userID = LOAN.userID
    WHERE returned = 0 AND return_date < NOW()
    GROUP BY LOAN.userID
";
$result = mysqli_query($conn, $query);

$data = array();
while($row = mysqli_fetch_assoc($result)) {
    $data[] = $row;
}

echo json_encode($data);
?>
