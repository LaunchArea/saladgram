<?php

require 'common.php';
use \Firebase\JWT\JWT;
use \Firebase\JWT\ExpiredException;

$method = $_SERVER['REQUEST_METHOD'];

if ($method != "POST") {
    http_response_code(405); // Method Not Allowed
}

$body = file_get_contents("php://input");
$data = json_decode($body, true);

if (!$data) {
    http_response_code(400); // Bad Request
}

if (!array_key_exists('id', $data) ||
    !array_key_exists('phone', $data) ||
    !array_key_exists('password', $data) ||
    !array_key_exists('name', $data) ||
    !array_key_exists('addr', $data) ||
    !array_key_exists('jwt', $data)) {
    http_response_code(400); // Bad Request
}


$id = $data['id'];
$phone = $data['phone'];
$password = $data['password'];
$name = $data['name'];
$addr = $data['addr'];
$jwt = $data['jwt'];

try {
    $decoded = JWT::decode($jwt, $jwt_secret, array('HS256'));
    if ($phone != $decoded->phone) {
        http_response_code(401); // Unauthorized
    }
} catch (ExpiredException $e1) {
    http_response_code(440); // Login Timeout
} catch (Exception $e2) {
    http_response_code(401); // Unauthorized
}


?>
