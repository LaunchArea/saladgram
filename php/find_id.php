<?php

require 'common.php';

$method = $_SERVER['REQUEST_METHOD'];

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

$phone = $_GET['phone'];
if ($phone != null) {
    $result = mysqli_query($db_conn, "select id from users where phone = '$phone'");
    if (!$result) {
        http_response_code(500);
    } else if (mysqli_num_rows($result) == 1) {
        $row = mysqli_fetch_array($result);
        $id = $row['id'];
        $len = strlen($id);
        $id = substr($id, 0, len - 3)."***";
        mysqli_free_result($result);
        $array = array();
        $array['success'] = true;
        $array['message'] = $id;
        print(json_encode($array));
    } else {
        mysqli_free_result($result);
        $array = array();
        $array['success'] = false;
        $array['message'] = "ID not found.";
        print(json_encode($array));
    }

} else {
    http_response_code(400);
}

mysqli_close($db_conn);

?>
