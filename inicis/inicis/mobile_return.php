<?php
require_once('order.php');

$P_STATUS = $_POST['P_STATUS'];
$P_REQ_URL = $_POST['P_REQ_URL'];
$P_TID = $_POST['P_TID'];
$P_MID = 'INIpayTest';
// $P_MID = 'saladgram0';
$server_output = "";
$order_data = NULL;

function makeParam($P_TID, $P_MID) {
    return "P_TID=".$P_TID."&P_MID=".$P_MID;
}

function parseData($receiveMsg) { //승인결과 Parse
    $returnArr = explode("&", $receiveMsg);
    foreach($returnArr as $value) {
        $tmpArr = explode("=",$value);
        $returnArr[] = $tmpArr;
    }
    return $returnArr;
}

function chkTid($P_TID) {
    //기승인 TID 여부 확인
    return true;
}

function saveTid($P_TID) {
    //승인된 TID 를 DB 에 저장
}

function setSocket($host, $port) {
    //소켓 생성
}

function connectSocket($sock) {
    //소켓 연결
}

function requestSocket($sock, $param) {
    //데이터 송신
}

function responseSocket() {
    //데이터 수신
}

if($P_STATUS=="00" && chkTid($P_TID)) {
    /*
    $sock = setSocket($P_REQ_URL,443); //https connection
    connectSocket($sock);
    requestSocket($sock,makeParam($P_TID, $P_MID));
    $returnData = responseSocket();
    $returnDataArr = parseData($returnData); //$returnDataArr 에 승인결과 저장
    saveTid($P_TID);
    */


    $ch = curl_init();

    curl_setopt($ch, CURLOPT_URL, $P_REQ_URL);
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, makeParam($P_TID, $P_MID));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $server_output = curl_exec($ch);
    if ($server_output !== FALSE) {
        parse_str($server_output, $arr);
        $order_data = confirm_order($arr['P_OID']);
        if ($order_data === FALSE) {
            print "주문 실패";
        } else {
            print "주문 완료. 최종 주문번호 : ".$order_data['order_id'];
        }
    }
}
?>

<html>
<body onload="orderComplete();">
<?php
var_dump(iconv("EUC-KR", "UTF-8", $server_output));
?>
<script>
function orderComplete() {
<?php
        if ($order_data !== FALSE) {
?>
            var orderCompleteUrl = "/#ordercomplete?";
            orderCompleteUrl = orderCompleteUrl + "order_type=<?php echo $order_data['order_type']; ?>";
            orderCompleteUrl = orderCompleteUrl + "&payment_type=<?php echo $order_data['payment_type']; ?>";
            orderCompleteUrl = orderCompleteUrl + "&addr=<?php echo $order_data['addr']; ?>";
            orderCompleteUrl = orderCompleteUrl + "&actual_price=<?php echo $order_data['actual_price']; ?>";
            orderCompleteUrl = orderCompleteUrl + "&reservation_time=<?php echo $order_data['reservation_time']; ?>";
            orderCompleteUrl = orderCompleteUrl + "&order_time=<?php echo $order_data['order_time']; ?>";
            orderCompleteUrl = orderCompleteUrl + "&order_id=<?php echo $order_data['order_id']; ?>";
            location.href = orderCompleteUrl;
<?php
        }
?>
}
</script>
</body>
</html>

