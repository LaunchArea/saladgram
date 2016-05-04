<?php

require 'common.php';

$method = $_SERVER['REQUEST_METHOD'];

if ($method != "GET") {
    http_response_code(405); // Method Not Allowed
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

$id = $_GET['id'];
if ($id != null) {
    $result = mysqli_query($db_conn, "select id from users where id = '$id'");
    if (!$result) {
        http_response_code(500);
    } else if (mysqli_num_rows($result) == 0) {
        mysqli_free_result($result);
        $array = array();
        $array['success'] = true;
        print(json_encode($array));
    } else {
        mysqli_free_result($result);
        $array = array();
        $array['success'] = false;
        $array['message'] = "ID already exists.";
        print(json_encode($array));
    }

} else {
    http_response_code(400);
}

mysqli_close($db_conn);

?>
