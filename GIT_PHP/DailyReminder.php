<?php
date_default_timezone_set("Asia/Seoul");

if (!isset($_GET['key']) || $_GET['key'] !== 'PASSWORD') {
    http_response_code(403);
    exit('⛔ 접근 거부: 올바른 key가 필요합니다.');
}

$SERVICE_ACCOUNT_FILE = __DIR__ . "/openbook-firebase-adminsdk.json";

function getAccessToken($serviceAccountFile) {
    $token = null;
    $now = time();
    $header = ['alg' => 'RS256', 'typ' => 'JWT'];
    $claims = [
        'iss' => $serviceAccountFile['client_email'],
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
        'aud' => 'https://oauth2.googleapis.com/token',
        'iat' => $now,
        'exp' => $now + 3600
    ];

    $jwtHeader = base64url_encode(json_encode($header));
    $jwtClaim = base64url_encode(json_encode($claims));
    $data = $jwtHeader . "." . $jwtClaim;
    openssl_sign($data, $signature, $serviceAccountFile['private_key'], 'sha256WithRSAEncryption');
    $jwt = $data . "." . base64url_encode($signature);

    $ch = curl_init("https://oauth2.googleapis.com/token");
    curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query([
        'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
        'assertion' => $jwt
    ]));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $res = curl_exec($ch);
    curl_close($ch);

    $resObj = json_decode($res, true);
    return $resObj['access_token'] ?? null;
}

function base64url_encode($data) {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}

// DB 연결
$conn = new mysqli('localhost', 'whdnd5725', 'PASSWORD', 'whdnd5725');
if ($conn->connect_error) die("DB 연결 실패");

$tomorrow = date('Y-m-d', strtotime('+1 day'));
$sql = "
    SELECT DISTINCT LOAN.userID, USER.userName, FCM_TOKEN.token
    FROM LOAN
    JOIN FCM_TOKEN ON LOAN.userID = FCM_TOKEN.userID
    JOIN USER ON LOAN.userID = USER.userID
    WHERE DATE(return_date) = ?
      AND returned = FALSE
";

$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $tomorrow);
$stmt->execute();
$result = $stmt->get_result();

$serviceAccountJson = json_decode(file_get_contents($SERVICE_ACCOUNT_FILE), true);
$accessToken = getAccessToken($serviceAccountJson);
$projectId = $serviceAccountJson['project_id'];
$fcmUrl = "https://fcm.googleapis.com/v1/projects/{$projectId}/messages:send";

while ($row = $result->fetch_assoc()) {
    $token = $row['token'];
    $userName = $row['userName'];

    $payload = [
        "message" => [
            "token" => $token,
            "notification" => [
                "title" => "📚 반납 하루 전 알림",
                "body" => "{$userName}님, 대출 도서 반납일이 내일입니다!"
            ]
        ]
    ];


    $headers = [
        "Authorization: Bearer $accessToken",
        "Content-Type: application/json"
    ];

    $ch = curl_init($fcmUrl);
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode($payload));
    curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    $response = curl_exec($ch);
    $status = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    curl_close($ch);

    echo "[FCM] {$userID} → HTTP {$status}: {$response}\n";
}

$stmt->close();
$conn->close();
?>
