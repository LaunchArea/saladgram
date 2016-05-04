<?php

require 'common.php';
use \Firebase\JWT\JWT;

$method = $_SERVER['REQUEST_METHOD'];

if ($method != "POST") {
    http_response_code(405); // Method Not Allowed
    return;
}

$body = file_get_contents("php://input");
$data = json_decode($body, true);

if (!$data) {
    http_response_code(400); // Bad Request
    return;
}

if (!array_key_exists('id', $data) ||
    !array_key_exists('password', $data)) {
    http_response_code(400); // Bad Request
    return;
}


$id = $data['id'];
$password = $data['password'];

$db_conn = mysqli_connect($db_host, $db_user, $db_password, $db_name);
if (mysqli_connect_errno($db_conn)) {
    http_response_code(500);
    return;
}

if (!$db_conn->set_charset("utf8")) {
    http_response_code(500);
    return;
}

$query = "select password from users where id = '$id'";
$result = mysqli_query($db_conn, "$query");

if (!$result) {
    $array = array();
    $array['success'] = false;
    $array['message'] = mysqli_error($db_conn);
    print(json_encode($array));
    http_response_code(500);
} else {
    if (mysqli_num_rows($result) == 0) {
        $array = array();
        $array['success'] = false;
        $array['message'] = "Invalid ID or password.";
        print(json_encode($array));
    } else {
        $row = mysqli_fetch_array($result);
        if (password_verify($password, $row['password'])) {
            $exp = time() + 60 * 60; // 1 hour expiration period for login token
            $token = array(
                "id" => $id,
                "exp" => $exp
            );
            $array = array();
            $array['success'] = true;
            $array['jwt'] = JWT::encode($token, $jwt_secret);
            print(json_encode($array));
        } else {
            $array = array();
            $array['success'] = false;
            $array['message'] = "Invalid ID or password.";
            print(json_encode($array));
        }
    }
    mysqli_free_result($result);
}

mysqli_close($db_conn);



?>
