<?php
error_reporting(E_ALL & ~E_NOTICE);

require 'common.php';
use \Firebase\JWT\JWT;
use \Firebase\JWT\ExpiredException;

$method = $_SERVER['REQUEST_METHOD'];

if ($method == "OPTIONS") {
    return;
}

if ($method != "POST") {
    http_response_code(405); // Method Not Allowed
    return;
}

$jwt = $_SERVER['HTTP_JWT'];

$body = file_get_contents("php://input");
$data = json_decode($body, true);

if (!$data) {
    http_response_code(400); // Bad Request
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
    http_response_code(401); // Unauthorized
    return;
}

$id = $data['id'];
$paid = $data['paid'];
if ($id == null) {
    http_response_code(400);
    return;
}
if ($paid == null || ($paid != 50000 && $paid != 100000)) {
    http_response_code(400);
    return;
}
$reward = 0;
$description = "";
if ($paid == 50000) {
    $reward = 55000;
    $description = "충전(55,000)";
} else if ($paid = 100000) {
    $reward = 115000;
    $description = "충전(115,000)";
}
$order_time = time();

$db_conn = mysqli_connect($db_host, $db_user, $db_password, $db_name);
if (mysqli_connect_errno($db_conn)) {
    http_response_code(500);
    return;
}

if (!$db_conn->set_charset("utf8")) {
    http_response_code(500);
    return;
}

if (!$db_conn->autocommit(false)) {
    http_response_code(500);
    return;
}

$query = "update users set reward = reward + $reward where id = '$id'";
$result = mysqli_query($db_conn, $query);
if (!$result) {
    $array = array();
    $array['success'] = false;
    $array['message'] = mysqli_error($db_conn);
    print(json_encode($array));
    http_response_code(500);
    return;
}

$query = "insert into rewards values('$id', $order_time, NULL, ".Types::REWARD_PREPAY.", '$description', $reward)";
$result = mysqli_query($db_conn, $query);
if (!$result) {
    $array = array();
    $array['success'] = false;
    $array['message'] = mysqli_error($db_conn);
    print(json_encode($array));
    http_response_code(500);
    return;
}


if (!mysqli_commit($db_conn)) {
    $array = array();
    $array['success'] = false;
    $array['message'] = mysqli_error($db_conn);
    print(json_encode($array));
    http_response_code(500);
    return;
}

$array = array();
$array['success'] = true;
$array['message'] = "reward prepaid succesfully.";
$array['order_id'] = $order_id;
print(json_encode($array));

mysqli_close($db_conn);


