define(['jquery', 'underscore', 'backbone'], function($, _, Backbone) {
    var timeModel = Backbone.Model.extend({
        initialize: function() {
            // this.on('all', function(e) { console.log(this.get('name') + " event: " + e); });
            
            var hoursArray = [];
            for(var i=WEEKDAY_OPEN_HOUR; i <= WEEKDAY_CLOSE_HOUR; i++){
                hoursArray.push(i);
            }
            this.set('hours',hoursArray);
        },
        defaults: {
            // hours:[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24],
            hours:[],
            mins:[00,10,20,30,40,50]    //10분 단위
        },
        /**
         * 년도를 검사한다
         * 1841~2043년 까지만 검사
         * 년도가 검사범위를 벗어나면 alert
         * @param int
         * @return int
         */
        get_year:function(src) {
            if ((src < 1841) || (src > 2043 )) {
                console.log('연도범위는 1841 ~ 2043 까지입니다');
                return;
            } else {
                return src;
            }
        },

        /**
         * 달이 12보다 크거나 1보다 작은지 검사
         * 날짜가 잘못된 경우에는 alert
         *
         * @param int
         * @return int
         */
        get_month: function(src) {
            if ((src < 1) || (src > 12 )) {
                console.log('범위는 1 ~ 12 까지입니다');
                return;
            } else {
                return src;
            }
        },

        /**
         * 날짜가 1일보다 크고 src보다 작은 경우는 날짜를 반환
         * 날짜가 잘못된 경우에는 alert
         *
         * @param int
         * @param int
         * @return int
         */
        get_day :function(src,day) {
            if ((src < 1) || (src > day )) {
                console.log('일 범위가 틀립니다');
                return;
            } else {
                return src;
            }
        },
        /**
         * 음력을 양력으로 변환
         * @param  {[string]} input_day 
         * @return {[string]}           
         */
        lunerCalenderToSolarCalenger: function( input_day ) {
            var kk = [[1, 2, 4, 1, 1, 2, 1, 2, 1, 2, 2, 1],   /* 1841 */
            [2, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 1],
            [2, 2, 2, 1, 2, 1, 4, 1, 2, 1, 2, 1],
            [2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2],
            [1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1],
            [2, 1, 2, 1, 5, 2, 1, 2, 2, 1, 2, 1],
            [2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2],
            [1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1],
            [2, 1, 2, 3, 2, 1, 2, 1, 2, 1, 2, 2],
            [2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2],
            [2, 2, 1, 2, 1, 1, 2, 1, 2, 1, 5, 2],   /* 1851 */
            [2, 1, 2, 2, 1, 1, 2, 1, 2, 1, 1, 2],
            [2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2],
            [1, 2, 1, 2, 1, 2, 5, 2, 1, 2, 1, 2],
            [1, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2, 1],
            [2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2],
            [1, 2, 1, 1, 5, 2, 1, 2, 1, 2, 2, 2],
            [1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2, 2],
            [2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2],
            [2, 1, 6, 1, 1, 2, 1, 1, 2, 1, 2, 2],
            [1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 1, 2],   /* 1861 */
            [2, 1, 2, 1, 2, 2, 1, 2, 2, 3, 1, 2],
            [1, 2, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2],
            [1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1],
            [2, 1, 1, 2, 4, 1, 2, 2, 1, 2, 2, 1],
            [2, 1, 1, 2, 1, 1, 2, 2, 1, 2, 2, 2],
            [1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2],
            [1, 2, 2, 3, 2, 1, 1, 2, 1, 2, 2, 1],
            [2, 2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 1],
            [2, 2, 2, 1, 2, 1, 2, 1, 1, 5, 2, 1],
            [2, 2, 1, 2, 2, 1, 2, 1, 2, 1, 1, 2],   /* 1871 */
            [1, 2, 1, 2, 2, 1, 2, 1, 2, 2, 1, 2],
            [1, 1, 2, 1, 2, 4, 2, 1, 2, 2, 1, 2],
            [1, 1, 2, 1, 2, 1, 2, 1, 2, 2, 2, 1],
            [2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1],
            [2, 2, 1, 1, 5, 1, 2, 1, 2, 2, 1, 2],
            [2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 1, 2],
            [2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1],
            [2, 2, 4, 2, 1, 2, 1, 1, 2, 1, 2, 1],
            [2, 1, 2, 2, 1, 2, 2, 1, 2, 1, 1, 2],
            [1, 2, 1, 2, 1, 2, 5, 2, 2, 1, 2, 1],   /* 1881 */
            [1, 2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2],
            [1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1, 2],
            [2, 1, 1, 2, 3, 2, 1, 2, 2, 1, 2, 2],
            [2, 1, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2],
            [2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2],
            [2, 2, 1, 5, 2, 1, 1, 2, 1, 2, 1, 2],
            [2, 1, 2, 2, 1, 2, 1, 1, 2, 1, 2, 1],
            [2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2],
            [1, 5, 2, 1, 2, 2, 1, 2, 1, 2, 1, 2],
            [1, 2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2],   /* 1891 */
            [1, 1, 2, 1, 1, 5, 2, 2, 1, 2, 2, 2],
            [1, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2],
            [1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2],
            [2, 1, 2, 1, 5, 1, 2, 1, 2, 1, 2, 1],
            [2, 2, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2],
            [1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1],
            [2, 1, 5, 2, 2, 1, 2, 1, 2, 1, 2, 1],
            [2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2],
            [1, 2, 1, 1, 2, 1, 2, 5, 2, 2, 1, 2],
            [1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1],   /* 1901 */
            [2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2],
            [1, 2, 1, 2, 3, 2, 1, 1, 2, 2, 1, 2],
            [2, 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1],
            [2, 2, 1, 2, 2, 1, 1, 2, 1, 2, 1, 2],
            [1, 2, 2, 4, 1, 2, 1, 2, 1, 2, 1, 2],
            [1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1],
            [2, 1, 1, 2, 2, 1, 2, 1, 2, 2, 1, 2],
            [1, 5, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2],
            [1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1],
            [2, 1, 2, 1, 1, 5, 1, 2, 2, 1, 2, 2],   /* 1911 */
            [2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2],
            [2, 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2],
            [2, 2, 1, 2, 5, 1, 2, 1, 2, 1, 1, 2],
            [2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2],
            [1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1],
            [2, 3, 2, 1, 2, 2, 1, 2, 2, 1, 2, 1],
            [2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2],
            [1, 2, 1, 1, 2, 1, 5, 2, 2, 1, 2, 2],
            [1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2, 2],
            [2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2],   /* 1921 */
            [2, 1, 2, 2, 3, 2, 1, 1, 2, 1, 2, 2],
            [1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 1, 2],
            [2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 1],
            [2, 1, 2, 5, 2, 1, 2, 2, 1, 2, 1, 2],
            [1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1],
            [2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2],
            [1, 5, 1, 2, 1, 1, 2, 2, 1, 2, 2, 2],
            [1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2],
            [1, 2, 2, 1, 1, 5, 1, 2, 1, 2, 2, 1],
            [2, 2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 1],   /* 1931 */
            [2, 2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2],
            [1, 2, 2, 1, 6, 1, 2, 1, 2, 1, 1, 2],
            [1, 2, 1, 2, 2, 1, 2, 2, 1, 2, 1, 2],
            [1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1],
            [2, 1, 4, 1, 2, 1, 2, 1, 2, 2, 2, 1],
            [2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1],
            [2, 2, 1, 1, 2, 1, 4, 1, 2, 2, 1, 2],
            [2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 1, 2],
            [2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1],
            [2, 2, 1, 2, 2, 4, 1, 1, 2, 1, 2, 1],   /* 1941 */
            [2, 1, 2, 2, 1, 2, 2, 1, 2, 1, 1, 2],
            [1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2],
            [1, 1, 2, 4, 1, 2, 1, 2, 2, 1, 2, 2],
            [1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1, 2],
            [2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2],
            [2, 5, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2],
            [2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2],
            [2, 2, 1, 2, 1, 2, 3, 2, 1, 2, 1, 2],
            [2, 1, 2, 2, 1, 2, 1, 1, 2, 1, 2, 1],
            [2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2],   /* 1951 */
            [1, 2, 1, 2, 4, 2, 1, 2, 1, 2, 1, 2],
            [1, 2, 1, 1, 2, 2, 1, 2, 2, 1, 2, 2],
            [1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2],
            [2, 1, 4, 1, 1, 2, 1, 2, 1, 2, 2, 2],
            [1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2],
            [2, 1, 2, 1, 2, 1, 1, 5, 2, 1, 2, 2],
            [1, 2, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2],
            [1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1],
            [2, 1, 2, 1, 2, 5, 2, 1, 2, 1, 2, 1],
            [2, 1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2],   /* 1961 */
            [1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1],
            [2, 1, 2, 3, 2, 1, 2, 1, 2, 2, 2, 1],
            [2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2],
            [1, 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1],
            [2, 2, 5, 2, 1, 1, 2, 1, 1, 2, 2, 1],
            [2, 2, 1, 2, 2, 1, 1, 2, 1, 2, 1, 2],
            [1, 2, 2, 1, 2, 1, 5, 2, 1, 2, 1, 2],
            [1, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1],
            [2, 1, 1, 2, 2, 1, 2, 1, 2, 2, 1, 2],
            [1, 2, 1, 1, 5, 2, 1, 2, 2, 2, 1, 2],   /* 1971 */
            [1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1],
            [2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 2, 1],
            [2, 2, 1, 5, 1, 2, 1, 1, 2, 2, 1, 2],
            [2, 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2],
            [2, 2, 1, 2, 1, 2, 1, 5, 2, 1, 1, 2],
            [2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 1],
            [2, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1],
            [2, 1, 1, 2, 1, 6, 1, 2, 2, 1, 2, 1],
            [2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2],
            [1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2, 2],   /* 1981 */
            [2, 1, 2, 3, 2, 1, 1, 2, 2, 1, 2, 2],
            [2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2],
            [2, 1, 2, 2, 1, 1, 2, 1, 1, 5, 2, 2],
            [1, 2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2],
            [1, 2, 2, 1, 2, 2, 1, 2, 1, 2, 1, 1],
            [2, 1, 2, 2, 1, 5, 2, 2, 1, 2, 1, 2],
            [1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1],
            [2, 1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2],
            [1, 2, 1, 1, 5, 1, 2, 1, 2, 2, 2, 2],
            [1, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2],   /* 1991 */
            [1, 2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2],
            [1, 2, 5, 2, 1, 2, 1, 1, 2, 1, 2, 1],
            [2, 2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2],
            [1, 2, 2, 1, 2, 2, 1, 5, 2, 1, 1, 2],
            [1, 2, 1, 2, 2, 1, 2, 1, 2, 2, 1, 2],
            [1, 1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1],
            [2, 1, 1, 2, 3, 2, 2, 1, 2, 2, 2, 1],
            [2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1],
            [2, 2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 1],
            [2, 2, 2, 3, 2, 1, 1, 2, 1, 2, 1, 2],   /* 2001 */
            [2, 2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1],
            [2, 2, 1, 2, 2, 1, 2, 1, 1, 2, 1, 2],
            [1, 5, 2, 2, 1, 2, 1, 2, 2, 1, 1, 2],
            [1, 2, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2],
            [1, 1, 2, 1, 2, 1, 5, 2, 2, 1, 2, 2],
            [1, 1, 2, 1, 1, 2, 1, 2, 2, 2, 1, 2],
            [2, 1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2],
            [2, 2, 1, 1, 5, 1, 2, 1, 2, 1, 2, 2],
            [2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2],
            [2, 1, 2, 2, 1, 2, 1, 1, 2, 1, 2, 1],   /* 2011 */
            [2, 1, 6, 2, 1, 2, 1, 1, 2, 1, 2, 1],
            [2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2],
            [1, 2, 1, 2, 1, 2, 1, 2, 5, 2, 1, 2],
            [1, 2, 1, 1, 2, 1, 2, 2, 2, 1, 2, 2],
            [1, 1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2],
            [2, 1, 1, 2, 3, 2, 1, 2, 1, 2, 2, 2],
            [1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2],
            [2, 1, 2, 1, 2, 1, 1, 2, 1, 2, 1, 2],
            [2, 1, 2, 5, 2, 1, 1, 2, 1, 2, 1, 2],
            [1, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1],   /* 2021 */
            [2, 1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 2],
            [1, 5, 2, 1, 2, 1, 2, 2, 1, 2, 1, 2],
            [1, 2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1],
            [2, 1, 2, 1, 1, 5, 2, 1, 2, 2, 2, 1],
            [2, 1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2],
            [1, 2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 2],
            [1, 2, 2, 1, 5, 1, 2, 1, 1, 2, 2, 1],
            [2, 2, 1, 2, 2, 1, 1, 2, 1, 1, 2, 2],
            [1, 2, 1, 2, 2, 1, 2, 1, 2, 1, 2, 1],
            [2, 1, 5, 2, 1, 2, 2, 1, 2, 1, 2, 1],   /* 2031 */
            [2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2],
            [1, 2, 1, 1, 2, 1, 5, 2, 2, 2, 1, 2],
            [1, 2, 1, 1, 2, 1, 2, 1, 2, 2, 2, 1],
            [2, 1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2],
            [2, 2, 1, 2, 1, 4, 1, 1, 2, 1, 2, 2],
            [2, 2, 1, 2, 1, 1, 2, 1, 1, 2, 1, 2],
            [2, 2, 1, 2, 1, 2, 1, 2, 1, 1, 2, 1],
            [2, 2, 1, 2, 5, 2, 1, 2, 1, 2, 1, 1],
            [2, 1, 2, 2, 1, 2, 2, 1, 2, 1, 2, 1],
            [2, 1, 1, 2, 1, 2, 2, 1, 2, 2, 1, 2],   /* 2041 */
            [1, 5, 1, 2, 1, 2, 1, 2, 2, 2, 1, 2],
            [1, 2, 1, 1, 2, 1, 1, 2, 2, 1, 2, 2]];

            var gan = new Array("Ë£","ëà","Ü°","ïË","Ùæ","Ðù","ÌÒ","ãô","ìó","Í¤");
            var jee = new Array("í­","õä","ìÙ","ÙÖ","òã","ÞÓ","çí","Ú±","ãé","ë·","âù","ú¤");
            var ddi = new Array("Áã","¼Ò","¹ü","Åä³¢","¿ë","¹ì","¸»","¾ç","¿ø¼þÀÌ","´ß","°³","µÅÁö");
            var week = new Array("ÀÏ","¿ù","È­","¼ö","¸ñ","±Ý","Åä");

            var md = new Array(31,0,31,30,31,30,31,31,30,31,30,31);

            var year =input_day.substring(0,4);
            var month =input_day.substring(4,6);
            var day =input_day.substring(6,8);

            // 음력에서 양력으로 변환
            var lyear, lmonth, lday, leapyes;
            var syear, smonth, sday;
            var mm, y1, y2, m1;
            var i, j, k1, k2, leap, w;
            var td, y;
            lyear = this.get_year(year);        // 년도 check
            lmonth = this.get_month(month);     // 월 check

            y1 = lyear - 1841;
            m1 = lmonth - 1;
            leapyes = 0;
            if (kk[y1][m1] > 2)  {
            if (document.frmTest.yoon[0].checked) {
            leapyes = 1;
            switch (kk[y1][m1]) {
            case 3:
            case 5:
            mm = 29;
            break;
            case 4:
            case 6:
            mm = 30;
            break;
            }
            } else {
            switch (kk[y1][m1]) {
            case 1:
            case 3:
            case 4:
            mm = 29;
            break;
            case 2:
            case 5:
            case 6:
            mm = 30;
            break;
            } // end of switch
            } // end of if
            } // end of if

            lday = this.get_day(day, mm);

            td = 0;
            for (i=0; i<y1; i++) {
            for (j=0; j<12; j++) {
            switch (kk[i][j]) {
            case 1:
            td = td + 29;
            break;
            case 2:
            td = td + 30;
            break;
            case 3:
            td = td + 58;    // 29+29
            break;
            case 4:
            td = td + 59;    // 29+30
            break;
            case 5:
            td = td + 59;    // 30+29
            break;
            case 6:
            td = td + 60;    // 30+30
            break;
            } // end of switch
            } // end of for
            } // end of for

            for (j=0; j<m1; j++) {
            switch (kk[y1][j]) {
            case 1:
            td = td + 29;
            break;
            case 2:
            td = td + 30;
            break;
            case 3:
            td = td + 58;    // 29+29
            break;
            case 4:
            td = td + 59;    // 29+30
            break;
            case 5:
            td = td + 59;    // 30+29
            break;
            case 6:
            td = td + 60;    // 30+30
            break;
            } // end of switch
            } // end of for

            if (leapyes == 1) {
            switch(kk[y1][m1]) {
            case 3:
            case 4:
            td = td + 29;
            break;
            case 5:
            case 6:
            td = td + 30;
            break;
            } // end of switch
            } // end of switch

            td =  td + parseFloat(lday) + 22;
            // td : 1841 년 1월1일부터 원하는 날짜까지의 전체 날수의 합
            y1 = 1840;
            do {
            y1 = y1 +1;
            if  ((y1 % 400 == 0) || ((y1 % 100 != 0) && (y1 % 4 == 0))) {
            y2 = 366;
            }
            else {
            y2 = 365;
            }
            if (td <= y2) {
            break;
            }
            else {
            td = td- y2;
            }
            } while(1); // end do-While

            syear = y1;
            md[1] = parseInt(y2) -337;
            m1 = 0;
            do {
            m1= m1 + 1;
            if (td <= md[m1-1]) {
            break;
            }
            else {
            td = td - md[m1-1];
            }
            } while(1); // end of do-While

            smonth = parseInt(m1);
            sday = parseInt(td);

            // 월이 한자리인경우에는 앞에 0을 붙여서 반환
            if ( smonth < 10 ) {
            smonth = "0" + smonth;
            }
            // 일이 한자리인경우에는 앞에 0을 붙여서 반환
            if ( sday < 10 ) {
            sday = "0" + sday;
            }

            return new String( syear + smonth + sday );
        },
        isSaturday: function( yyyymmdd ) {
            var yyyy = parseInt( yyyymmdd.substring( 0, 4 ), 10 );
            var mm  = ( parseInt( yyyymmdd.substring( 4, 6 ), 10 ) - 1 );
            var dd  = parseInt( yyyymmdd.substring( 6, 8 ), 10 );
            var date = new Date( yyyy, mm, dd ); //Date 객체 생성

            // 토요일, 일요일 인경우 false
            // if ( date.getDay() == 6 || date.getDay() == 0 ) {
            // 일요일 인경우 false
            if ( date.getDay() == 6 ) {
                return true;
            } else {
                return false;
            }
        },
        isSunday: function( yyyymmdd ) {
            var yyyy = parseInt( yyyymmdd.substring( 0, 4 ), 10 );
            var mm  = ( parseInt( yyyymmdd.substring( 4, 6 ), 10 ) - 1 );
            var dd  = parseInt( yyyymmdd.substring( 6, 8 ), 10 );
            var date = new Date( yyyy, mm, dd ); //Date 객체 생성

            // 토요일, 일요일 인경우 false
            // if ( date.getDay() == 6 || date.getDay() == 0 ) {
            // 일요일 인경우 false
            if ( date.getDay() == 0 ) {
                return true;
            } else {
                return false;
            }
        },
        isHoliday: function( yyyymmdd ) {
            // °Ë»ç³âµµ
            var yyyy = yyyymmdd.substring( 0, 4 );
            var holidays = new Array();

            // À½·Â °øÈÞÀÏÀ» ¾ç·ÂÀ¸·Î ¹Ù²Ù¾î¼­ ÀÔ·Â
            var tmp01 = this.lunerCalenderToSolarCalenger( yyyy + "0101" );// 음력설날
            var tmp02 = this.lunerCalenderToSolarCalenger( yyyy + "0815" );// 음력추석
            holidays[0] = parseInt(tmp01) - 1; // 음력설 첫째날
            holidays[1] = parseInt(tmp01);   // 음력설 둘째날
            holidays[2] = parseInt(tmp01) + 1; // 음력설 셋째날
            holidays[3] = parseInt(tmp02) - 1; // 추석 첫째날
            holidays[4] = parseInt(tmp02);   // 추석 둘째날
            holidays[5] = parseInt(tmp02) + 1; // 추석 셋째날

            holidays[6] = this.lunerCalenderToSolarCalenger( yyyy + "0408" ); // 석가탄신일

            // ¾ç·Â °øÈÞÀÏ ÀÔ·Â
            holidays[7] = yyyy + "0101";  // 양력설날
            holidays[8] = yyyy + "0301";  // 삼일절
            holidays[9] = yyyy + "0405";  // 식목일
            holidays[10] = yyyy + "0505";  // 어린이날
            holidays[11] = yyyy + "0606";  // 현충일
            holidays[12] = yyyy + "0717";  // 제헌절
            holidays[13] = yyyy + "0815";  // 광복절
            holidays[14] = yyyy + "1003";  // 개천절
            holidays[15] = yyyy + "1225";  // 성탄절 

            var returnValue = false;
            for ( var i=0; i<holidays.length ; i++ ) {
                if ( holidays[i] == yyyymmdd ) {
                    returnValue = true;
                }
            };
            return returnValue;
        },
        //영업날짜/시간인지 확인
        isStoreHours: function(checkDate){

            var checkYear = checkDate.getFullYear();
            var checkMonth = checkDate.getMonth() + 1;
            var checkDay = checkDate.getUTCDate();
            var checkHours = checkDate.getHours();
            console.log('checkYear : ' + checkYear);
            console.log('checkMonth : ' + checkMonth);
            console.log('checkDay : ' + checkDay);
            console.log('checkHours : ' + checkHours);
            if ( checkMonth < 10 ) {
                checkMonth = "0" + checkMonth;
            }
            // 일이 한자리인경우에는 앞에 0을 붙여서 반환
            if ( checkDay < 10 ) {
                checkDay = "0" + checkDay;
            }

            var checkFullDay = checkYear+''+checkMonth+''+checkDay;
            console.log('checkFullDay : ' + checkFullDay);

            var isHoliday = this.isHoliday(checkFullDay);
            var isSunday = this.isSunday(checkFullDay);
            var isSaturday = this.isSaturday(checkFullDay);

            var isStoreDay = true;
            
            if(isHoliday || isSunday){
                isStoreDay = false;
            }else if(!isSaturday){  //주중
                if(checkHours < WEEKDAY_OPEN_HOUR || checkHours >= WEEKDAY_CLOSE_HOUR){
                    console.log('주중 영업시간이 아닙니다');
                    isStoreDay = false;
                }else if(checkHours >= WEEKDAY_BREAK_START_HOUR && checkHours < WEEKDAY_BREAK_END_HOUR){
                    console.log('주중 브레이크 타임 입니다');
                    isStoreDay = false;
                }
            }else{  //토요일
                if(checkHours < SATURDAY_OPEN_HOUR || checkHours >= SATURDAY_CLOSE_HOUR){
                    console.log('토요일 영업시간이 아닙니다');
                    isStoreDay = false;
                }
            }
            return isStoreDay;
        }
    });
    return timeModel;
});