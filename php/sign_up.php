<?php

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

if (!array_key_exists('id', $data) ||
    !array_key_exists('phone', $data) ||
    !array_key_exists('password', $data) ||
    !array_key_exists('name', $data) ||
    !array_key_exists('addr', $data)) {
    http_response_code(400); // Bad Request
    return;
}


$id = $data['id'];
$phone = $data['phone'];
$password = $data['password'];
$name = $data['name'];
$addr = $data['addr'];

if ($jwt == null) {
    http_response_code(401); // Unauthorized
    return;
}
try {
    $decoded = JWT::decode($jwt, $jwt_secret, array('HS256'));
    if ($phone != $decoded->phone) {
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

$time = time();
$query = "insert into rewards values('$id', $time, NULL, ".Types::REWARD_EVENT.", 'Open Event 적립금', 3000)";
$result = mysqli_query($db_conn, "$query");
if (!$result) {
    $array = array();
    $array['success'] = false;
    $array['message'] = mysqli_error($db_conn);
    print(json_encode($array));
    http_response_code(500);
    return;
}

$hash = password_hash($password, PASSWORD_DEFAULT);
$query = "insert into users values('$id', '$phone', '$hash', '$name', '$addr', 3000, $time)";
$result = mysqli_query($db_conn, "$query");
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
print(json_encode($array));

mysqli_close($db_conn);

?>
