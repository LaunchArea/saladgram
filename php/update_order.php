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

if ($jwt == null) {
    http_response_code(401); // Unauthorized
    return;
}
try {
    $decoded = JWT::decode($jwt, $jwt_secret, array('HS256'));
    if ($decoded->id != "saladgram") {
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

if (!$data['order_id']) {
    http_response_code(400); // Bad Request
    return;
}

$updated = 0;
$query = "update orders set";
if ($data['deliverer_id']) {
    $query = $query." deliverer_id = '".$data['deliverer_id']."'";
    $updated = 1;
}
if ($data['payment_type']) {
    if ($updated) {
        $query = $query.",";
    }
    $query = $query." payment_type = ".$data['payment_type'];
    $updated = 1;
}
if ($data['paid']) {
    if ($updated) {
        $query = $query.",";
    }
    $query = $query." paid = ".$data['paid'];
    $updated = 1;
}
if ($data['status']) {
    if ($updated) {
        $query = $query.",";
    }
    $query = $query." status = ".$data['status'];
    $updated = 1;
    if ($data['status'] == Types::STATUS_DONE) {
        $result = mysqli_query($db_conn, "select id, actual_price from orders where order_id = ".$data['order_id']);
        if (!$result) {
            $array = array();
            $array['success'] = false;
            $array['message'] = mysqli_error($db_conn);
            print(json_encode($array));
            http_response_code(500);
            return;
        } else if (mysqli_num_rows($result) != 0) {
            $row = mysqli_fetch_array($result);
            $reward_id = $row['id'];
            $actual_price = (int)$row['actual_price'];
            mysqli_free_result($result);
            if ($actual_price != 0) {
                $time = time();
                $reward_reward = (int)($actual_price * 5 / 100);
                $reward_query = "insert into rewards values('$reward_id', $time, ".$data['order_id'].", ".Types::REWARD_REWARD.", '구매 적립', $reward_reward)";
                $result = mysqli_query($db_conn, $reward_query);
                if (!$result) {
                    $array = array();
                    $array['success'] = false;
                    $array['message'] = mysqli_error($db_conn);
                    print(json_encode($array));
                    http_response_code(500);
                    return;
                }
                $reward_query = "update users set reward = reward + $reward_reward where id = '$reward_id'";
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
        } else {
            mysqli_free_result($result);
            http_response_code(400);
            return;
        }
    }
}
if (!$updated) {
    http_response_code(400); // Bad Request
    return;
}
$query = $query." where order_id = ".$data['order_id'];

$result = mysqli_query($db_conn, $query);
if (!$result) {
    $array = array();
    $array['success'] = false;
    $array['message'] = mysqli_error($db_conn);
    print(json_encode($array));
    http_response_code(500);
    return;
}

if (!mysqli_commit($db_conn)) {
    $array = array();
    $array['success'] = false;
    $array['message'] = mysqli_error($db_conn);
    print(json_encode($array));
    http_response_code(500);
} else {
    $array = array();
    $array['success'] = true;
    $array['message'] = "Order updated successfully.";
    print(json_encode($array));
}

mysqli_close($db_conn);

?>

