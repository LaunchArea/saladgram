<?php

require 'vendor/autoload.php';

$db_host = "saladgram-mysql.cue6club2lsf.ap-northeast-2.rds.amazonaws.com";
$db_user = "saladgram";
$db_password = "saladgram";
$db_name = "saladgram";

$memcache_host = '127.0.0.1';
$memcache_port = 11211;

$jwt_secret = "saladgram";

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

