define(['jquery', 'underscore', 'backbone'], function($, _, Backbone) {
    var timeModel = Backbone.Model.extend({
        initialize: function() {
            // this.on('all', function(e) { console.log(this.get('name') + " event: " + e); });

            var hoursArray = [];
            for(var i=WEEKDAY_OPEN_HOUR; i <= WEEKDAY_CLOSE_HOUR; i++){
                hoursArray.push(i);
            }
            this.set('hours',hoursArray);
			$.ajax({
                url: mApiUrl + 'holidays.php',
                method: 'GET',
                success: function(res) {
                    this.holidays = JSON.parse(res);
                    console.log('holidays : ' + JSON.stringify(this.holidays));
                },
                error:function(error){
                    console.log('휴일 체크 실패');
                    console.log('error : ' + JSON.stringify(error));
                }
            });
        },
        defaults: {
            // hours:[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24],
            hours:[],
            mins:[00,20,40]    //20분 단위
        },
        isSaturday: function(date) {
            if ( date.getDay() == 6 ) {
                return true;
            } else {
                return false;
            }
        },
        isSunday: function(date) {
            if ( date.getDay() == 0 ) {
                return true;
            } else {
                return false;
            }
        },
        isHoliday: function(date) {
            dateString = this.dateFormat(date);
            if (((Array)(this.holidays)).indexOf(dateString) == -1) {
                return false;
            } else {
                return true;
            }
        },
        isOffDay:function(date){
            var isHoliday = this.isHoliday(date);
            var isSunday = this.isSunday(date);
            if(isHoliday || isSunday){
                return true;
            } else {
                return false;
            }
        },
        //영업날짜/시간인지 확인, 가능시 true 리턴, 불가능시 string reason 리턴
        checkOrderTime: function(orderType, currentDate, reservationDate){
            if (orderType == ORDER_TYPE_DELIVERY) {
                // 배달 주문
                if (reservationDate != null && currentDate.getDate() != reservationDate.getDate()) {
                    // 내일 예약
                    var reservationHour = reservationDate.getHours();
                    if (this.isSaturday(reservationDate)) {
                        if (reservationHour < 9 || reservationHour >= 14) {
                            return "토요일 배달 시간은 오전 9시부터 오후 2시까지 입니다";
                        }
                    } else if (this.isSunday(reservationDate) || this.isHoliday(reservationDate)) {
                        return "일요일 및 공휴일은 매장 휴일입니다";
                    } else {
                        if (reservationHour < 7 || reservationHour >= 14) {
                            return "평일 배달 시간은 오전 7시부터 오후 2시까지 입니다";
                        }
                    }
                } else if (reservationDate != null && currentDate.getDate() == reservationDate.getDate()) {
                    // 당일 예약
                    var reservationHour = reservationDate.getHours();
                    if (this.isSaturday(reservationDate)) {
                        if (reservationHour < 9 || reservationHour >= 14) {
                            return "토요일 배달 시간은 오전 9시부터 오후 2시까지 입니다";
                        }
                    } else if (this.isSunday(reservationDate) || this.isHoliday(reservationDate)) {
                        return "일요일 및 공휴일은 매장 휴일입니다";
                    } else {
                        if (reservationHour < 7 || reservationHour >= 14) {
                            return "평일 배달 시간은 오전 7시부터 오후 2시까지 입니다";
                        }
                    }
                } else {
                    // 즉시
                    var currentHour = currentDate.getHours();
                    if (this.isSaturday(currentDate)) {
                        if (currentHour < 9 || currentHour >= 14) {
                            return "토요일 배달 시간은 오전 9시부터 오후 2시까지 입니다";
                        }
                    } else if (this.isSunday(currentDate) || this.isHoliday(currentDate)) {
                        return "일요일 및 공휴일은 매장 휴일입니다";
                    } else {
                        if (currentHour < 7 || currentHour >= 14) {
                            return "평일 배달 시간은 오전 7시부터 오후 2시까지 입니다";
                        }
                    }
                }
            } else if (orderType == ORDER_TYPE_PICKUP) {
                // 픽업 주문
                if (reservationDate != null && currentDate.getDate() != reservationDate.getDate()) {
                    // 내일 예약
                    var reservationHour = reservationDate.getHours();
                    if (this.isSaturday(reservationDate)) {
                        if (reservationHour < 9 || reservationHour >= 14) {
                            return "토요일 영업 시간은 오전 9시부터 오후 2시까지 입니다";
                        }
                    } else if (this.isSunday(reservationDate) || this.isHoliday(reservationDate)) {
                        return "일요일 및 공휴일은 매장 휴일입니다";
                    } else {
                        if (reservationHour < 7 || reservationHour >= 22) {
                            return "평일 영업 시간은 오전 7시부터 오후 10시까지 입니다";
                        }
                        if (reservationHour >= 14 && reservationHour < 17) {
                            return "오후 2시부터 오후 5시까지는 브레이크타임 입니다";
                        }
                    }
                } else if (reservationDate != null && currentDate.getDate() == reservationDate.getDate()) {
                    // 당일 예약
                    if (this.isSaturday(reservationDate)) {
                        if (reservationHour < 9 || reservationHour >= 14) {
                            return "토요일 영업 시간은 오전 9시부터 오후 2시까지 입니다";
                        }
                    } else if (this.isSunday(reservationDate) || this.isHoliday(reservationDate)) {
                        return "일요일 및 공휴일은 매장 휴일입니다";
                    } else {
                        if (reservationHour < 7 || reservationHour >= 22) {
                            return "평일 영업 시간은 오전 7시부터 오후 10시까지 입니다";
                        }
                        if (currentDate.getHours() >= 21) {
                            return "픽업 웹주문 가능 시간은 오후 9시까지 입니다";
                        }
                        if (reservationHour >= 14 && reservationHour < 17) {
                            return "오후 2시부터 오후 5시까지는 브레이크타임 입니다";
                        }
                    }
                } else {
                    // 즉시
                    var currentHour = currentDate.getHours();
                    if (this.isSaturday(currentDate)) {
                        if (currentHour < 9 || currentHour >= 14) {
                            return "토요일 영업 시간은 오전 9시부터 오후 2시까지 입니다";
                        }
                    } else if (this.isSunday(currentDate) || this.isHoliday(currentDate)) {
                        return "일요일 및 공휴일은 매장 휴일입니다";
                    } else {
                        if (currentHour < 7 || currentHour >= 22) {
                            return "평일 영업 시간은 오전 7시부터 오후 10시까지 입니다";
                        }
                        if (currentHour >= 21) {
                            return "픽업 웹주문 가능 시간은 오후 9시까지 입니다";
                        }
                        if (currentHour >= 14 && currentHour < 17) {
                            return "오후 2시부터 오후 5시까지는 브레이크타임 입니다";
                        }
                    }
                }
            } else {
                return "정기배송 서비스 준비중입니다";
            }

            return true;
        },
        dateFormat: function(date) {
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var day = date.getUTCDate();

            if (month < 10) {
                month = '0' + month;
            }
            if (day < 10) {
                day = '0' + day;
            }
            return year+'-'+month+'-'+day;
        }
    });
    return timeModel;
});
