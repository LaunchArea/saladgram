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

$id = $_GET['id'];
$jwt = $_SERVER['HTTP_JWT'];

if ($id == null) {
    http_response_code(400);
    return;
}
if ($jwt == null) {
    http_response_code(401); // Unauthorized
    return;
}

try {
    $decoded = JWT::decode($jwt, $jwt_secret, array('HS256'));
    if ($id != $decoded->id) {
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

$salad_items = array();
$result = mysqli_query($db_conn, "select * from salad_items");
if (!$result) {
    http_response_code(500);
    return;
} else if (mysqli_num_rows($result) != 0) {
    while ($row = mysqli_fetch_array($result)) {
        $array = array();
        $array['item_id'] = (int)$row['item_id'];
        $array['salad_item_type'] = (int)$row['salad_item_type'];
        $array['name'] = $row['name'];
        $array['description'] = $row['description'];
        $array['image'] = $row['image'];
        $array['amount1'] = (int)$row['amount1'];
        $array['amount2'] = (int)$row['amount2'];
        $array['amount3'] = (int)$row['amount3'];
        $array['amount4'] = (int)$row['amount4'];
        $array['unit'] = $row['unit'];
        $array['calorie'] = (int)$row['calorie'];
        $array['price'] = (int)$row['price'];
        $array['available'] = (int)$row['available'];
        $array['hide'] = (int)$row['hide'];
        $salad_items[] = $array;
    }
    mysqli_free_result($result);
} else {
    http_response_code(500);
    return;
}

$query = "select * from orders as a join order_items as b on a.order_id = b.order_id ";
if ($id == "saladgram") {
    $query= $query."";
} else {
    $query = $query."where a.id = '$id' and a.order_type != 3 order by a.order_id desc";
}

$orders = array();
$result = mysqli_query($db_conn, $query);
if (!$result) {
    http_response_code(500);
} else {
    $order_id = -1;
    $order = NULL;
    while ($row = mysqli_fetch_array($result)) {
        if ($order_id != (int)$row['order_id']) {
            if ($order_id != -1) {
                $orders[] = $order;
            }
            $order_id = (int)$row['order_id'];
            $order = array();
            $order['order_id'] = (int)$row['order_id'];
            $order['order_type'] = (int)$row['order_type'];
            if ($row['id']) {
                $order['id'] = $row['id'];
            } else {
                $order['phone'] = $row['phone'];
            }
            if ($row['addr']) {
                $order['addr'] = $row['addr'];
            }
            $order['total_price'] = (int)$row['total_price'];
            $order['discount'] = (int)$row['discount'];
            $order['reward_use'] = (int)$row['reward_use'];
            $order['actual_price'] = (int)$row['actual_price'];
            $order['payment_type'] = (int)$row['payment_type'];
            $order['paid'] = (int)$row['paid'];
            $order['order_time'] = (int)$row['order_time'];
            $order['reservation_time'] = (int)$row['reservation_time'];
            $order['status'] = (int)$row['status'];
        }
        $array = array();
        $array['order_item_type'] = (int)$row['order_item_type'];
        $array['item_id'] = (int)$row['item_id'];
        if ($array['order_item_type'] == 1) {
            $items = json_decode($row['salad_items'], true);
            foreach ($items as &$item) {
                $item['name'] = $salad_items[(int)$item['item_id']]['name'];
                $amount_type = 'amount'.$item['amount_type'];
                $item['amount'] = $salad_items[(int)$item['item_id']][$amount_type].$salad_items[(int)$item['item_id']]['unit'];
                $item['salad_item_type'] = $salad_items[(int)$item['item_id']]['salad_item_type'];
            }
            $array['salad_items'] = $items;
        }
        $array['quantity'] = (int)$row['quantity'];
        $array['price'] = (int)$row['price'];
        $array['calorie'] = (int)$row['calorie'];

        $order['order_items'][] = $array;
    }
    if ($order) {
        $orders[] = $order;
    }
}

print(json_encode($orders, JSON_UNESCAPED_UNICODE));

mysqli_close($db_conn);

