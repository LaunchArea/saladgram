<!DOCTYPE html>
<html>
<head>
<script language="javascript" type="text/javascript" charset="UTF-8">
    function script() {
        console.log("jack");
        var el = top.document.getElementsByTagName('iframe');
        console.log(el[0]);
        console.log(el[0].parentNode);
        el[0].parentNode.removeChild(el[0]);
    }
</script>
</head>
<body onload="script();">
</body>
</html>
