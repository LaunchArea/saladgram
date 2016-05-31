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

$menu_list = menu_list($db_conn);
if ($menu_list == []) {
    http_response_code(500);
    return;
}
$salads = $menu_list['salads'];
$salad_items = $menu_list['salad_items'];
$others = $menu_list['others'];
$soups = $menu_list['soups'];
$beverages = $menu_list['beverages'];

$query = "select *, a.total_price as atotal_price, a.discount as adiscount, a.reward_use as areward_use, a.actual_price as aactual_price, a.paid as apaid ";
$query = $query."from subscriptions as a join orders as b on a.subscription_id = b.subscription_id ";
$query = $query."where a.id = '$id' order by a.subscription_id desc, b.reservation_time desc";

$result = mysqli_query($db_conn, $query);
if (!$result) {
    http_response_code(500);
    return;
}

$subscriptions = array();
$subscription_id = -1;
$subscription = NULL;
while ($row = mysqli_fetch_array($result)) {
    if ($subscription_id != (int)$row['subscription_id']) {
        if ($subscription_id != -1) {
            $subscriptions[] = $subscription;
        }
        $subscription_id = (int)$row['subscription_id'];
        $subscription = array();
        $subscription['id'] = $row['id'];
        $subscription['subscription_id'] = $subscription_id;
        $subscription['order_time'] = (int)$row['order_time'];
        $subscription['start_time'] = (int)$row['start_time'];
        $subscription['weeks'] = (int)$row['weeks'];
        if ($row['mon']) {
            $meal = json_decode($row['mon'], true);
            tag_items($meal);
            $subscription['mon'] = $meal;
        }
        if ($row['tue']) {
            $meal = json_decode($row['tue'], true);
            tag_items($meal);
            $subscription['tue'] = $meal;
        }
        if ($row['wed']) {
            $meal = json_decode($row['wed'], true);
            tag_items($meal);
            $subscription['wed'] = $meal;
        }
        if ($row['thur']) {
            $meal = json_decode($row['thur'], true);
            tag_items($meal);
            $subscription['thur'] = $meal;
        }
        if ($row['fri']) {
            $meal = json_decode($row['fri'], true);
            tag_items($meal);
            $subscription['fri'] = $meal;
        }
        $subscription['total_price'] = (int)$row['atotal_price'];
        $subscription['discount'] = (int)$row['adiscount'];
        $subscription['reward_use'] = (int)$row['areward_use'];
        $subscription['actual_price'] = (int)$row['aactual_price'];
        $subscription['payment_type'] = (int)$row['apayment_type'];
        $subscription['paid'] = (int)$row['apaid'];
    }
    $array = array();
    $array['order_id'] = (int)$row['order_id'];
    $array['order_type'] = (int)$row['order_type'];
    $array['id'] = $row['id'];
    $array['addr'] = $row['addr'];
    $array['total_price'] = (int)$row['total_price'];
    $array['discount'] = (int)$row['discount'];
    $array['reward_use'] = (int)$row['reward_use'];
    $array['actual_price'] = (int)$row['actual_price'];
    $array['payment_type'] = (int)$row['payment_type'];
    $array['paid'] = (int)$row['paid'];
    $array['order_time'] = (int)$row['order_time'];
    $array['reservation_time'] = (int)$row['reservation_time'];
    $array['status'] = (int)$row['status'];
    switch((int)date("w", $array['reservation_time'])) {
    case 1:
        $array['order_items'] = $subscription['mon']['order_items'];
        break;
    case 2:
        $array['order_items'] = $subscription['tue']['order_items'];
        break;
    case 3:
        $array['order_items'] = $subscription['wed']['order_items'];
        break;
    case 4:
        $array['order_items'] = $subscription['thur']['order_items'];
        break;
    case 5:
        $array['order_items'] = $subscription['fri']['order_items'];
        break;
    default:
        break;
    }

    $subscription['orders'][] = $array;
}
if ($subscription) {
    $subscriptions[] = $subscription;
}
print(json_encode($subscriptions, JSON_UNESCAPED_UNICODE));

mysqli_close($db_conn);

function tag_items(&$array) {
    global $salads, $salad_items, $soups, $others, $beverages;
    foreach($array['order_items'] as &$order_item) {
        if ($order_item['order_item_type'] == 1) {
            $order_item['name'] = $salads[$order_item['item_id']]['name'];
            foreach ($order_item['salad_items'] as &$item) {
                $item['name'] = $salad_items[(int)$item['item_id']]['name'];
                $amount_type = 'amount'.$item['amount_type'];
                $item['amount'] = $salad_items[(int)$item['item_id']][$amount_type];
                $item['salad_item_type'] = $salad_items[(int)$item['item_id']]['salad_item_type'];
                $item['image'] = $salad_items[(int)$item['item_id']]['image'];
                $item['unit'] = $salad_items[(int)$item['item_id']]['unit'];
                $item['price'] = $salad_items[(int)$item['item_id']]['price'];
                $item['calorie'] = $salad_items[(int)$item['item_id']]['calorie'];
            }
        } else if ($order_item['order_item_type'] == 2) {
            $order_item['name'] = $soups[$order_item['item_id']]['name'];
            $amount_type = 'amount'.$order_item['amount_type'];
            $order_item['amount'] = $soups[$order_item['item_id']][$amount_type].$soups[$order_item['item_id']]['unit'];
        } else if ($order_item['order_item_type'] == 3) {
            $order_item['name'] = $others[$order_item['item_id']]['name'];
        } else if ($order_item['order_item_type'] == 4) {
            $order_item['name'] = $beverages[$order_item['item_id']]['name'];
        }
    }
}

?>
