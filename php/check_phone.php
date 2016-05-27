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

$phone = $_GET['phone'];
if ($phone != null) {
    $result = mysqli_query($db_conn, "select phone from users where phone = '$phone'");
    if (!$result) {
        http_response_code(500);
    } else if (mysqli_num_rows($result) == 0) {
        mysqli_free_result($result);
        $memcache = memcache_connect($memcache_host, $memcache_port);
        if (!$memcache) {
            http_response_code(500);
        } else {
            // generate random number
            // set to memcache
            memcache_set($memcache, $phone, 1234, 0, 180);
            memcache_close($memcache);
            // send sms
            $array = array();
            $array['success'] = true;
            $array['message'] = "Verification message sent.";
            print(json_encode($array));
        }
    } else {
        mysqli_free_result($result);
        $array = array();
        $array['success'] = false;
        $array['message'] = "Phone number already exists.";
        print(json_encode($array));
    }

} else {
    http_response_code(400);
}

mysqli_close($db_conn);

?>
