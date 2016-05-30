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

$response = array();
$result = mysqli_query($db_conn, "select reward from users where id = '$id'");
if (!$result) {
    http_response_code(500);
    return;
} else if (mysqli_num_rows($result) != 0) {
    $row = mysqli_fetch_array($result);
    $response['reward'] = (int)$row['reward'];
    mysqli_free_result($result);
} else {
    http_response_code(500);
    return;
}

$response['rewards'] = [];
$result = mysqli_query($db_conn, "select * from rewards where id = '$id' order by time desc");
if (!$result) {
    http_response_code(500);
    return;
}
while ($row = mysqli_fetch_array($result)) {
    $array = array();
    $array['id'] = $row['id'];
    $array['time'] = (int)$row['time'];
    if ($row['order_id']) {
        $array['order_id'] = (int)$row['order_id'];
    }
    $array['reward_type'] = (int)$row['reward_type'];
    $array['description'] = $row['description'];
    $array['amount'] = (int)$row['amount'];
    $response['rewards'][] = $array;
}
print(json_encode($response, JSON_UNESCAPED_UNICODE));
mysqli_free_result($result);
mysqli_close($db_conn);

?>
