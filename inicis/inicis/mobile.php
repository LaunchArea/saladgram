<?php

$goodname = $_GET['goodname'];
$oid = $_GET['oid'];
$price = $_GET['price'];
$buyername = $_GET['buyername'];
$buyertel = $_GET['buyertel'];
$buyeremail = $_GET['buyeremail'];
$mname = $_GET['mname'];

$goodname_euckr = iconv("UTF-8", "EUC-KR", $goodname);
$buyername_euckr = iconv("UTF-8", "EUC-KR", $buyername);
$mname_euckr = iconv("UTF-8", "EUC-KR", $mname);

$mid = "INIpayTest";
// $mid = "saladgram0";
$siteDomain = "https://www.saladgram.com/inicis";

?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=euc-kr" />
<meta name="viewport" content="width=device-width"/>
<title>INIpayMobile</title>
<style>
body, tr, td {font-size:10pt; font-family:돋움,verdana; color:#433F37; line-height:19px;}
table, img {border:none}

</style>
<script type="application/x-javascript">
    
    addEventListener("load", function()
    {
        setTimeout(updateLayout, 0);
    }, false);
 
    var currentWidth = 0;
    
    function updateLayout()
    {
        if (window.innerWidth != currentWidth)
        {
            currentWidth = window.innerWidth;
 
            var orient = currentWidth == 320 ? "profile" : "landscape";
            document.body.setAttribute("orient", orient);
            setTimeout(function()
            {
                window.scrollTo(0, 1);
            }, 100);            
        }
    }
 
    setInterval(updateLayout, 400);
    
</script>

<script language=javascript>
window.name = "BTPG_CLIENT";

var width = 330;
var height = 480;
var xpos = (screen.width - width) / 2;
var ypos = (screen.width - height) / 2;
var position = "top=" + ypos + ",left=" + xpos;
var features = position + ", width=320, height=440";

function on_web()
{
	var order_form = document.ini;
	var wallet = window.open("", "BTPG_WALLET", features);
	<!--
	if (wallet == null) 
	{
		if ((webbrowser.indexOf("Windows NT 5.1")!=-1) && (webbrowser.indexOf("SV1")!=-1)) 
		{    // Windows XP Service Pack 2
			alert("팝업이 차단되었습니다. 브라우저의 상단 노란색 [알림 표시줄]을 클릭하신 후 팝업창 허용을 선택하여 주세요.");
		} 
		else 
		{
			alert("팝업이 차단되었습니다.");
		}
		return false;
	}
    -->
	
	order_form.target = "BTPG_WALLET";
	order_form.action = "https://mobile.inicis.com/smart/wcard/";
	order_form.submit();
}

function onSubmit()
{
	var order_form = document.ini;
	order_form.action = "https://mobile.inicis.com/smart/wcard/";
	order_form.submit();
}

</script>
</head>

<body onload="onSubmit()" topmargin="0"  leftmargin="0" marginwidth="0" marginheight="0">
    <form id="form1" name="ini" method="post" action="">
        <input type="hidden" name="P_OID" value="<?php echo $oid ?>">
        <input type="hidden" name="P_GOODS" value="<?php echo $goodname_euckr ?>">
        <input type="hidden" name="P_AMT" value="<?php echo $price ?>">
        <input type="hidden" name="P_UNAME" value="<?php echo $buyername_euckr ?>">
        <input type="hidden" name="P_MNAME" value="<?php echo $mname_euckr ?>">
        <input type="hidden" name="P_MOBILE" value="<?php echo $buyertel ?>">
        <input type="hidden" name="P_EMAIL" value="<?php echo $buyeremail ?>">
        <input type="hidden" name="P_MID" value="INIpayTest"> 
        <input type=hidden name="P_NEXT_URL" value="<?php echo $siteDomain.'/mobile_return.php'?>">
        <input type=hidden name="P_NOTI_URL" value="https://mobile.inicis.com/rnoti/rnoti.php">
        <input type=hidden name="P_HPP_METHOD" value="1">
    </form>
</body>
</html>
