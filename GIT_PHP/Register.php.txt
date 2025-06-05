<?php 
    $con = mysqli_connect("localhost", "whdnd5725", "PASSWORD", "whdnd5725");
    mysqli_query($con, 'SET NAMES utf8');

    $userID = isset($_POST["userID"]) ? $_POST["userID"] : "";
    $userPassword = isset($_POST["userPassword"]) ? $_POST["userPassword"] : "";
    $userName = isset($_POST["userName"]) ? $_POST["userName"] : "";

    $checkStmt = mysqli_prepare($con, "SELECT userID FROM USER WHERE userID = ?");
    mysqli_stmt_bind_param($checkStmt, "s", $userID);
    mysqli_stmt_execute($checkStmt);
    mysqli_stmt_store_result($checkStmt);

    $response = array();

    if (mysqli_stmt_num_rows($checkStmt) > 0) {
        $response["success"] = false;
        $response["reason"] = "duplicate";
    } else {
        $insertStmt = mysqli_prepare($con, "INSERT INTO USER (userID, userPassword, userName) VALUES (?, ?, ?)");
        mysqli_stmt_bind_param($insertStmt, "sss", $userID, $userPassword, $userName);
        $success = mysqli_stmt_execute($insertStmt);

        if ($success) {
            $response["success"] = true;
        } else {
            $response["success"] = false;
            $response["reason"] = "insert_error";
        }
    }

    echo json_encode($response);
?>
