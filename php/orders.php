<?php
error_reporting(E_ALL & ~E_NOTICE);

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

$query = "select * from orders as a join order_items as b on a.order_id = b.order_id ";
if ($id == "saladgram") {
    $where = 0;
    if ($_GET['deliverer_id']) {
        $query = $query."where deliverer_id = '".$_GET['deliverer_id']."' ";
        $where = 1;
    }
    if ($_GET['order_type']) {
        if ($where) {
            $query = $query."and order_type = ".$_GET['order_type']." ";
        } else {
            $query = $query."where order_type = ".$_GET['order_type']." ";
        }
        $where = 1;
    }
    if ($_GET['addr']) {
        if ($where) {
            $query = $query."and addr like '".$_GET['addr']."%' ";
        } else {
            $query = $query."where addr like '".$_GET['addr']."%' ";
        }
        $where = 1;
    }
    if ($_GET['status']) {
        if ($where) {
            $query = $query."and status = ".$_GET['status']." ";
        } else {
            $query = $query."where status = ".$_GET['status']." ";
        }
        $where = 1;
    }
    if ($_GET['reservation_time']) {
        $time = time() + ($_GET['reservation_time'] * 3600);
        if ($where) {
            $query = $query."and reservation_time < $time ";
        } else {
            $query = $query."where reservation_time < $time ";
        }
        $where = 1;
    }

    $query = $query."order by reservation_time asc";
} else {
    $query = $query."where a.id = '$id' and (a.order_type == ".Types::ORDER_PICK_UP." or a.order_type == ".Types::ORDER_DELIVERY.") ";
    $query = $query."order by a.order_id desc";
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
            if ($row['deliverer_id']) {
                $order['deliverer_id'] = $row['deliverer_id'];
            }
        }
        $array = array();
        $array['order_item_type'] = (int)$row['order_item_type'];
        $array['item_id'] = (int)$row['item_id'];
        if ($array['order_item_type'] == Types::ORDER_ITEM_SALAD) {
            $array['name'] = $salads[$array['item_id']]['name'];
            $items = json_decode($row['salad_items'], true);
            foreach ($items as &$item) {
                $item['name'] = $salad_items[(int)$item['item_id']]['name'];
                $amount_type = 'amount'.$item['amount_type'];
                $item['amount'] = $salad_items[(int)$item['item_id']][$amount_type];
                $item['salad_item_type'] = $salad_items[(int)$item['item_id']]['salad_item_type'];
                $item['image'] = $salad_items[(int)$item['item_id']]['image'];
                $item['unit'] = $salad_items[(int)$item['item_id']]['unit'];
                $item['price'] = $salad_items[(int)$item['item_id']]['price'];
                $item['calorie'] = $salad_items[(int)$item['item_id']]['calorie'];
            }
            $array['salad_items'] = $items;
        } else if ($array['order_item_type'] == Types::ORDER_ITEM_SOUP) {
            $array['name'] = $soups[$array['item_id']]['name'];
            $amount_type = 'amount'.$item['amount_type'];
            $array['amount_type'] = (int)$row['amount_type'];
            $array['amount'] = $soups[$array['item_id']][$amount_type].$soups[$array['item_id']]['unit'];
        } else if ($array['order_item_type'] == Types::ORDER_ITEM_OTHER) {
            $array['name'] = $others[$array['item_id']]['name'];
        } else if ($array['order_item_type'] == Types::ORDER_ITEM_BEVERAGE) {
            $array['name'] = $beverages[$array['item_id']]['name'];
        }
        $array['quantity'] = (int)$row['quantity'];
        $array['price'] = (int)$row['price'];
        $array['calorie'] = (int)$row['calorie'];
        $array['package_type'] = (int)$row['package_type'];

        $order['order_items'][] = $array;
    }
    if ($order) {
        $orders[] = $order;
    }
}

print(json_encode($orders, JSON_UNESCAPED_UNICODE));

mysqli_close($db_conn);

