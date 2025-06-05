<?php
$conn = mysqli_connect("localhost", "whdnd5725", "PASSWORD", "whdnd5725");
mysqli_set_charset($conn, "utf8");

$result = mysqli_query($conn, "SELECT * FROM LOAN ORDER BY loan_date DESC");

$data = array();
while ($row = mysqli_fetch_assoc($result)) {
    $data[] = $row;
}
echo json_encode($data);
?>
