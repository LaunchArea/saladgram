<?php

require 'common.php';
use \Firebase\JWT\JWT;

$method = $_SERVER['REQUEST_METHOD'];

if ($method != "GET") {
    http_response_code(405); // Method Not Allowed
    return;
}

$phone = $_GET['phone'];
$key = $_GET['key'];
if ($phone != null && $key != null) {
    $memcache = memcache_connect($memcache_host, $memcache_port);
    if (!$memcache) {
        http_response_code(500);
    } else {
        $value = memcache_get($memcache, $phone);
        if (!$value || $value != $key) {
            $array = array();
            $array['success'] = false;
            $array['message'] = "Invalid verification key for $phone.";
            print(json_encode($array));
        } else {
            memcache_delete($memcache, $phone);
            $exp = time() + 60 * 60; // 1 hour expiration period for phone verification token
            $token = array(
                "phone" => $phone,
                "exp" => $exp
            );
            $array = array();
            $array['success'] = true;
            $array['message'] = "Verification success.";
            $array['jwt'] = JWT::encode($token, $jwt_secret);
            print(json_encode($array));
        }
        memcache_close($memcache);
    }
} else {
    http_response_code(400);
}

?>
