<?php

require 'common.php';

$method = $_SERVER['REQUEST_METHOD'];

if ($method == "OPTIONS") {
    return;
}

if ($method != "GET") {
    http_response_code(405); // Method Not Allowed
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

$salads = array();
$result = mysqli_query($db_conn, "select * from salads");
if (!$result) {
    http_response_code(500);
    return;
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
            $item['amount'] = $salad_items[(int)$item['item_id']][$amount_type].$salad_items[(int)$item['item_id']]['unit'];
            $item['salad_item_type'] = $salad_items[(int)$item['item_id']]['salad_item_type'];
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
    http_response_code(500);
    return;
}

$soups = array();
$result = mysqli_query($db_conn, "select * from soups");
if (!$result) {
    http_response_code(500);
    return;
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
    http_response_code(500);
    return;
}

$others = array();
$result = mysqli_query($db_conn, "select * from others");
if (!$result) {
    http_response_code(500);
    return;
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
    http_response_code(500);
    return;
}

$beverages = array();
$result = mysqli_query($db_conn, "select * from beverages");
if (!$result) {
    http_response_code(500);
    return;
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
    http_response_code(500);
    return;
}

$response = array();
$response['salads'] = $salads;
$response['salad_items'] = $salad_items;
$response['soups'] = $soups;
$response['others'] = $others;
$response['beverages'] = $beverages;
print(json_encode($response, JSON_UNESCAPED_UNICODE));

mysqli_close($db_conn);

?>

