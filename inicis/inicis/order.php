<?php

function confirm_order($oid) {
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


    $memcache = memcache_connect($memcache_host, $memcache_port);
    if (!$memcache) {
        return FALSE;
    } else {
        $data = memcache_get($memcache, "inipay_order".$oid);
        if (!$data) {
            memcache_close($memcache);
            return FALSE;
        }
        memcache_delete($memcache, "inipay_order".$oid);
        memcache_close($memcache);
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
    $reservation_time = $data['reservation_time'];
    $comment = $data['comment'];
    if (!$comment) {
        $comment = "";
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

    $paid = ($actual_price / 100) * 100;
    $query = $query."$total_price, $discount, $reward_use, $actual_price, $payment_type, $paid, $order_time, $reservation_time, ".Types::STATUS_TODO.", '$comment')";

    $db_conn = mysqli_connect($db_host, $db_user, $db_password, $db_name);
    if (mysqli_connect_errno($db_conn)) {
        return FALSE;
    }

    if (!$db_conn->set_charset("utf8")) {
        return FALSE;
    }

    if (!$db_conn->autocommit(false)) {
        return FALSE;
    }

    $result = mysqli_query($db_conn, $query);
    if (!$result) {
        $array = array();
        $array['success'] = false;
        $array['message'] = mysqli_error($db_conn);
        print(json_encode($array));
        return FALSE;
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
            return FALSE;
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
            return FALSE;
        }
        $reward_query = "update users set reward = reward - $reward_use where id = '$id'";
        $result = mysqli_query($db_conn, $reward_query);
        if (!$result) {
            $array = array();
            $array['success'] = false;
            $array['message'] = mysqli_error($db_conn);
            print(json_encode($array));
            return FALSE;
        }
    }


    if (!mysqli_commit($db_conn)) {
        $array = array();
        $array['success'] = false;
        $array['message'] = mysqli_error($db_conn);
        print(json_encode($array));
        return FALSE;
    }

    mysqli_close($db_conn);
    $data['order_id'] = $order_id;
    return $data;
}
