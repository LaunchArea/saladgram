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
            $subscription['mon'] = json_decode($row['mon'], true);
        }
        if ($row['tue']) {
            $subscription['tue'] = json_decode($row['tue'], true);
        }
        if ($row['wed']) {
            $subscription['wed'] = json_decode($row['wed'], true);
        }
        if ($row['thur']) {
            $subscription['thur'] = json_decode($row['thur'], true);
        }
        if ($row['fri']) {
            $subscription['fri'] = json_decode($row['fri'], true);
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

?>
