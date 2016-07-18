<?php
require_once('../libs/INIStdPayUtil.php');
$SignatureUtil = new INIStdPayUtil();

$goodname = $_GET['goodname'];
$oid = $_GET['oid'];
$price = $_GET['price'];
$buyername = $_GET['buyername'];
$buyertel = $_GET['buyertel'];
$buyeremail = $_GET['buyeremail'];

/*
  //*** 위변조 방지체크를 signature 생성 ***

  oid, price, timestamp 3개의 키와 값을

  key=value 형식으로 하여 '&'로 연결한 하여 SHA-256 Hash로 생성 된값

  ex) oid=INIpayTest_1432813606995&price=819000&timestamp=2012-02-01 09:19:04.004


 * key기준 알파벳 정렬

 * timestamp는 반드시 signature생성에 사용한 timestamp 값을 timestamp input에 그대로 사용하여야함
 */

//############################################
// 1.전문 필드 값 설정(***가맹점 개발수정***)
//############################################
// 여기에 설정된 값은 Form 필드에 동일한 값으로 설정
$mid = "INIpayTest";  // 가맹점 ID(가맹점 수정후 고정)					
//인증
$signKey = "SU5JTElURV9UUklQTEVERVNfS0VZU1RS"; // 가맹점에 제공된 웹 표준 사인키(가맹점 수정후 고정)
$timestamp = $SignatureUtil->getTimestamp();   // util에 의해서 자동생성

//$orderNumber = $mid . "_" . $SignatureUtil->getTimestamp(); // 가맹점 주문번호(가맹점에서 직접 설정)

$cardNoInterestQuota = "11-2:3:,34-5:12,14-6:12:24,12-12:36,06-9:12,01-3:4";  // 카드 무이자 여부 설정(가맹점에서 직접 설정)
$cardQuotaBase = "2:3:4:5:6:11:12:24:36";  // 가맹점에서 사용할 할부 개월수 설정
//###################################
// 2. 가맹점 확인을 위한 signKey를 해시값으로 변경 (SHA-256방식 사용)
//###################################
$mKey = $SignatureUtil->makeHash($signKey, "sha256");

$params = array(
    "oid" => $oid,
    "price" => $price,
    "timestamp" => $timestamp
);
$sign = $SignatureUtil->makeSignature($params, "sha256");

/* 기타 */
$siteDomain = "https://www.saladgram.com/inicis"; //가맹점 도메인 입력
// 페이지 URL에서 고정된 부분을 적는다. 
// Ex) returnURL이 http://localhost:8082/demo/INIpayStdSample/INIStdPayReturn.jsp 라면
//                 http://localhost:8082/demo/INIpayStdSample 까지만 기입한다.

?>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <style type="text/css">
            body { background-color: transparent;}
        </style>

        <!-- 이니시스 표준결제 js -->
        <script language="javascript" type="text/javascript" src="https://stgstdpay.inicis.com/stdjs/INIStdPay.js" charset="UTF-8"></script>

        <script type="text/javascript">
            INIStdPay.pay('SendPayForm_id');
        </script>

    </head>
    <body>
        <form id="SendPayForm_id" name="" method="POST">
            <input type="hidden"  style="width:100%;" name="version" value="1.0" >
            <input type="hidden"  style="width:100%;" name="mid" value="<?php echo $mid ?>" >
            <input type="hidden"  style="width:100%;" name="goodname" value="<?php echo $goodname ?>" >
            <input type="hidden"  style="width:100%;" name="oid" value="<?php echo $oid ?>" >
            <input type="hidden"  style="width:100%;" name="price" value="<?php echo $price ?>" >
            <input type="hidden"  style="width:100%;" name="currency" value="WON" >
            <input type="hidden"  style="width:100%;" name="buyername" value="<?php echo $buyername ?>" >
            <input type="hidden"  style="width:100%;" name="buyertel" value="<?php echo $buyertel ?>" >
            <input type="hidden"  style="width:100%;" name="buyeremail" value="<?php echo $buyeremail ?>" >
            <input type="hidden" type="text"  style="width:100%;" name="timestamp" value="<?php echo $timestamp ?>" >
            <input type="hidden" style="width:100%;" name="signature" value="<?php echo $sign ?>" >
            <input type="hidden"  style="width:100%;" name="returnUrl" value="<?php echo $siteDomain ?>/INIStdPayReturn.php" >
            <input type="hidden"  name="mKey" value="<?php echo $mKey ?>" >
            <input type="hidden"  style="width:100%;" name="gopaymethod" value="Card" >
            <input type="hidden"  style="width:100%;" name="offerPeriod" value="2015010120150331" >
            <input type="hidden" style="width:100%;" name="acceptmethod" value="HPP(1):no_receipt:va_receipt:vbanknoreg(0):vbank(20150611):below1000" >
            <input type="hidden" style="width:100%;" name="languageView" value="" >
            <input type="hidden" style="width:100%;" name="charset" value="" >
            <input type="hidden" style="width:100%;" name="payViewType" value="overlay" >
            <input type="hidden" style="width:100%;" name="closeUrl" value="<?php echo $siteDomain ?>/close.php" >
            <input type="hidden" style="width:100%;" name="popupUrl" value="<?php echo $siteDomain ?>/popup.php" >
            <input type="hidden"  style="width:100%;" name="nointerest" value="<?php echo $cardNoInterestQuota ?>" >
            <input type="hidden"  style="width:100%;" name="quotabase" value="<?php echo $cardQuotaBase ?>" >	
            <input type="hidden"  style="width:100%;" name="vbankRegNo" value="" >
            <input type="hidden"  style="width:100%;" name="merchantData" value="<?php echo $oid ?>" >
        </form>
    </body>
</html>
