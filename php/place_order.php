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

if (array_key_exists('id', $data)) {
    if ($jwt == null) {
        http_response_code(401); // Unauthorized
        return;
    }
    try {
        $decoded = JWT::decode($jwt, $jwt_secret, array('HS256'));
        if ($data['id'] != $decoded->id) {
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
} else {
    http_response_code(400); // Bad Request
    return;
}

$type = $data['type'];
$id = $data['id'];
$phone = $data['phone'];
$addr = $data['addr'];
$total_price = $data['total_price'];
$discount = $data['discount'];
$reward_use = $data['reward_use'];
$actual_price = $data['actual_price'];
$payment_type = $data['payment_type'];
$order_time = $data['order_time'];
$reservation_time = $data['reservation_time'];

$query = "insert into orders values(NULL, NULL, NULL, $type, ";
if ($id) {
    $query = $query."'$id', NULL, ";
} else {
    $query = $query."NULL, '$phone', ";
}
$query = $query."'$addr', $total_price, $discount, $reward_use, $actual_price, $payment_type, 0, $order_time, $reservation_time, 1)";

$db_conn = mysqli_connect($db_host, $db_user, $db_password, $db_name);
if (mysqli_connect_errno($db_conn)) {
    http_response_code(500);
    return;
}

if (!$db_conn->set_charset("utf8")) {
    http_response_code(500);
    return;
}

$result = mysqli_query($db_conn, "$query");
if (!$result) {
    $array = array();
    $array['success'] = false;
    $array['message'] = mysqli_error($db_conn);
    print(json_encode($array));
    http_response_code(500);
} else {
    $array = array();
    $array['success'] = true;
    $array['message'] = "Order placed succesfully.";
    print(json_encode($array));
}

mysqli_close($db_conn);


