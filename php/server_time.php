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

$array = array();
$array['server_time'] = time();
print(json_encode($array));


mysqli_close($db_conn);

?>
