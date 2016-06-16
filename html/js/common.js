var mApiUrl = 'https://www.saladgram.com/api/';
// var mApiUrl = '/api/';

var ORDER_TYPE_PICKUP = 1;
var ORDER_TYPE_DELIVERY = 2;
var ORDER_TYPE_SUBSCRIBE = 3;

var mMinDeliveryPrice = 8000;
var mMinPricePerSalad = 3000;

var mFirstDiscountMinPrice = 100000;
var mFirstDiscountRate = 10;
var mSecondDiscountMinPrice = 50000;
var mSecondDiscountRate = 7;
var mThirdDiscountMinPrice = 20000;
var mThirdDiscountRate = 5;

var mDiscountRateByPickup = 5;
var mDiscountRateBySubscribe = 5;

var WEEKDAY_OPEN_HOUR = 7;
var WEEKDAY_CLOSE_HOUR = 22;
var WEEKDAY_BREAK_START_HOUR = 14;
var WEEKDAY_BREAK_END_HOUR = 17;
var SATURDAY_OPEN_HOUR = 9;
var SATURDAY_CLOSE_HOUR = 14;

var MES_REQUIRED_ID = "아이디를 입력하세요";
var MES_REQUIRED_NAME = "이름을 입력하세요";
var MES_REQUIRED_PHONE = "휴대폰번호를 입력하세요";
var MES_REQUIRED_CORRECT_PHONE = "올바른 휴대폰번호를 입력하세요";
var MES_REQUIRED_PHONE_AUTH = "휴대폰을 인증받으세요";
var MES_REQUIRED_ADDR_01 = "건물을 선택하세요";
var MES_REQUIRED_ADDR_02 = "나머지 주소를 입력하세요";
var MES_REQUIRED_PW = "패스워드를 입력하세요";
var MES_REQUIRED_PW_CONFIRM = "패스워드 확인을 입력하세요";
var MES_REQUIRED_PW_SAME = "비밀번호와 비밀번호확인은 같아야 합니다";
var MES_REQUIRED_AUTH_NUM = "인증번호를 입력하세요";
var MES_REQUIRED_PRIVACY = "개인정보 제공에 동의하셔야 합니다";

var MES_ERROR = "실패. 정보를 확인해주세요";
var MES_LOGIN_ERROR = "로그인 정보를 확인해주세요";
var MES_AVAILABLE_ID = "사용가능한 ID입니다";
var MES_UNAVAILABLE_ID = "사용 불가능한 ID입니다";
var MES_PHONE_SEND_AUTH_SUCCESS = "인증번호가 발송 되었습니다";
var MES_PHONE_AUTH_SUCCESS = "인증이 정상적으로 처리되었습니다";
var MES_ADDR_CHANGE_SUCCESS = "주소가 정상적으로 변경되었습니다";
var MES_ADDR_CHANGE_FAIL= "주소 변경에 실패했습니다";
var MES_PHONE_CHANGE_SUCCESS = "휴대폰번호가 정상적으로 변경되었습니다";
var MES_PHONE_CHANGE_FAIL= "휴대폰번호 변경에 실패했습니다";
var MES_PWD_CHANGE_SUCCESS = "패스워드가 정상적으로 변경되었습니다";
var MES_PWD_CHANGE_FAIL= "패스워드 변경에 실패했습니다";
var MES_PHONE_AUTH_SUCCESS = "인증이 정상적으로 처리되었습니다";
var MES_PHONE_AUTH_FAIL = "인증번호를 확인하세요";
var MES_THANKS_FOR_JOIN = "회원가입을 감사드립니다";

window.utils = {

    getDateTextByTimeStamp: function(timestamp, returnType) {
        var date = new Date(timestamp*1000);
        var textYear = date.getFullYear();
        var textMonth = date.getMonth() + 1;
        var textDay = date.getUTCDate();
        var textHours = date.getHours();
        var textMins = date.getMinutes();
        var displayText;
        if(returnType === 'full'){
            displayText = textYear+'년 '+textMonth+'월 '+textDay+'일 '+textHours+'시 '+textMins+'분';
        }else if(returnType === 'date'){
            displayText = textYear+'.'+textMonth+'.'+textDay;
        }
        
        return displayText;
    },
    setCookie: function(cname, cvalue, exdays) {
        var d = new Date();
        d.setTime(d.getTime() + (exdays*24*60*60*1000));
        var expires = "expires="+d.toUTCString();
        document.cookie = cname + "=" + cvalue + "; " + expires;
    },

    getCookie: function(cname) {
        var name = cname + "=";
        var ca = document.cookie.split(';');
        for(var i = 0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') {
                c = c.substring(1);
            }
            if (c.indexOf(name) == 0) {
                return c.substring(name.length, c.length);
            }
        }
        return "";
    },
    deleteCookie: function(cname) {
        //어제 날짜를 쿠키 소멸 날짜로 설정해서 삭제처리
        var expireDate = new Date();
        expireDate.setDate( expireDate.getDate() - 1 );
        var expires = "expires="+expireDate.toGMTString();
        document.cookie = cname + "=; " + expires;
    },
    // checkCookie: function() {
    //     var user = getCookie("username");
    //     if (user != "") {
    //         alert("Welcome again " + user);
    //     } else {
    //         user = prompt("Please enter your name:", "");
    //         if (user != "" && user != null) {
    //             setCookie("username", user, 365);
    //         }
    //     }
    // }
};

