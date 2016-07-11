<?php

require 'vendor/autoload.php';

$db_host = "saladgram.cue6club2lsf.ap-northeast-2.rds.amazonaws.com";
$db_user = "saladgram";
$db_password = "saladgram";
$db_name = "saladgram";

$memcache_host = '127.0.0.1';
$memcache_port = 11211;

$jwt_secret = "saladgram";
$sms_key = 'NCS574D847E3A2C6';
$sms_secret = 'A1A9D1742F2E9B6E0A02E297F183204E';

abstract class Types {
    const ORDER_ITEM_SALAD = 1;
    const ORDER_ITEM_SOUP = 2;
    const ORDER_ITEM_OTHER = 3;
    const ORDER_ITEM_BEVERAGE = 4;
    const ORDER_ITEM_SELF_SALAD = 5;
    const ORDER_ITEM_SELF_SOUP = 6;

    const ORDER_PICK_UP = 1;
    const ORDER_DELIVERY = 2;
    const ORDER_SUBSCRIBE = 3;
    const ORDER_DINE_IN = 4;
    const ORDER_TAKE_OUT = 5;

    const PAYMENT_CARD = 1;
    const PAYMENT_CASH = 2;
    const PAYMENT_CASH_RECEIPT = 3;
    const PAYMENT_DELIVERY_CARD = 4;
    const PAYMENT_DELIVERY_CASH = 5;
    const PAYMENT_DELIVERY_CASH_RECEIPT = 6;
    const PAYMENT_INIPAY = 7;
    const PAYMENT_AT_PICK_UP = 8;
    const PAYMENT_AT_DELIVERY = 9;
    const PAYMENT_REWARD_ONLY = 10;

    const STATUS_TODO = 1;
    const STATUS_READY = 2;
    const STATUS_SHIPPING = 3;
    const STATUS_DONE = 4;
    const STATUS_CANCELED = 5;

    const REWARD_EVENT = 1;
    const REWARD_USE = 2;
    const REWARD_REWARD = 3;
    const REWARD_CANCEL = 4;

    const SALAD_ITEM_BASE = 1;
    const SALAD_ITEM_VEGETABLE = 2;
    const SALAD_ITEM_FRUIT = 3;
    const SALAD_ITEM_PROTEIN = 4;
    const SALAD_ITEM_OTHER = 5;
    const SALAD_ITEM_DRESSING = 6;

    const PACKAGE_TAKE_OUT = 1;
    const PACKAGE_DINE_IN = 2;
}

function menu_list($db_conn) {
    // TODO : caching
    $salad_items = array();
    $result = mysqli_query($db_conn, "select * from salad_items");
    if (!$result) {
        return [];
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
        return [];
    }

    $salads = array();
    $result = mysqli_query($db_conn, "select * from salads");
    if (!$result) {
        return [];
    } else if (mysqli_num_rows($result) != 0) {
        while ($row = mysqli_fetch_array($result)) {
            $array = array();
            $array['item_id'] = (int)$row['item_id'];
            $array['name'] = $row['name'];
            $array['description'] = $row['description'];
            $array['image'] = $row['image'];
            $array['thumbnail'] = $row['thumbnail'];
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
            if ($row['amount']) {
                $array['amount'] = $row['amount'];
            }
            if ($row['calorie']) {
                $array['calorie'] = (int)$row['calorie'];
            }
            if ($row['price']) {
                $array['price'] = (int)$row['price'];
            }
            $array['available'] = (int)$row['available'];
            $array['hide'] = (int)$row['hide'];
            $salads[] = $array;
        }
        mysqli_free_result($result);
    } else {
        return [];
    }

    $soups = array();
    $result = mysqli_query($db_conn, "select * from soups");
    if (!$result) {
        return [];
    } else if (mysqli_num_rows($result) != 0) {
        while ($row = mysqli_fetch_array($result)) {
            $array = array();
            $array['item_id'] = (int)$row['item_id'];
            $array['name'] = $row['name'];
            $array['description'] = $row['description'];
            $array['image'] = $row['image'];
            $array['thumbnail'] = $row['thumbnail'];
            $array['amount1'] = (int)$row['amount1'];
            $array['amount2'] = (int)$row['amount2'];
            $array['unit'] = $row['unit'];
            $array['calorie'] = (int)$row['calorie'];
            $array['calorie'] = (int)$row['calorie'];
            $array['price'] = (int)$row['price'];
            $array['available'] = (int)$row['available'];
            $array['hide'] = (int)$row['hide'];
            $soups[] = $array;
        }
        mysqli_free_result($result);
    } else {
        return [];
    }

    $others = array();
    $result = mysqli_query($db_conn, "select * from others");
    if (!$result) {
        return [];
    } else if (mysqli_num_rows($result) != 0) {
        while ($row = mysqli_fetch_array($result)) {
            $array = array();
            $array['item_id'] = (int)$row['item_id'];
            $array['name'] = $row['name'];
            $array['description'] = $row['description'];
            $array['image'] = $row['image'];
            $array['thumbnail'] = $row['thumbnail'];
            $array['amount'] = $row['amount'];
            $array['calorie'] = (int)$row['calorie'];
            $array['price'] = (int)$row['price'];
            $array['available'] = (int)$row['available'];
            $array['hide'] = (int)$row['hide'];
            $others[] = $array;
        }
        mysqli_free_result($result);
    } else {
        return [];
    }

    $beverages = array();
    $result = mysqli_query($db_conn, "select * from beverages");
    if (!$result) {
        return [];
    } else if (mysqli_num_rows($result) != 0) {
        while ($row = mysqli_fetch_array($result)) {
            $array = array();
            $array['item_id'] = (int)$row['item_id'];
            $array['name'] = $row['name'];
            $array['description'] = $row['description'];
            $array['image'] = $row['image'];
            $array['thumbnail'] = $row['thumbnail'];
            $array['amount'] = $row['amount'];
            $array['calorie'] = (int)$row['calorie'];
            $array['price'] = (int)$row['price'];
            $array['available'] = (int)$row['available'];
            $array['hide'] = (int)$row['hide'];
            $beverages[] = $array;
        }
        mysqli_free_result($result);
    } else {
        return [];
    }
    $ret = array();
    $ret['salads'] = $salads;
    $ret['salad_items'] = $salad_items;
    $ret['soups'] = $soups;
    $ret['others'] = $others;
    $ret['beverages'] = $beverages;
    return $ret;
}

?>

