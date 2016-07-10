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

$result = mysqli_query($db_conn, "select * from holidays order by date");
if (!$result) {
    http_response_code(500);
} else {
    $array = array();
    while ($row = mysqli_fetch_array($result)) {
        $array[] = $row['date'];
    }
    print(json_encode($array, JSON_UNESCAPED_UNICODE));
    mysqli_free_result($result);
}


mysqli_close($db_conn);

?>
