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

$subscription = array();
$subscription['id'] = $data['id'];
$subscription['addr'] = $data['addr'];
$subscription['start_time'] = $data['start_time'];
$subscription['weeks'] = $data['weeks'];
$subscription['mon'] = $data['mon'];
if (!$subscription['mon']) {
    unset($subscription['mon']);
} else {
    tag_items($subscription['mon']);
}
$subscription['tue'] = $data['tue'];
if (!$subscription['tue']) {
    unset($subscription['tue']);
} else {
    tag_items($subscription['tue']);
}
$subscription['wed'] = $data['wed'];
if (!$subscription['wed']) {
    unset($subscription['wed']);
} else {
    tag_items($subscription['wed']);
}
$subscription['thur'] = $data['thur'];
if (!$subscription['thur']) {
    unset($subscription['thur']);
} else {
    tag_items($subscription['thur']);
}
$subscription['fri'] = $data['fri'];
if (!$subscription['fri']) {
    unset($subscription['fri']);
} else {
    tag_items($subscription['fri']);
}

$start_day = (int)date("w", $subscription['start_time']);
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

$holidays = array();
$result = mysqli_query($db_conn, "select * from holidays");
if (!$result) {
    http_response_code(500);
    return;
}
while ($row = mysqli_fetch_array($result)) {
    $holidays[] = $row['date'];
}

$total_price = 0;
$orders = array();
for ($i = 0; $i < $subscription['weeks']; $i++) {
    $array = array();
    if ($subscription['mon']) {
        if (in_array(date("Y-m-d", $mon_time), $holidays)) {
            $array2 = array();
            $array2['reservation_time'] = $mon_time;
            $array2['holiday'] = 1;
            $orders[] = $array2;
            $mon_time = $mon_time + 86400 * 7;
        } else {
            $array['total_price'] = $subscription['mon']['total_price'];
            $total_price = $total_price + $array['total_price'];
            $array['order_items'] = $subscription['mon']['order_items'];
            $array['reservation_time'] = $mon_time;
            $mon_time = $mon_time + 86400 * 7;
            $orders[] = $array;
        }
    }
    if ($subscription['tue']) {
        if (in_array(date("Y-m-d", $tue_time), $holidays)) {
            $array2 = array();
            $array2['reservation_time'] = $tue_time;
            $array2['holiday'] = 1;
            $orders[] = $array2;
            $tue_time = $tue_time + 86400 * 7;
        } else {
            $array['total_price'] = $subscription['tue']['total_price'];
            $total_price = $total_price + $array['total_price'];
            $array['order_items'] = $subscription['tue']['order_items'];
            $array['reservation_time'] = $tue_time;
            $tue_time = $tue_time + 86400 * 7;
            $orders[] = $array;
        }
    }
    if ($subscription['wed']) {
        if (in_array(date("Y-m-d", $wed_time), $holidays)) {
            $array2 = array();
            $array2['reservation_time'] = $wed_time;
            $array2['holiday'] = 1;
            $orders[] = $array2;
            $wed_time = $wed_time + 86400 * 7;
        } else {
            $array['total_price'] = $subscription['wed']['total_price'];
            $total_price = $total_price + $array['total_price'];
            $array['order_items'] = $subscription['wed']['order_items'];
            $array['reservation_time'] = $wed_time;
            $wed_time = $wed_time + 86400 * 7;
            $orders[] = $array;
        }
    }
    if ($subscription['thur']) {
        if (in_array(date("Y-m-d", $thur_time), $holidays)) {
            $array2 = array();
            $array2['reservation_time'] = $thur_time;
            $array2['holiday'] = 1;
            $orders[] = $array2;
            $thur_time = $thur_time + 86400 * 7;
        } else {
            $array['total_price'] = $subscription['thur']['total_price'];
            $total_price = $total_price + $array['total_price'];
            $array['order_items'] = $subscription['thur']['order_items'];
            $array['reservation_time'] = $thur_time;
            $thur_time = $thur_time + 86400 * 7;
            $orders[] = $array;
        }
    }
    if ($subscription['fri']) {
        if (in_array(date("Y-m-d", $fri_time), $holidays)) {
            $array2 = array();
            $array2['reservation_time'] = $fri_time;
            $array2['holiday'] = 1;
            $orders[] = $array2;
            $fri_time = $fri_time + 86400 * 7;
        } else {
            $array['total_price'] = $subscription['fri']['total_price'];
            $total_price = $total_price + $array['total_price'];
            $array['order_items'] = $subscription['fri']['order_items'];
            $array['reservation_time'] = $fri_time;
            $fri_time = $fri_time + 86400 * 7;
            $orders[] = $array;
        }
    }
}
$subscription['total_price'] = $total_price;
$discount = 0;
if ($total_price < 20000) {
    $discount = 5;
} else if ($total_price < 50000) {
    $discount = 10;
} else if ($total_price < 100000) {
    $discount = 12;
} else {
    $discount = 15;
}
$subscription['discount'] = $discount;
$subscription['actual_price'] = $total_price * (100 - $discount) / 100;



$order_time = time();
$query = "insert into subscriptions values('".$subscription['id']."', NULL, $order_time, ".$subscription['start_time'].", ".$subscription['weeks'].", ";
if ($data['mon']) {
    $query = $query."'".json_encode($data['mon'], JSON_UNESCAPED_UNICODE)."', ";
} else {
    $query = $query."NULL, ";
}
if ($data['tue']) {
    $query = $query."'".json_encode($data['tue'], JSON_UNESCAPED_UNICODE)."', ";
} else {
    $query = $query."NULL, ";
}
if ($data['wed']) {
    $query = $query."'".json_encode($data['wed'], JSON_UNESCAPED_UNICODE)."', ";
} else {
    $query = $query."NULL, ";
}
if ($data['thur']) {
    $query = $query."'".json_encode($data['thur'], JSON_UNESCAPED_UNICODE)."', ";
} else {
    $query = $query."NULL, ";
}
if ($data['fri']) {
    $query = $query."'".json_encode($data['fri'], JSON_UNESCAPED_UNICODE)."', ";
} else {
    $query = $query."NULL, ";
}
$query = $query.$subscription['total_price'].", ".$subscription['discount'].", 0, ".$subscription['actual_price'].", 8, ".$subscription['actual_price'].")";

$result = mysqli_query($db_conn, $query);
if (!$result) {
    $array = array();
    $array['success'] = false;
    $array['message'] = mysqli_error($db_conn);
    print(json_encode($array));
    http_response_code(500);
    return;
}

$subscription_id = $db_conn->insert_id;
foreach ($orders as &$order) {
    if ($order['holiday'] == 1){
        continue;
    }
    $actual_price = $order['total_price'] * (100 - $discount) / 100;
    $query = "insert into orders values(NULL, $subscription_id, NULL, 3, '".$subscription['id']."', NULL, '".$subscription['addr']."', ";
    $query = $query.$order['total_price'].", $discount, 0, $actual_price, 8, $actual_price, $order_time, ".$order['reservation_time'].", ".Types::STATUS_TODO.")";

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
    foreach ($order['order_items'] as &$item) {
        $order_item_type = $item['order_item_type'];
        $item_id = $item['item_id'];
        $salad_items = json_encode($item['salad_items']);
        $amount_type = $item['amount_type'];
        $quantity = $item['quantity'];
        $price = $item['price'];
        $calorie = $item['calorie'];

        $query = "insert into order_items values($order_id, '".$subscription['id']."', $order_item_type, $item_id, ";
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

        $query = $query." $quantity, $price, $calorie, ".Types::PACKAGE_TAKE_OUT.")";

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
}


if (!mysqli_commit($db_conn)) {
    $array = array();
    $array['success'] = false;
    $array['message'] = mysqli_error($db_conn);
    print(json_encode($array));
    http_response_code(500);
    return;
}

$response = array();
$response['subscription'] = $subscription;
$response['orders'] = $orders;

print(json_encode($response, JSON_UNESCAPED_UNICODE));

mysqli_close($db_conn);

function tag_items(&$array) {
    global $salads, $salad_items, $soups, $others, $beverages;
    foreach($array['order_items'] as &$order_item) {
        if ($order_item['order_item_type'] == Types::ORDER_ITEM_SALAD) {
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
        } else if ($order_item['order_item_type'] == Types::ORDER_ITEM_SOUP) {
            $order_item['name'] = $soups[$order_item['item_id']]['name'];
            $amount_type = 'amount'.$order_item['amount_type'];
            $order_item['amount'] = $soups[$order_item['item_id']][$amount_type].$soups[$order_item['item_id']]['unit'];
        } else if ($order_item['order_item_type'] == Types::ORDER_ITEM_OTHER) {
            $order_item['name'] = $others[$order_item['item_id']]['name'];
        } else if ($order_item['order_item_type'] == Types::ORDER_ITEM_BEVERAGE) {
            $order_item['name'] = $beverages[$order_item['item_id']]['name'];
        }
    }
}

?>
