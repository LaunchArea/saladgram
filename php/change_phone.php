<?php

require 'common.php';
use \Firebase\JWT\JWT;
use \Firebase\JWT\ExpiredException;

$method = $_SERVER['REQUEST_METHOD'];

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

$id = "";
$phone = "";
if (array_key_exists('id', $data) &&
    array_key_exists('phone', $data) &&
    array_key_exists('key', $data)) {

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
        http_response_code(401); // Unauthorized
        return;
    }

    $memcache = memcache_connect($memcache_host, $memcache_port);
    if (!$memcache) {
        http_response_code(500);
        return;
    }
    $id = $data['id'];
    $phone = $data['phone'];
    $key = $data['key'];

    $value = memcache_get($memcache, $phone);
    if (!$value || $value != $key) {
        $array = array();
        $array['success'] = false;
        $array['message'] = "Invalid verification key for $phone.";
        print(json_encode($array));
        http_response_code(401); // Unauthorized
        return;
    }
    memcache_delete($memcache, $phone);
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

$query = "update users set phone = '$phone' where id = '$id'";
$result = mysqli_query($db_conn, "$query");
if (!$result) {
    $array = array();
    $array['success'] = false;
    $array['message'] = mysqli_error($db_conn);
    print(json_encode($array));
    http_response_code(500);
} else {
    $array = array();
    $array['success'] = true;
    $array['message'] = "Phone number changed succesfully.";
    print(json_encode($array));
}

mysqli_close($db_conn);

?>

