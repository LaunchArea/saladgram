// var mApiUrl = 'https://www.saladgram.com/api/';
var mApiUrl = '/api/';

var ORDER_TYPE_PICKUP = 1;
var ORDER_TYPE_DELIVERY = 2;
var ORDER_TYPE_SUBSCRIBE = 3;

var mMinDeliveryPrice = 5000;
var mMinPricePerSalad = 3000;

//10만원 이상 주문시 10%할인
var mFirstDiscountMinPrice = 100000;    
var mFirstDiscountRate = 10;
//5만원 이상 주문시 7%할인
var mSecondDiscountMinPrice = 50000;
var mSecondDiscountRate = 7;
//2만원 이상 주문시 5%할인
var mThirdDiscountMinPrice = 20000;
var mThirdDiscountRate = 5;

var mDiscountRateByPickup = 5;
var mDiscountRateBySubscribe = 5;

//영업시간
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

var MES_WRAN_ORDER_PAGE_REFRESH = '이 페이지를 벗어나면 진행중인 주문은 저장되지 않습니다';
var MES_WRAN_ORDER_PAGE_BACK = '진행중이던 주문을 삭제하시겠습니까?';

var MES_WRAN_RESERVE_TIME = '예약시간이 현재시간보다 작습니다';
var MES_WRAN_NOT_STORE_HOUR = '선택하신 시간은 영업시간이 아닙니다';
var MES_WRAN_NOT_NOW_STORE_HOUR = '지금은 영업시간이 아닙니다';
var MES_EMPTY_SALAD_ITEM = "아이템을 한개 이상 선택하세요";

var MES_MIN_SALAD_PRICE = "샐러드1개당 최소금액은 "+mMinPricePerSalad+"원 입니다";
var MES_EMPTY_ORDER_ITEM = "장바구니가 비었습니다";
var MES_MIN_DELIVERY_PRICE = "최소 배달금액은 "+mMinDeliveryPrice+"원 입니다";
var MES_VALID_USE_REWARD = "사용가능 한 포인트를 입력해주세요";
var MES_MIN_UNIT_USE_REWARD = "적립금은 100원 단위로 사용 가능합니다";

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
        if (typeof(Storage) !== "undefined") {
            sessionStorage.setItem(cname, cvalue);
        } else {
            var d = new Date();
            d.setTime(d.getTime() + (exdays*24*60*60*1000));
            var expires = "expires="+d.toUTCString();
            document.cookie = cname + "=" + cvalue + "; " + expires;
        }
    },

    getCookie: function(cname) {
        if (typeof(Storage) !== "undefined") {
            return sessionStorage.getItem(cname);
        } else {
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
        }
    },
    deleteCookie: function(cname) {
        if (typeof(Storage) !== "undefined") {
            return sessionStorage.removeItem(cname);
        } else {
            //어제 날짜를 쿠키 소멸 날짜로 설정해서 삭제처리
            var expireDate = new Date();
            expireDate.setDate( expireDate.getDate() - 1 );
            var expires = "expires="+expireDate.toGMTString();
            document.cookie = cname + "=; " + expires;
        }
    },
};

if (typeof console === "undefined") {
    var console = {
        log: function (logMsg) { }
    };
}

