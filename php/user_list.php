<?php

require 'common.php';
use \Firebase\JWT\JWT;
use \Firebase\JWT\ExpiredException;

$method = $_SERVER['REQUEST_METHOD'];

if ($method == "OPTIONS") {
    return;
}

if ($method != "GET") {
    http_response_code(405); // Method Not Allowed
    return;
}

$phone = $_GET['phone'];
$jwt = $_SERVER['HTTP_JWT'];

if ($phone == null) {
    http_response_code(400);
    return;
}
if ($jwt == null) {
    http_response_code(401); // Unauthorized
    return;
}

try {
    $decoded = JWT::decode($jwt, $jwt_secret, array('HS256'));
    if ($decoded->id != 'saladgram') {
        http_response_code(401); // Unauthorized
        return;
    }
} catch (ExpiredException $e1) {
    http_response_code(440); // Login Timeout
    return;
} catch (Exception $e2) {
    print($e2->getMessage());
    http_response_code(401); // Unauthorized
    return;
}

$db_conn = mysqli_connect($db_host, $db_user, $db_password, $db_name);

if (mysqli_connect_errno($db_conn)) {
    http_response_code(500);
    return;
}

if (!$db_conn->set_charset("utf8")) {
    http_response_code(500);
    return;
}

$result = mysqli_query($db_conn, "select * from users where phone like '%$phone'");
if (!$result) {
    http_response_code(500);
} else {
    $user_list = array();
    while ($row = mysqli_fetch_array($result)) {
        $array = array();
        $array['id'] = $row['id'];
        $array['phone'] = $row['phone'];
        $array['name'] = $row['name'];
        $array['addr'] = $row['addr'];
        $array['reward'] = $row['reward'];
        $user_list[] = $array;
    }
    mysqli_free_result($result);
    print(json_encode($user_list, JSON_UNESCAPED_UNICODE));
}

mysqli_close($db_conn);

?>
