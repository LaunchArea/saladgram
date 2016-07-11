<?php
error_reporting(E_ALL & ~E_NOTICE);

require 'common.php';
require 'coolsms.php';

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
    if (substr($phone, 0, 2) != '01' || (strlen($phone) != 11 && strlen($phone != 10)) || !is_numeric($phone)) {
        http_response_code(400);
        return;
    }
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
            // send sms
            // set to memcache
            $key = rand(100000, 999999);
            $rest = new coolsms($sms_key, $sms_secret);
            $options->to = $phone;
            $options->from = "024064726";
            $options->text = "샐러드그램 인증 번호는 [".$key."] 입니다.";
            $result = $rest->send($options)->getResult();
            if ($result->result_code != '00') {
                http_response_code(500);
                memcache_close($memcache);
                return;
            }
            memcache_set($memcache, "check_phone".$phone, $key, 0, 180);
            memcache_close($memcache);
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
