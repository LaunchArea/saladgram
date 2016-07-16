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

if (array_key_exists('id', $data)) {
    if ($jwt == null) {
        http_response_code(401); // Unauthorized
        return;
    }
    try {
        $decoded = JWT::decode($jwt, $jwt_secret, array('HS256'));
        if ($data['id'] != $decoded->id && $decoded->id != 'saladgram') {
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
} else if (array_key_exists('phone', $data)) {
    if ($jwt == null) {
        http_response_code(401); // Unauthorized
        return;
    }
    try {
        $decoded = JWT::decode($jwt, $jwt_secret, array('HS256'));
        if ($data['phone'] != $decoded->phone) {
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

$order_type = $data['order_type'];
$id = $data['id'];
$phone = $data['phone'];
$addr = $data['addr'];
$total_price = $data['total_price'];
$discount = $data['discount'];
$reward_use = $data['reward_use'];
$actual_price = $data['actual_price'];
$payment_type = $data['payment_type'];
$order_time = $data['order_time'];
if (!$order_time) {
    $order_time = time();
}
$reservation_time = $data['reservation_time'];
if (!$reservation_time) {
    $reservation_time = $order_time;
}

$query = "insert into orders values(NULL, NULL, NULL, $order_type, ";
if ($id) {
    $query = $query."'$id', ";
} else {
    $query = $query."NULL, ";
}

$query = $query."'$phone', ";

if ($addr) {
    $query = $query."'$addr', ";
} else {
    $query = $query."NULL, ";
}

$paid = 0;
if ($payment_type == Types::PAYMENT_CARD || $payment_type == Types::PAYMENT_CASH || $payment_type == Types::PAYMENT_CASH_RECEIPT) {
    $paid = ($actual_price / 100) * 100;
}
// TODO : inipay

$query = $query."$total_price, $discount, $reward_use, $actual_price, $payment_type, $paid, $order_time, $reservation_time, ".Types::STATUS_TODO.")";

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

$result = mysqli_query($db_conn, $query);
if (!$result) {
    $array = array();
    $array['success'] = false;
    $array['message'] = mysqli_error($db_conn);
    print(json_encode($array));
    http_response_code(500);
    return;
}

$order_id = $db_conn->insert_id;
$order_items = $data['order_items'];
foreach ($order_items as &$item) {
    $order_item_type = $item['order_item_type'];
    $item_id = $item['item_id'];
    $salad_items = json_encode($item['salad_items']);
    $amount_type = $item['amount_type'];
    $quantity = $item['quantity'];
    $price = $item['price'];
    $calorie = $item['calorie'];
    $package_type = Types::PACKAGE_TAKE_OUT;
    if ($order_type == Types::ORDER_DINE_IN) {
        $package_type = Types::PACKAGE_DINE_IN;
    }
    $package_type = $item['package_type'] ? $item['package_type'] : $package_type;

    $query = "insert into order_items values($order_id, '$id', $order_item_type, $item_id, ";
    if ($order_item_type == Types::ORDER_ITEM_SALAD) {
        $query = $query."'$salad_items', ";
    } else {
        $query = $query."NULL, ";
    }
    if ($order_item_type == Types::ORDER_ITEM_SOUP) {
        $query = $query."$amount_type, ";
    } else {
        $query = $query."NULL, ";
    }

    $query = $query." $quantity, $price, $calorie, $package_type)";
    $result = mysqli_query($db_conn, $query);
    if (!$result) {
        $array = array();
        $array['success'] = false;
        $array['message'] = mysqli_error($db_conn);
        print(json_encode($array));
        http_response_code(500);
        return;
    }
}

if ($reward_use != 0) {
    $reward_query = "insert into rewards values('$id', $order_time, $order_id, 2, '적립금 사용', -$reward_use)";
    $result = mysqli_query($db_conn, $reward_query);
    if (!$result) {
        $array = array();
        $array['success'] = false;
        $array['message'] = mysqli_error($db_conn);
        print(json_encode($array));
        http_response_code(500);
        return;
    }
    $reward_query = "update users set reward = reward - $reward_use where id = '$id'";
    $result = mysqli_query($db_conn, $reward_query);
    if (!$result) {
        $array = array();
        $array['success'] = false;
        $array['message'] = mysqli_error($db_conn);
        print(json_encode($array));
        http_response_code(500);
        return;
    }
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
$array['message'] = "Order placed succesfully.";
$array['order_id'] = $order_id;
print(json_encode($array));

mysqli_close($db_conn);


