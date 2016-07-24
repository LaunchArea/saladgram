<!DOCTYPE html>
<html>
<head>
<script language="javascript" type="text/javascript" charset="UTF-8">
    function script() {
        var el = top.document.getElementsByTagName('iframe');
        el[0].parentNode.removeChild(el[0]);
    }
</script>
</head>
<body onload="script();">
</body>
</html>
