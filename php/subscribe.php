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

$subscription = array();
$subscription['id'] = $data['id'];
$subscription['start_time'] = $data['start_time'];
$subscription['weeks'] = $data['weeks'];
$subscription['mon'] = $data['mon'];
if (!$subscription['mon']) {
    unset($subscription['mon']);
}
$subscription['tue'] = $data['tue'];
if (!$subscription['tue']) {
    unset($subscription['tue']);
}
$subscription['wed'] = $data['wed'];
if (!$subscription['wed']) {
    unset($subscription['wed']);
}
$subscription['thur'] = $data['thur'];
if (!$subscription['thur']) {
    unset($subscription['thur']);
}
$subscription['fri'] = $data['fri'];
if (!$subscription['fri']) {
    unset($subscription['fri']);
}

$start_day = (int)date("D", $subscription['start_time']);
$mon_time = 0;
$tue_time = 0;
$wed_time = 0;
$thur_time = 0;
$fri_time = 0;

for ($i = 0; $i < 7; $i++) {
    switch (($start_day + $i) % 7) {
    case 1:
        $mon_time = $subscription['start_time'] + 86400 * $i;
        break;
    case 2:
        $tue_time = $subscription['start_time'] + 86400 * $i;
        break;
    case 3:
        $wed_time = $subscription['start_time'] + 86400 * $i;
        break;
    case 4:
        $thur_time = $subscription['start_time'] + 86400 * $i;
        break;
    case 5:
        $fri_time = $subscription['start_time'] + 86400 * $i;
        break;
    default:
        break;
    }
}

$orders = array();
for ($i = 0; $i < $subscription['weeks']; $i++) {
    $array = array();
    $array['order_type'] = 3;
    $array['id'] = $subscription['id'];
    $array['addr'] = $subscription['addr'];
    $array['order_time'] = time();
    $array['status'] = 1;
    if ($subscription['mon'] != []) {
        $array['total_price'] = $subscription['mon']['total_price'];
        $array['order_items'] = $subscription['mon']['order_items'];
        $array['reservation_time'] = $mon_time;
        $mon_time = $mon_time + 86400 * 7;
        $orders[] = $array;
    }
    if ($subscription['tue'] != []) {
        $array['total_price'] = $subscription['tue']['total_price'];
        $array['order_items'] = $subscription['tue']['order_items'];
        $array['reservation_time'] = $tue_time;
        $tue_time = $tue_time + 86400 * 7;
        $orders[] = $array;
    }
    if ($subscription['wed'] != []) {
        $array['total_price'] = $subscription['wed']['total_price'];
        $array['order_items'] = $subscription['wed']['order_items'];
        $array['reservation_time'] = $wed_time;
        $wed_time = $wed_time + 86400 * 7;
        $orders[] = $array;
    }
    if ($subscription['thur'] != []) {
        $array['total_price'] = $subscription['thur']['total_price'];
        $array['order_items'] = $subscription['thur']['order_items'];
        $array['reservation_time'] = $thur_time;
        $thur_time = $thur_time + 86400 * 7;
        $orders[] = $array;
    }
    if ($subscription['fri'] != []) {
        $array['total_price'] = $subscription['fri']['total_price'];
        $array['order_items'] = $subscription['fri']['order_items'];
        $array['reservation_time'] = $fri_time;
        $fri_time = $fri_time + 86400 * 7;
        $orders[] = $array;
    }
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

$holidays = array();
$result = mysqli_query($db_conn, "select * from holidays");
if (!$result) {
    http_response_code(500);
    return;
}
while ($row = mysqli_fetch_array($result)) {
    $holidays[] = $row['date'];
}

$response = array();
$response['subscription'] = $subscription;
$response['orders'] = $orders;

print(json_encode($response, JSON_UNESCAPED_UNICODE));

mysqli_close($db_conn);
