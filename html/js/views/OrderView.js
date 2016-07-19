define(['jquery', 'underscore', 'backbone','text!templates/order/orderTimeSelectTemplate.html'
	,'text!templates/order/orderTemplate.html','text!templates/order/orderItemListTemplate.html'
	,'text!templates/order/selectSaladItemListTemplate.html','text!templates/order/finalOrderPageTemplate.html'
	,'text!templates/order/recentOrderListTemplate.html','text!templates/order/editAddrTemplate.html'
	, 'models/TimeModel' ,'models/OrderInfoModel','collections/ItemCollection','collections/UserRecentOrderCollection'
	,'collections/OrderItemsCollection','collections/PlaceOrderCollection','collections/SaladItemsCollection']
	, function($, _, Backbone, orderTimeSelectTemplate
		,orderTemplate, orderItemListTemplate, selectSaladItemListTemplate, finalOrderPageTemplate
		,recentOrderListTemplate,editAddrTemplate,  TimeModel, OrderInfoModel, ItemCollection, UserRecentOrderCollection
		,OrderItemsCollection, PlaceOrderCollection, SaladItemsCollection) {

    var orderView = Backbone.View.extend({
	    initialize: function () {
			// this.orderItem = $('#order_item_temp').children();;
		},
        el: $("#page"),
        orderlistView : '',
        selSaladItemListView : '',
        // cancelOrder : false,
        render: function() {
        	var mOrderView = this;
        	console.log('window.userCollection : ' + JSON.stringify(window.userCollection));
        	//멤버,게스트로 로그인 되지 않은 경우
        	if(typeof window.userCollection === "undefined" 
        		|| typeof window.userCollection.models[0] === "undefined"
        		|| typeof window.userCollection.models[0].get === "undefined"
        		|| typeof window.userCollection.models[0].get('jwt') === "undefined"){
        		location.href = '/';
        		return;
        	}else{
        		if(typeof window.orderInfoModel === "undefined"){
					window.orderInfoModel = new OrderInfoModel();
				};
				var userInfo = window.userCollection.models[0].get('user_info');
				var userType = window.userCollection.models[0].get('user_type');
				var jwt = window.userCollection.models[0].get('jwt');
				var id = userInfo.id;
				var addr = userInfo.addr;
				var phone = userInfo.phone;

				//뒤로가기, 새로고침시 alert
				if(typeof window.cancelOrder === 'undefined'){
					window.cancelOrder = false;
					function doCheck() {
					  // TODO: add code that checks the app state that we have unsaved data
					  return window.cancelOrder || window.confirm(MES_WRAN_ORDER_PAGE_BACK);
					}
					var oldLoad = Backbone.History.prototype.loadUrl;
					Backbone.History.prototype.loadUrl = function() {
						if(doCheck()) {
							console.log('docheck ok ');
							window.cancelOrder = true;
							// location.href = '/';
							return oldLoad.apply(this, arguments);
						} else {
							console.log('docheck no ');
							// change hash back
							window.cancelOrder = true;
							// history.back();
							location.href = '/#order';
							window.cancelOrder = false;
							// return true;
						}
					};
				    $(window).on("beforeunload", function(){
				    	console.log('window.cancelOrder : ' + window.cancelOrder);
				        if(!window.cancelOrder){
				        	// window.cancelOrder = true;
				        	return MES_WRAN_ORDER_PAGE_REFRESH;
				        }
				    });
				}else{
					window.cancelOrder = false;
				}
        	}

			$('#main_nav_wrap').remove();	

			window.times = new TimeModel();
			console.log('JSON.stringify(times) : ' + JSON.stringify(times));

			//서버 시간 체크
			$.ajax({
                url: mApiUrl + 'server_time.php',
                method: 'GET',
                success: function(res) {
                    console.log('서버시간 체크 성공');
                    var serverTime = JSON.parse(res).server_time;
                    // var serverTime = '1466214763';//토요일 오전 10시52분
                    // var serverTime = '1466128542';//금요일 오전 10시55분
                    // var serverTime = '1465867842';//화요일 오전 10시30분
                    // var serverTime = '1466303142';//일요일
                    // var serverTime = '1482632742';//성탄절 and 일요일
                    // var serverTime = '1475461542';//개천절 월요일
                    // var serverTime = '1473992742';//추석 셋째날 금요일
                    var currentDate = new Date((serverTime*1000));
					var currentHour = currentDate.getHours();
					var currentMins = currentDate.getMinutes();

					console.log('currentHour : ' + currentHour);
					console.log('currentMins : ' + currentMins);
                    var offDay = window.times.isOffDay(currentDate);
					var isSaturday = window.times.isSaturday(currentDate);
					var userType = window.userCollection.models[0].get('user_type');

					function setHtmlView(){
						var models = window.menuCollection.models[0];
						mOrderView.$el.html(_.template(orderTemplate)({
							user_type: userType,
							salads:models.get('salads'),
							salad_items:models.get('salad_items'),
							soups:models.get('soups'),
							others:models.get('others'),
							beverages:models.get('beverages')
						}));

						var addrText = window.userCollection.models[0].get('user_info').addr;
						if(currentMins > 50){
							currentHour = currentHour + 1
							currentMins = 0;
						};
						console.log('addrText : ' +  addrText);
						mOrderView.$el.find('#delivery').append(_.template(orderTimeSelectTemplate)({
							week_break_start:WEEKDAY_BREAK_START_HOUR,
							week_break_end:WEEKDAY_BREAK_END_HOUR,
							saturday_open:SATURDAY_OPEN_HOUR,
							saturday_end:SATURDAY_CLOSE_HOUR,
                            off_day:offDay,
							is_saturday:isSaturday,
							order_type : 'delivery',
							addr: addrText,
							hours: window.times.get('hours'),
							c_hours: currentHour,
							mins: window.times.get('mins'),
							c_mins: currentMins,
						}));
						mOrderView.$el.find('#pickup').append(_.template(orderTimeSelectTemplate)({
							week_break_start:WEEKDAY_BREAK_START_HOUR,
							week_break_end:WEEKDAY_BREAK_END_HOUR,
							saturday_open:SATURDAY_OPEN_HOUR,
							saturday_end:SATURDAY_CLOSE_HOUR,
                            off_day:offDay,
							is_saturday:isSaturday,
							order_type : 'pickup',
							addr: addrText,
							hours: window.times.get('hours'),
							c_hours: currentHour,
							mins: window.times.get('mins'),
							c_mins: currentMins,
						}));

						//mobile 우측 버튼
						$('[data-toggle="offcanvas"]').click(function (e) {
							console.log('e : ' + e);
							var currentTargetId = $(e.currentTarget).attr('id');
							if(currentTargetId === 'btn_order_results_toggle'){
								$('#step_2_wrap .row-offcanvas').toggleClass('active');
								$('#order_results_toggle button').toggleClass('active-toggle');
								$('#order_results_toggle button').removeClass('animation');
							    $('#step_2_wrap').toggleClass('offcanvas-active');
							}else if(currentTargetId === 'btn_salad_sel_results_toggle'){
								$('#step_3_wrap .row-offcanvas').toggleClass('active');
								$('#salad_sel_results_toggle button').toggleClass('active-toggle');
								$('#salad_sel_results_toggle button').removeClass('animation');
							    $('#step_3_wrap').toggleClass('offcanvas-active');
							}
						   
						});

						//멤버일경우 최근 주문 리스트를 가져와서 저장
						if(userType === 'member'){
							
							window.orderInfoModel.set('phone', phone);
							window.orderInfoModel.set('id', id);
							window.orderInfoModel.set('addr', addr);

							//사용자 최근 주문했던 리스트
			                if(typeof window.userRecentOrderCollection === "undefined"){
			                    window.userRecentOrderCollection = new UserRecentOrderCollection();
			                };
			                var setHeader = function (xhr) {
			                    xhr.setRequestHeader('jwt', jwt);
			                }; 
			                window.userRecentOrderCollection.fetchById(
			                    id,{ 
			                    beforeSend: setHeader,
			                    success: function (collection) {
			                        console.log('fetch success!');
			                        // console.log('window.userRecentOrderCollection : ' + JSON.stringify(window.userRecentOrderCollection));
			                        // console.log('collection.models; : ' + JSON.stringify(collection.models));

									var recentOrdersModel = collection.models;
			                        for(var i=0; i < recentOrdersModel.length; i++){
					                    var reservationTime = recentOrdersModel[i].get('reservation_time');
					                    var name = recentOrdersModel[i].get('name');
					                    var displayText = window.utils.getDateTextByTimeStamp(reservationTime,'date');
					                    recentOrdersModel[i].set('reservation_text',displayText);
					                    recentOrdersModel[i].set('name',name);
					                    recentOrdersModel[i].set('checked',false);
					                };
			                        var template1 = _.template(recentOrderListTemplate)({
											before_orders: recentOrdersModel,
									});
									mOrderView.$el.prepend(template1);
									console.log('userType : ' + userType);
									var template2 = _.template(editAddrTemplate)({
											user_type: userType,
									});
									mOrderView.$el.prepend(template2);

									//modal backdrop reset
					                $('.modal').on('shown.bs.modal', function(e){
					                    console.log('aaa');
					                    $(this).modal('handleUpdate'); //Update backdrop on modal show
					                    $(this).scrollTop(0); //reset modal to top position
					                }); 

			                    }}
			                );
						}else{
							window.orderInfoModel.unset('id');
							window.orderInfoModel.set('phone', phone);
							window.orderInfoModel.set('addr', addr);
						};

						//create timer
						// var timer = null;
						// // bind to determined event(s) (mobild과 통합을 위해 jquery bind사용)
						// var orderItemWrap = $(".order-item-wrap");
						// orderItemWrap.bind('mousedown touchstart', function(e) {
						//     // clearTimeout(timer);
						//     // timer = setTimeout(function () {
						//     	console.log('menuMouseDown touchstart');
						// 		var menuWrap = $(e.currentTarget);
						// 		menuWrap.addClass('mousedown_menu');
						//     // }, 50);
						// });
						// orderItemWrap.bind('mouseleave touchmove', function(e) {

						//     // clearTimeout(timer);
						//     // timer = setTimeout(function () {
						//     	console.log('mouseleave touchmove');
						// 		var menuWrap = $(e.currentTarget);
						// 		menuWrap.removeClass('mousedown_menu');
						//     // }, 50);touchend
						// });
						// orderItemWrap.bind('mouseup click', function(e) {

						//     // clearTimeout(timer);
						//     // timer = setTimeout(function () {
						//     	console.log('mouseup touchend');
						// 		var menuWrap = $(e.currentTarget);
						// 		menuWrap.removeClass('mousedown_menu');
						// 		mOrderView.menuClick(e);
						//     // }, 50);
						// });
					};
					if(typeof window.menuCollection === "undefined"){
						window.menuCollection = new ItemCollection(); 
						window.menuCollection.fetch({
							success: function () {
								console.log('fetch success!'); 
								console.log(JSON.stringify(window.menuCollection.models));
								var soups = window.menuCollection.models[0].get('soups');
								for(var i=0; i < soups.length; i++){
									soups[i].amount_type = 1;
									soups[i].amount = soups[i].amount1;
									// soups[i].price = soups[i].price * 1.5;
									// soups[i].calorie = soups[i].calorie * 1.5;
								}
								// console.log(JSON.stringify(window.menuCollection.models[0]));
								// console.log(JSON.stringify(window.menuCollection.models[0].get('salads')));
								setHtmlView();
							}
						});
					}else{
						setHtmlView();
						// console.log(JSON.stringify(window.menuCollection.models[0]));
					}
                },
                error:function(error){
                    console.log('서버시간 체크 실패');
                    console.log('error : ' + JSON.stringify(error));
                }
            });
			
        },
        events: {
        	/*************************STEP 1**********************************/
        	"click div.order-type-tab": "orderTypeTabChange",
			"change select.dates-select": "changedDate",
			"change select.hours-select": "changedHour",
			"click a.btn-change-order-addr": "showChangeOrderAddr",
			"click button#btn_change_order_addr": "changeOrderAddr",
			"click button#btn_change_basic_addr": "changeOrderAddr",
			"click button#btn_order_type_done": "orderTypeDone",
			/*************************STEP 2**********************************/
			"click .menu-tab li": "clickSubMenu",
        	"click .btn-scroll-top": "pageScrollTop",
        	"scroll": 'scroll',	//#page scroll
			// "mousedown div.order-item-wrap": "menuMouseDown",
			// "touchstart div.order-item-wrap": "menuMouseDown",
			// "mouseleave div.order-item-wrap": "menuMouseLeave",
			// "touchend div.order-item-wrap": "menuMouseLeave",
			"click div.order-item-wrap": "menuClick",
			"click button.btn-add-soup": "addItem",
			"click span.menu-modify": "modifyItem",
			"click span.menu-remove": "removeItem",
			"click button.btn-soup-amount": "soupAmountChange",
			"click button.btn-item-quantity": "itemQuantityChange",
			"click button#btn_items_sel_done": "orderItemsSelDone",
			/**STEP 2(add from before order list)**/
			"click a#btn_show_before_orders": "showBeforeOrders",
			"click input.before-order-checkbox-input": "changeBeforeOrdersCheck",
			"click button#btn_add_before_order_items": "addBeforeOrders",
			/*************************STEP 2-1*************(salad item select)*********/
			"click div.salad-item-in-salad-wrap": "addSaladItem",
			"click button.btn-item-amount": "saladItemAmountChange",
			"click button.btn-salad-item-del": "saladItemRemove",
			"click div.btn-salad-item-cancel": "saladItemCancel",
			"click button#btn_salads_items_sel_done": "saladItemsChangeDone",
			"click a.btn-change-step-time": "changeTimeBack",
			/*************************STEP 3*********************************/
			"click button#btn_do_order": "doOrder",
			"click button#check_order_type_online": "checkOrderTypeOnline",
			"click button#check_order_type_offline": "checkOrderTypeOffline",
			"click a.btn-change-step-order": "changeOrderBack",
			"click button#btn_confirm_order_complete": "confirmOrderComplete",
		},
		/**
		 * 주문 스탭을 변경합니다
		 * @param  {[int]} nextStepNum (변경될 스탭번호)
		 * 1. 주문타입/날짜/시간 선택
		 * 2. 메뉴 선택
		 * 3. 샐러드의 아이템 선택
		 * 4. 최종주문 페이지
		 */
		changeStep: function(nextStepNum, saladName){
			var currentStepNum = parseInt($('#order_full_wrap').attr('stepNum'));

			$('#order_full_wrap').addClass('order-step-'+nextStepNum);
			$('#order_full_wrap').removeClass('order-step-'+currentStepNum);
			$('#order_full_wrap').attr('stepNum',nextStepNum);
			$('.navbar-order-wrap').removeClass('navbar-order-salad-items-nav');
			$('#btn_order_nav_cancel').html('주문취소');
			var stepTitle='';
			switch(nextStepNum){
				case 1: 
					stepTitle = '주문방법';
					break;
				case 2:
					stepTitle = '메뉴선택';
					var soupItemsWraps = $('.order-item-soup-wrap');
					for(var i=0; i < soupItemsWraps.length; i++){
						soupItemsWraps.eq(i).removeClass('item-selected');
						soupItemsWraps.eq(i).removeAttr('selecteditem');
						// if(soupItemsWraps.eq(i).attr('selecteditem') === 'true'){
						// 	soupItemsWraps.eq(i).addClass('item-selected');
						// }
					};
					break;
				case 3:
					if(typeof saladName !== 'undefined'){
						stepTitle = saladName;
					}else{
						stepTitle = '메뉴구성';
					}
					$('.navbar-order-wrap').addClass('navbar-order-salad-items-nav');
					$('#btn_order_nav_cancel').html('취소');
					break;
				case 4:
					stepTitle = '주문결제';
					break;
			};
			$('#navbar_order_center_title').html(stepTitle);
			this.pageScrollTop();
		},

		
		/*****************************STEP 1**********************************/
		//주문 방법을 변경
		orderTypeTabChange : function(e){
			$(e.currentTarget).addClass('active');
			$(e.currentTarget).siblings('.order-type-tab').removeClass('active');
        },
        //쉬는날인지 반환
        //날짜를 변경합니다
		changedDate: function(e){
			console.log('dates-select changedDate');
			var mOrderView = this;
			var currentTarget = $(e.currentTarget);
			var date = parseInt(currentTarget.val());
			var currentOrderType = $(e.currentTarget).attr('order_type');
			var hoursOptions = $('#'+currentOrderType+'_hour_select').find('option');
			var minsOptions = $('#'+currentOrderType+'_min_select').find('option');

			$.ajax({
                url: mApiUrl + 'server_time.php',
                method: 'GET',
                success: function(res) {
                    console.log('서버시간 체크 성공');
                    var serverTime = JSON.parse(res).server_time;
                    // var serverTime = '1466214763';//토요일 오전 10시52분
                    // var serverTime = '1466128542';//금요일 오전 10시55분
                    // var serverTime = '1465867842';//화요일 오전 10시30분
                    // var serverTime = '1466303142';//일요일
                    // var serverTime = '1482632742';//성탄절 and 일요일
                    // var serverTime = '1475461542';//개천절 월요일
                    // var serverTime = '1473992742';//추석 셋째날 금요일
                    // var serverTime = '1473823542';//추석 첫째날 수요일
                    var currentDate = new Date((serverTime*1000));
                    var currentHour = currentDate.getHours();
					var currentMins = currentDate.getMinutes();

					var tomorrowDate = new Date((serverTime*1000));
					tomorrowDate.setDate(tomorrowDate.getDate() + 1);
					var isTodayOffDay = window.times.isOffDay(currentDate);
					var isTomorrowOffDay = window.times.isOffDay(tomorrowDate);
					var isTodaySaturday = window.times.isSaturday(currentDate);
					var isTomorrowSaturday = window.times.isSaturday(tomorrowDate);

					console.log('isTodayOffDay : ' + isTodayOffDay);
					console.log('isTomorrowOffDay : ' + isTomorrowOffDay);
					console.log('isTodaySaturday : ' + isTodaySaturday);
					console.log('isTomorrowSaturday : ' + isTomorrowSaturday);
					
					//현재 분이 50분 이상이면 
					if(currentMins > 50){
						currentHour = currentHour + 1
						currentMins = 0;
					};
					if(date === 0){ //today
						console.log('today');
						if(isTodayOffDay){
							hoursOptions.attr('disabled',true);
							minsOptions.attr('disabled',true);
						}else{
							hoursOptions.attr('disabled',false);
							for(var i=0; i < hoursOptions.length; i++){
								var option = hoursOptions.eq(i);
								var optionVal = parseInt(option.val());
								var disable = true;
								console.log('currentHour : ' + currentHour);
								console.log('hours optionVal : ' + optionVal);
								if(!isTodaySaturday){
									if(currentHour <= optionVal && (optionVal < WEEKDAY_BREAK_START_HOUR || optionVal >= WEEKDAY_BREAK_END_HOUR)){
										disable = false;
									}
								}else{
									if(currentHour <= optionVal && (optionVal >= SATURDAY_OPEN_HOUR && optionVal < SATURDAY_CLOSE_HOUR)){
										disable = false;
									}
								}
                                if (currentOrderType == 'delivery') {
                                    if (optionVal >= 14) {
                                        disable = true;
                                    }
                                }
								if(disable){
									// console.log('hours optionVal : ' + optionVal);
									option.attr('disabled',true);
								}else if(optionVal == currentHour){
									option.prop({selected: true});
								}
							}
							minsOptions.attr('disabled',false);
                            /*
							for(var j=0; j < minsOptions.length; j++){
								var option = minsOptions.eq(j);
								var optionVal = parseInt(option.val());
								if(optionVal < currentMins){
									console.log('min optionVal : ' + optionVal);
									option.attr('disabled',true);
								}
							}*/
						}
					}else{	//tomorrow
						if(isTomorrowOffDay){
							hoursOptions.attr('disabled',true);
							minsOptions.attr('disabled',true);
						}else{
							hoursOptions.attr('disabled',false);
							for(var i=0; i < hoursOptions.length; i++){
								var option = hoursOptions.eq(i);
								var optionVal = parseInt(option.val());
								var disable = true;
								console.log('hours optionVal : ' + optionVal);
								if(!isTomorrowSaturday){
									if(optionVal < WEEKDAY_BREAK_START_HOUR || optionVal >= WEEKDAY_BREAK_END_HOUR){
										disable = false;
									}
								}else{
									if(optionVal >= SATURDAY_OPEN_HOUR && optionVal < SATURDAY_CLOSE_HOUR){
										disable = false;
									}
								}
                                if (currentOrderType == 'delivery') {
                                    if (optionVal >= 14) {
                                        disable = true;
                                    }
                                }
								if(disable){
									console.log('hours optionVal : ' + optionVal);
									option.attr('disabled',true);
								};
							}
							minsOptions.attr('disabled',false);
						}
					}
                },
                error:function(error){
                    console.log('서버시간 체크 실패');
                    console.log('error : ' + JSON.stringify(error));
                }
            });
		},
		//시간을 변경합니다
		changedHour: function(e){
			console.log('dates-hour changedHour');

			var currentTarget = $(e.currentTarget);
			var hour = parseInt(currentTarget.val());
			var currentOrderType = $(e.currentTarget).attr('order_type');
			var minsOptions = $('#'+currentOrderType+'_min_select').find('option');
            minsOptions.attr('disabled',false);
            /*
			$.ajax({
                url: mApiUrl + 'server_time.php',
                method: 'GET',
                success: function(res) {
                    console.log('서버시간 체크 성공');
                    var serverTime = JSON.parse(res).server_time;
                    console.log('res : ' + res);
                    var currentDate = new Date((serverTime*1000));
                    var currentHour = currentDate.getHours();
                    var currentMins = currentDate.getMinutes();

                    if(hour > currentHour){
                    	minsOptions.attr('disabled',false);
                    }else{
                    	for(var j=0; j < minsOptions.length; j++){
							var option = minsOptions.eq(j);
							var optionVal = parseInt(option.val());
							if(optionVal < currentMins){
								console.log('min optionVal : ' + optionVal);
								option.attr('disabled',true);
							}
						}
                    }
					
                },
                error:function(error){
                    console.log('서버시간 체크 실패');
                    console.log('error : ' + JSON.stringify(error));
                }
            });
            */
		},
		//주소변경 창을 띄웁니다
		showChangeOrderAddr: function(e){
			$('#changeOrderAddrModal').modal({
                show: true,
            });
            $('#changeOrderAddrModal').show();
		},
		//주소를 변경합니다(주문용 일회성, 기본주소 변경)
		changeOrderAddr: function(e){
			var addrVal = parseInt($("#input_change_order_addr_sel option:selected").val());
            var addrText = $("#input_change_order_addr_sel option:selected").text();
            var addrRest = this.$el.find('#input_change_order_addr').val();
            var addr = addrText+' '+addrRest;
            if(addrVal === 0){
                swal({
                  title: "",
                  text: MES_REQUIRED_ADDR_01,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if(addrRest === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_ADDR_02,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };

            //기본 주소 변경이라면
            if($(e.currentTarget).attr('id') === 'btn_change_basic_addr'){
            	var userInfo = window.userCollection.models[0].get('user_info');
				var jwt = window.userCollection.models[0].get('jwt');
				var id = userInfo.id;

            	var data = '{'
                        +'"id":"'+id+'", '
                        +'"addr":"'+addr+'"}';
	            $.ajax({
	                type:"POST",
	                url: mApiUrl + 'change_addr.php',
	                data : data,
	                headers: {
	                    "jwt": jwt
	                },
	                success: function(res) {
	                    if(JSON.parse(res).success){
	                        // $(e.currentTarget).html('사용가능한 ID');
	                        swal({
	                          title: "",
	                          text: MES_ADDR_CHANGE_SUCCESS,
	                          confirmButtonClass: "btn-primary",
	                        },
							function(isConfirm) {
								window.orderInfoModel.set('addr', addr);
					            window.userCollection.models[0].get('user_info').addr = addr;
					           	$('.addr-display').html(addr);
					           	$('.addr-label').html('기본배송지');
					           	$('#changeOrderAddrModal').hide();
							});
	                    }else{
	                        swal({
	                          title: "",
	                          text: MES_ADDR_CHANGE_FAIL + JSON.parse(res).message,
	                          confirmButtonClass: "btn-warning",
	                        },
							function(isConfirm) {
								$('#changeOrderAddrModal').hide();
							});
	                    }
	                },
	                error:function(error){
                        if (error['status'] == 440) {
                            swal({
                                title: "",
                                text: "장시간 입력이 없어 로그아웃되었습니다",
                                confirmButtonClass: "btn-warning",
                            },
                            function(isConfirm) {
                                window.utils.deleteCookie('saladgram_user_id');
                                window.utils.deleteCookie('saladgram_jwt');
                                location.href = '/';
                            });
                        }
	                    swal({
	                      title: "",
	                      text: MES_ERROR,
	                      confirmButtonClass: "btn-warning",
	                    });
	                    console.log('error : ' + JSON.stringify(error));
	                }
	            });
            }else{
            	window.orderInfoModel.set('addr', addr);
	            window.userCollection.models[0].get('user_info').addr = addr;
	           	$('.addr-display').html(addr);
	           	$('.addr-label').html('배송지');
	           	$('#changeOrderAddrModal').hide();
            }
		},
		//주문 방법선택을 완료합니다
		orderTypeDone: function(e){
			var that = this;

			var typeTabs = $('.order-type-tabs-wrap').children('li');
			var orderType = 'undefined';
			for(var i=0; i < typeTabs.length; i++){
				if(typeTabs.eq(i).hasClass('active')){
					// orderType = i+1;
					// UI에서는 deleviery가 1번, pickup이 2번
					if(i === 0){
						orderType = ORDER_TYPE_DELIVERY;
					}else if(i === 1){
						orderType = ORDER_TYPE_PICKUP;
					}else if(i === 2){
						orderType = ORDER_TYPE_SUBSCRIBE;
					}
				};
			};
			console.log('orderType : ' + orderType);
			var currentTypeWrapId;
			if(orderType === ORDER_TYPE_SUBSCRIBE){
				swal({
                  title: "",
                  text: "정기배송 서비스 준비중입니다",
                  confirmButtonClass: "btn-warning",
                });
                return;
			};
			if(orderType === ORDER_TYPE_DELIVERY){
				currentTypeWrapId = 'delivery';
			}else if(orderType === ORDER_TYPE_PICKUP){
				currentTypeWrapId = 'pickup';
			};

			var wrap = $('#'+currentTypeWrapId);
			var orderTimeTypeWrap = wrap.find('.order-type-tab');
			var orderTimeType = 'undefined';
			for(var i=0; i < orderTimeTypeWrap.length; i++){
				if(orderTimeTypeWrap.eq(i).hasClass('active')){
					orderTimeType = i;
				};
			};

			var dates =  $('#'+currentTypeWrapId+'_date_select option:selected').val();
			var hours = $('#'+currentTypeWrapId+'_hour_select option:selected').val();
			var mins = $('#'+currentTypeWrapId+'_min_select option:selected').val();

			$.ajax({
                url: mApiUrl + 'server_time.php',
                method: 'GET',
                success: function(res) {
                    console.log('서버시간 체크 성공');
                    var serverTime = JSON.parse(res).server_time;
                    var currentDate = new Date((serverTime*1000));
                    var currentTimeStamp = currentDate.getTime() / 1000;
                    var reservationDate = new Date((serverTime*1000));

					if(typeof window.orderInfoModel === "undefined"){
						window.orderInfoModel = new OrderInfoModel();
					};

					var reservationTimeStamp = '';
					//orderTimeType : 0(바로주문), 1(예약주문)
					if(orderTimeType === 1){
						if(typeof hours === 'undefined' || typeof mins === 'undefined'){
                            var available = window.times.isReservationAvailable(orderType, parseInt(dates), currentDate);
                            if (available === true) {
                                available = "예약시간을 선택하세요";
                            }
							swal({
			                  title: "",
			                  text: available,
			                  confirmButtonClass: "btn-warning",
			                });	
			                return;			
						}else{
							dates =  parseInt(dates);
							hours = parseInt(hours);
							mins = parseInt(mins);
						}
						var day = reservationDate.getDate() + dates;
						reservationDate.setDate(day);
						reservationDate.setHours(hours);
						reservationDate.setMinutes(mins);
						reservationDate.setSeconds(0);
						reservationTimeStamp = reservationDate.getTime() / 1000;

						if(currentTimeStamp > reservationTimeStamp){
                            var available = window.times.isReservationAvailable(orderType, 0, currentDate);
                            if (available === true) {
                                available = MES_WRAN_RESERVE_TIME;
                            }
							swal({
			                  title: "",
			                  text: available,
			                  confirmButtonClass: "btn-warning",
			                });		
			                return;			
						}else{
							console.log('예약 할 수 있습니다');
						};
                        var orderTimeChecked = window.times.checkOrderTime(orderType, currentDate, reservationDate);
						if(orderTimeChecked === true){
							window.orderInfoModel.set({order_time:  currentTimeStamp });
							window.orderInfoModel.set({reservation_time: reservationTimeStamp});
                            window.orderInfoModel.set({is_tomorrow: parseInt(dates)});
                            var soldOutItems = $('#order_full_wrap').find('.soldout-div');
                            for (var i = 0; i < soldOutItems.length; i++) {
                                if (window.orderInfoModel.get('is_tomorrow')) {
                                    soldOutItems.eq(i).hide();
                                } else {
                                    soldOutItems.eq(i).show();
                                }
                            }
						}else{
							swal({
			                  title: "",
			                  text: orderTimeChecked,
			                  confirmButtonClass: "btn-warning",
			                });		
			                return;
						}
						
					}else{
                        var orderTimeChecked = window.times.checkOrderTime(orderType, currentDate, null);
						if(orderTimeChecked === true){
							window.orderInfoModel.set({order_time: 0});
							window.orderInfoModel.set({reservation_time: 0});
                            window.orderInfoModel.set({is_tomorrow: 0});
                            var soldOutItems = $('#order_full_wrap').find('.soldout-div');
                            for (var i = 0; i < soldOutItems.length; i++) {
                                soldOutItems.eq(i).show();
                            }
						}else{
							swal({
			                  title: "",
			                  text: orderTimeChecked,
			                  confirmButtonClass: "btn-warning",
			                });		
			                return;	
						}
					};

					
					var disCount = 0;
					if(orderType === ORDER_TYPE_PICKUP){
						disCount = disCount + mDiscountRateByPickup;
					}else if(orderType === ORDER_TYPE_SUBSCRIBE){
						disCount = disCount + mDiscountRateBySubscribe;
					};
					window.orderInfoModel.set({order_type: orderType});
					window.orderInfoModel.set({discount: disCount});
					
					var displayText = '바로주문';
					if(reservationTimeStamp !== ''){
						var timeText = window.utils.getDateTextByTimeStamp(reservationTimeStamp, 'full');
	                    displayText = timeText+' 배송예정';
                        if (orderType === ORDER_TYPE_PICKUP) {
                            displayText = timeText+' 픽업예정';
                        }
					}
					var orderTypeText = '배송예정시간'
					if(orderType === ORDER_TYPE_PICKUP){
						orderTypeText = '픽업예정시간';
					};

					$('#order_time_label').html(orderTypeText)
					$('#order_time_info').html(displayText);

					console.log('window.orderInfoModel : ' + JSON.stringify(window.orderInfoModel));
					that.changeStep(2);
                },
                error:function(error){
                    console.log('서버시간 체크 실패');
                    console.log('error : ' + JSON.stringify(error));
                }
            });
		},
		//sub 메뉴 클릭
		clickSubMenu:function(e){
		    // $('.menu-tab li').removeClass('active');
		    // $('.menu-tab li').removeClass('click-active');
		    var mOrderView = this;
		    var currentTarget = $(e.currentTarget);
		    var targetId = 'con'+currentTarget.attr('id');
		    var offsetTop = $('#'+targetId).offset().top; //nav 88, pager 50, sub nav 70
		    var position = offsetTop - mOrderView.$el.offset().top + mOrderView.$el.scrollTop() - 208; //nav 88, pager 50, sub nav 70
		    console.log('targetId : ' + targetId);
		    console.log('offsetTop : ' + offsetTop);
		    console.log('position : ' + position);
			mOrderView.$el.animate({
		        scrollTop: position
		    }, 500);
		    // if (!currentTarget.hasClass('click-active')) {
		    //     currentTarget.addClass('click-active');
		    // }
		},
		//scroll top
		pageScrollTop:function(e){
			console.log('pageScrollTop');
			// this.$el.scrollTop( 0 );
			var mOrderView = this;
			mOrderView.$el.animate({scrollTop:0}, 500);
		    $('.menu-tab li').removeClass('active');
		    $('.menu-tab li').first().addClass('active');
		},
		//scroll linstener
		scroll:function(e){
			// console.log('scroll!');
			var mOrderView = this;
			var currentStepNum = parseInt($('#order_full_wrap').attr('stepNum'));
			var scrollPosTop = mOrderView.$el.scrollTop();
			if(scrollPosTop > 100){
				$('.scroll-top-btn-wrap').removeClass('hidden');
			}else{
				$('.scroll-top-btn-wrap').addClass('hidden');
			}
			if(currentStepNum === 2){	//2. 메뉴 선택
				var viewPageHeight = mOrderView.$el.height() - 208;
				var fullContentsHeight = $('#step_2_navbar_contents').height();
				var scrollPosTop = mOrderView.$el.scrollTop();
				var menuLiWrap = $('#step_2_wrap .menu-tab li');
				// console.log('viewPageHeight : '  + viewPageHeight);
				// console.log('fullContentsHeight : '  + fullContentsHeight);
				// console.log('scrollPosTop : '  + scrollPosTop);

				var lastPostion = viewPageHeight + scrollPosTop;
				// console.log('lastPostion : '  + lastPostion);
				if(fullContentsHeight < lastPostion+10){
					menuLiWrap.removeClass('active');
					menuLiWrap.eq(3).addClass('active');
					return;
				};

				var conothersHeight = $('#con_others').parents('.section').height();
				var conbeveragesHeight = $('#con_beverages').parents('.section').height();
				// console.log('conothersHeight : '  + conothersHeight);
				// console.log('conbeveragesHeight : '  + conbeveragesHeight);
				// console.log('(conothersHeight+conbeveragesHeight) : '  + (conothersHeight+conbeveragesHeight));
				
				if(viewPageHeight > (conothersHeight+conbeveragesHeight) &&
				fullContentsHeight < lastPostion+15){
					menuLiWrap.removeClass('active');
					menuLiWrap.eq(2).addClass('active');
					return;
				};

				var scrollPos = mOrderView.$el.scrollTop() + 208;
				// console.log('scrollPos : '  + scrollPos);
			    menuLiWrap.each(function () {
			        var currLink = $(this);
			        var targetId = 'con'+currLink.attr('id');
			        var refElement = $('#'+targetId).parents('.section');
			        // console.log('targetId : '  + targetId);
			        // console.log('refElement.position().top : '  + refElement.position().top);
			        // console.log('refElement.height() : '  + refElement.height());
			        var heightpulstop = refElement.position().top + refElement.height();
			        // console.log('refElement.position().top + refElement.height() : '  + heightpulstop);
			        if (refElement.position().top <= scrollPos && refElement.position().top + refElement.height() > scrollPos) {
			            // $('#menu-center ul li a').removeClass("active");
			            currLink.addClass("active");
			        }
			        else{
			            currLink.removeClass("active");
					};
				});
			}else if(currentStepNum === 3){	//3. 샐러드의 아이템 선택
				var viewPageHeight = mOrderView.$el.height() - 208;
				var fullContentsHeight = $('#step_3_navbar_contents').height();
				var scrollPosTop = mOrderView.$el.scrollTop();
				var menuLiWrap = $('#step_3_wrap .menu-tab li');
				// console.log('viewPageHeight : '  + viewPageHeight);
				// console.log('fullContentsHeight : '  + fullContentsHeight);
				// console.log('scrollPosTop : '  + scrollPosTop);

				var lastPostion = viewPageHeight + scrollPosTop;
				if(fullContentsHeight < lastPostion+10){
					menuLiWrap.removeClass('active');
					menuLiWrap.eq(5).addClass('active');
					return;
				};

				var conothersItemsHeight = $('#con_others_items').parents('.section').height();
				var dressingHeight = $('#con_dressings').parents('.section').height();
				if(viewPageHeight > (conothersItemsHeight+dressingHeight) &&
				fullContentsHeight < lastPostion+15){
					menuLiWrap.removeClass('active');
					menuLiWrap.eq(4).addClass('active');
					return;
				};

				var scrollPos = mOrderView.$el.scrollTop() + 208;
				// console.log('scrollPos : '  + scrollPos);
			    menuLiWrap.each(function () {
			        var currLink = $(this);
			        var targetId = 'con'+currLink.attr('id');
			        var refElement = $('#'+targetId).parents('.section');
			        // console.log('targetId : '  + targetId);
			        // console.log('refElement.position().top : '  + refElement.position().top);
			        // console.log('refElement.height() : '  + refElement.height());
			        var heightpulstop = refElement.position().top + refElement.height();
			        // console.log('refElement.position().top + refElement.height() : '  + heightpulstop);
			        if (refElement.position().top <= scrollPos && refElement.position().top + refElement.height() > scrollPos) {
			            // $('#menu-center ul li a').removeClass("active");
			            currLink.addClass("active");
			        }
			        else{
			            currLink.removeClass("active");
					};
				});
			}else{
				return;
			}
		},

		/*****************************STEP 2**********************************/
		//샐러드 아이템 컬렉션을 만들고 선택된 샐러드의 샐러드 아이템을 넣어줍니다
		makeSaladItemsCollection : function(e){
			var that = this;
			var allSaladItemsModel = window.menuCollection.models[0].get('salad_items');
			window.currentSaladItemsCollsection = new SaladItemsCollection();
            window.currentSaladItemsCollsection.on({
			    "add" : function(e, collection, options) {
                    console.log('1');
			    	console.log('SaladItemsCollection add event' + e); 
			    	if(options.render_view){
			    		this.trigger('render_view',e);
			    	};
					// this.trigger('calculateAll');
		    	},
                "render_view" : function(e) {
                    console.log('2');
		    		console.log('SaladItemsCollection set event' + e); 
                    console.log(" saladitem set event: " + JSON.stringify(e));
			    	var itemId = e.get('item_id');
		    		var name = e.get('name');
		    		var calorie = e.get('calorie');
		    		var price = e.get('price');
		    		var image = e.get('image');

		    		that.selSaladItemListView = that.$el.find('#sel_salad_item_list_wrap');
		    		//salad item 정보 reset;
		    		$('#sel_salad_item_list_wrap').empty();

					var template = _.template(selectSaladItemListTemplate)({
									item_cid: e.cid,
									//time_label :timeLabelText,
									//time_info :timeText,
									item_name: name,
									item_cal: calorie,
									item_price: price,
									image : image,
									item_id : itemId,
									salad_items:e.get('salad_items'),
								});
					
			    	that.selSaladItemListView.prepend(template);
			    	
			    	var saladItemWraps = $('.order-item-wrap');
			    	var length = saladItemWraps.length;
			    	//salad item 선택정보 reset;
					saladItemWraps.removeClass('item-selected');
                    var saladItemsModel = e.get('salad_items');
					for(var i=0; i < saladItemWraps.length; i++){
						var saladItemType = parseInt(saladItemWraps.eq(i).attr('salad_item_type'));
						var saladItemId = parseInt(saladItemWraps.eq(i).attr('salad_item_id'));
						//salad item 양 정보 reset;
						saladItemWraps.eq(i).find('.item-amount').html('0.5');
						for(var j=0; j < saladItemsModel.length; j++){
							var containType = parseInt(saladItemsModel[j].salad_item_type);
							var containId = parseInt(saladItemsModel[j].item_id);
							if(saladItemType == containType && containId === saladItemId){
								saladItemWraps.eq(i).addClass('item-selected');
								var amount;
                                if (saladItemsModel[j].unit === "개") {
                                    amount = parseInt(saladItemsModel[j].amount_type);
                                } else if (saladItemsModel[j].unit === "g") {
                                    amount = parseInt(saladItemsModel[j].amount_type) * 0.5;
                                }
								saladItemWraps.eq(i).find('.item-amount').html("x"+amount);
							}
						};
					};
					this.trigger('calculateAll');
					//mobile toggle btn badge change
					// this.trigger('changeBadgeCount');
		    	},
		    	"changeBadgeCount": function(){
                    console.log('3');
		    		var count = window.currentSaladItemsCollsection.length;
		    		var saladNew = 'new';
		    		if(count >= 1){
		    			$('#salad_sel_results_toggle button').addClass('active_badge');
		    			$('#salad_sel_results_toggle button').addClass('animation');
		    			setTimeout(function () {
						    	$('#salad_sel_results_toggle button').removeClass('animation');
					    }, 1400);
						$('#salad_sel_results_toggle').find('.badge').html(saladNew);
		    		}else{
		    			$('#salad_sel_results_toggle button').removeClass('active_badge');
		    		};
		    	},
		    	"set" : function(e, collection, options) {
                    console.log('4');
		    		console.log('SaladItemsCollection set event' + e); 
			    	console.log(" saladitem set event: " + e); 
		    	},
		    	"change" : function(e, options) {
                    console.log('5');
		    		console.log('SaladItemsCollection change event' + e); 
		    	},
		    	"addsaladitem" : function(item) { 
                    console.log('6');
		    		console.log('addsaladitem');
		    		console.log('added item : ' + JSON.stringify(item));
		    		var addedItem = $('#selected_salad_item_clone').clone();
					addedItem.removeAttr('id');
					addedItem.attr('style','');
					addedItem.children().eq(0).attr('item_id',item.item_id);
					addedItem.children().eq(0).attr('item_type',item.salad_item_type);
					addedItem.children().eq(1).attr('src','https://'+item.image);
					addedItem.children().eq(2).html(item.name);
		    		$('#selected_salad_items_wrap').prepend(addedItem);
		    		this.trigger('changeBadgeCount');
		    	},
                "itemamountchange" : function(e, item) {
                    console.log('7');
                    console.log('itemamountchange');
                    // var itemAmoutDisplay = item.amount + item.unit;
                    var itemAmoutDisplay;
                    if (item.unit === 'g') {
                        itemAmoutDisplay = "x" + (item.amount_type * 0.5);
                    } else if (item.unit === '개') {
                        itemAmoutDisplay = item.amount_type;
                    }
                    var itemAmount = $(e.currentTarget).siblings('.item-amount').html(itemAmoutDisplay);
                    console.log("itemamountchange event: " + JSON.stringify(window.currentSaladItemsCollsection));
                    this.trigger('calculateAll');
		    	},
		    	"itemremoved" : function(saladItemId, saladItemType) {
                    console.log('8');
		    		console.log("itemremoved saladItemId: " + saladItemId);
		    		console.log("itemremoved saladItemType: " + saladItemType);
		    		var saladItemWraps = $('.salad-item-in-salad-wrap');
		    		for(var i=0; i < saladItemWraps.length; i++){
		    			var saladItemWrap = saladItemWraps.eq(i);
						var containType = parseInt(saladItemWrap.attr('salad_item_type'));
						var containId = parseInt(saladItemWrap.attr('salad_item_id'));
						if(saladItemType == containType && containId === saladItemId){
							saladItemWrap.removeClass('item-selected');
							saladItemWrap.find('item-amount').html('0.5');
							break;
						}
					};
					// $(e.currentTarget).parent().remove();
					this.trigger('changeBadgeCount');
					this.trigger('calculateAll');
		    	},	
		    	"resetCollectionView": function(e, item) {
                    console.log('9');
		    	},
		    	//현재 샐러드(currentSaladItemsCollsection)의 총 칼로리/가격을 계산합니다
		    	"calculateAll": function() {
                    console.log('10');
		    		console.log('calculateAll');
		    		var saladItemsModel = this.models[0].get('salad_items');
		    		var grossCalorie = 0;
		    		var grossPrice = 0;
		    		for(var i=0; i < saladItemsModel.length; i++){
                        var amountType = parseInt(saladItemsModel[i].amount_type);
                        var amount = parseInt(allSaladItemsModel[saladItemsModel[i]["item_id"]]["amount"+amountType]);
                        var price;
                        var calorie;
                        if (allSaladItemsModel[saladItemsModel[i]["item_id"]]["unit"] === "개") {
                            price = parseInt(saladItemsModel[i].price * amount);
                            calorie = saladItemsModel[i].calorie * amount;
                        } else if (allSaladItemsModel[saladItemsModel[i]["item_id"]]["unit"] === "g") {
                            price = parseInt(saladItemsModel[i].price * amount / 100);
                            calorie = saladItemsModel[i].calorie * amount / 100;
                        }
                        grossCalorie = grossCalorie + calorie;
                        grossPrice = grossPrice + price;
		    		};
                    grossCalorie = parseInt(grossCalorie);
		    		this.models[0].set({'calorie':grossCalorie});
		    		this.models[0].set({'price':grossPrice});
		    		$('#current_selected_salad_wrap').find('.item-cal').html(grossCalorie);
		    		$('#current_selected_salad_wrap').find('.item-price').html(grossPrice);
		    		// mOrderView.$el.find('#order_gross_cal').children().html(grossCalorie);
		    		// mOrderView.$el.find('#order_gross_price').children().html(grossPrice);
		    	}
		    });

			// return window.currentSaladItemsCollsection ;
		},

		/************************************STEP 2-1salad item select)*************************************/
		//salad를 선택시 해당salad의 items을 보여줍니다
		showSaladItems : function(saladId, itemCid){

			var mOrderView = this;
			var saladItemCid = itemCid;
			//전에 menuCollection에서 가져간 model이 변경될 경우가 있으므로 새롭게 menu의 salad items를 가져와서 보여준다
			window.menuCollection = new ItemCollection(); 
			window.menuCollection.fetch({
				success: function () {
					console.log('fetch success!'); 
                    /*
					var soups = window.menuCollection.models[0].get('soups');
					for(var i=0; i < soups.length; i++){
						soups[i].amount_type = 1;
						soups[i].amount = soups[i].amount1;
						// soups[i].price = soups[i].price * 1.5;
						// soups[i].calorie = soups[i].calorie * 1.5;
					}*/
					var saladsModel = window.menuCollection.models[0].get('salads');
					var clickedModel;
					for(var i=0; i < saladsModel.length; i++){
						if(saladsModel[i].item_id === saladId){
							clickedModel = saladsModel[i];
							break;
						}
					};

					// itemCid가 없을경우 new salad
					if(typeof itemCid === "undefined"){
						console.log('****NEW SALAD****');
						mOrderView.makeSaladItemsCollection();
						window.currentSaladItemsCollsection.add(clickedModel,{
							render_view:true 
						});
					}else{
						console.log('***********aleady has salaDitemsCollection******')
						console.log("window[collectionName] " + JSON.stringify(window[collectionName]));
						var collectionName = 'saladItemsCollsection'+itemCid;
						window.currentSaladItemsCollsection = window[collectionName];
						window.currentSaladItemsCollsection.trigger('render_view',window.currentSaladItemsCollsection.models[0]);
					};
                    window.undoSaladItemsModel = JSON.parse(JSON.stringify(window.currentSaladItemsCollsection.models[0].get('salad_items')));
                    window.undoSaladPrice = window.currentSaladItemsCollsection.models[0].get('price');
                    window.undoSaladCalorie = window.currentSaladItemsCollsection.models[0].get('calorie');
					mOrderView.changeStep(3, clickedModel.name);
					$('#navbar_salad_sel_sub').find('li').eq(0).addClass('active');
				}
			});
			
		},
		// menuMouseDown: function(e){
		// 	console.log('menuMouseDown');
		// 	var menuWrap = $(e.currentTarget);
		// 	menuWrap.addClass('mousedown_menu');
		// },
		// menuMouseLeave: function(e){
		// 	console.log('menuMouseLeave');
		// 	var menuWrap = $(e.currentTarget);
		// 	menuWrap.removeClass('mousedown_menu');
		// },
		//menu 아이템 클릭
		menuClick: function(e){
			console.log('menuClick');

			e.preventDefault();
			var menuWrap = $(e.currentTarget);
			var itemId = parseInt(menuWrap.attr("itemId"));
			var orderItmeType = menuWrap.attr("orderItmeType");
            var available = menuWrap.attr("available");
            if (available == 0 && window.orderInfoModel.get('is_tomorrow') == 0) {
                return;
            }

			console.log('itemId : ' +itemId);
			console.log('orderItmeType : ' +orderItmeType);
			if(orderItmeType !== 'soups'){	//soup는 추가버튼이후 mousedown_menu을 넣는다
				menuWrap.addClass('mousedown_menu');
			}
			switch(orderItmeType){
				case 'salads':
					this.showSaladItems(itemId);
					break;
				case 'soups':
					//samll로 초기화
					menuWrap.find('.soup-amount-minus').trigger('click');
					//다른 스프 unselcted
					menuWrap.siblings('.order-item-wrap').removeClass('item-selected');
					menuWrap.siblings('.order-item-wrap').attr('selecteditem','false');
					//클릭한 스프 selected
					if(!menuWrap.hasClass('item-selected')){
						menuWrap.addClass('item-selected');
						menuWrap.attr('selecteditem','true');
					};
					break;
				case 'others':
					this.addItem(e);
					break;
				case 'beverages':
					this.addItem(e);
					break;
			};
			setTimeout(function () {
		    	console.log('remove mousedown_menu');
				menuWrap.removeClass('mousedown_menu');
		    }, 200);
		},
		//최근 주문목록 modal show
		showBeforeOrders:function(e){
			console.log('showBeforeOrders');
			$('#beforeOrderList').modal({
	            show: true,
	        });
	        $('#beforeOrderList').show();
		},
		//최근 주문목록에서 아이템 체크
		changeBeforeOrdersCheck:function(e){
			console.log('changeBeforeOrdersCheck');
			var cId = $(e.currentTarget).attr('c_id');
			var checkeChangedItems = window.userRecentOrderCollection.get(cId);
			checkeChangedItems.set('checked', $(e.currentTarget)[0].checked);
			console.log('checkeChangedItems : ' + JSON.stringify(checkeChangedItems));
		},
		//최근주문목록에서 orderCollection으로 add
		addBeforeOrders:function(e){
			console.log('addBeforeOrders');
			var mOrderView = this;
			var beforeModels = window.userRecentOrderCollection.models;
			if(typeof window.orderItemsCollection === "undefined"){
				this.makeOrderItemsCollection();
			};
			for(var i=0; i < beforeModels.length; i++){
				if(beforeModels[i].get('checked')){
					//주문 리스트에 추가(before_order type);
					if(!window.orderItemsCollection.contains(beforeModels[i])){
						beforeModels[i].set('quantity',1);
						window.orderItemsCollection.add(beforeModels[i] , 
							{ 
								'order_itme_type' : 'salads',
								'is_recent_order' : true,
							}
						);
					};

					$('#beforeOrderList').hide();
				}
			};
		},
		//샐러드에 새로운 샐러드 아이템을 추가합니다
		addSaladItem:function(e){
			console.log('addSaladItem click');

			if($(e.currentTarget).hasClass('item-selected')){
				return;
			}
            if($(e.currentTarget).attr('available') == 0 && window.orderInfoModel.get('is_tomorrow') == 0) {
                return;
            }

			$(e.currentTarget).addClass('item-selected');
			$(e.currentTarget).find('.item-amount').html('x1');

			var saladItemType = parseInt($(e.currentTarget).attr('salad_item_type'));
			var saladItemId = parseInt($(e.currentTarget).attr('salad_item_id'));

			console.log('saladItemType : ' + saladItemType);
			console.log('saladItemId : ' + saladItemId);
			
			var currentSaladItemsModel = window.currentSaladItemsCollsection.models[0].get('salad_items');
			var saladItemsModel = window.menuCollection.models[0].get('salad_items');
			var clickedSaladItem;
			for(var i=0; i < saladItemsModel.length; i++){
				var containType = parseInt(saladItemsModel[i].salad_item_type);
				var containId = parseInt(saladItemsModel[i].item_id);
				if(saladItemType == containType && containId === saladItemId){
					clickedSaladItem = saladItemsModel[i];
					//추가 했을때 amount값 초기화
					// clickedSaladItem.amount_type = 1;
					if(clickedSaladItem.unit === 'g'){
                        clickedSaladItem.amount_type = 2;
						clickedSaladItem.amount = 100;
					}else if(clickedSaladItem.unit === '개'){
                        clickedSaladItem.amount_type = 1;
						clickedSaladItem.amount = 1;
					};

					currentSaladItemsModel.push(clickedSaladItem);
					window.currentSaladItemsCollsection.trigger('addsaladitem',clickedSaladItem)
				}
			};
			//샐러드의 총 가격/칼로리 계산
			window.currentSaladItemsCollsection.trigger('calculateAll');
		},
		//샐러드 아이템의 양을 조절합니다
		saladItemAmountChange: function(e){
			console.log('saladItemAmountChange');
			e.stopPropagation();
			e.preventDefault();
			var mOrderView = this;
			var saladItemWrap = $(e.currentTarget).parents('.salad-item-in-salad-wrap');
			var saladItemType = parseInt(saladItemWrap.attr('salad_item_type'));
			var saladItemId = parseInt(saladItemWrap.attr('salad_item_id'));
			var btnVal = parseInt($(e.currentTarget).attr('btnVal'));
			var currentSaladItemsModel = window.currentSaladItemsCollsection.models[0].get('salad_items');
			var allSaladItemsModel = window.menuCollection.models[0].get('salad_items');

			console.log('saladItemType : ' + saladItemType);
			console.log('saladItemId : ' + saladItemId);
			console.log('btnVal : ' + btnVal);
			for(var i=0; i < currentSaladItemsModel.length; i++){
				var containType = parseInt(currentSaladItemsModel[i].salad_item_type);
				var containId = parseInt(currentSaladItemsModel[i].item_id);
				if(saladItemType == containType && containId === saladItemId){
					
					var currentAmountType = parseInt(currentSaladItemsModel[i].amount_type);
					if(btnVal === 1 &&
                       (currentAmountType >= 4 || !allSaladItemsModel[currentSaladItemsModel[i]["item_id"]]["amount" + (currentAmountType + 1)])) {
                        return;
					};
					if(btnVal === -1 &&
                       (currentAmount <= 1 || !allSaladItemsModel[currentSaladItemsModel[i]["item_id"]]["amount" + (currentAmountType - 1)])) {
                        var btnSaldItemDel = $('.btn-salad-item-del');
                        for(var i=0; i < btnSaldItemDel.length; i++){
                            var btnItemId = parseInt(btnSaldItemDel.eq(i).attr('item_id'));
                            var btnItemType = parseInt(btnSaldItemDel.eq(i).attr('item_type'));
                            if(saladItemId === btnItemId && btnItemType === saladItemType){
                                // 샐러드 아이템 삭제 버튼 강제 클릭
                                btnSaldItemDel.eq(i).trigger('click');
                                return;
                            }
                        }
					}
					var currentAmount = parseInt(currentSaladItemsModel[i].amount);
					var changedAmountType = currentAmountType + btnVal;
					
					var unit = currentSaladItemsModel[i].unit;
					var changedAmount;
					if(unit === 'g'){
		    			changedAmount = parseInt(currentSaladItemsModel[i]["amount"+changedAmountType]);
					}else if(unit === '개'){
						changedAmount = (changedAmountType);
					};
					// var changedAmount = currentAmountType + btnVal;
					currentSaladItemsModel[i].amount_type = changedAmountType;
					currentSaladItemsModel[i].amount = changedAmount;
					window.currentSaladItemsCollsection.trigger('itemamountchange', e , currentSaladItemsModel[i]);
					return;
				}
			};
		},
		//샐러드 아이템을 삭제합니다
		saladItemRemove : function(e){
			console.log('saladItemRemove');
			var currentTarget = $(e.currentTarget);
			var saladItemId = parseInt(currentTarget.attr('item_id'));
			var saladItemType = parseInt(currentTarget.attr('item_type'));
			var currentSaladItemsModel = window.currentSaladItemsCollsection.models[0].get('salad_items');
			var removeIndex;
			for(var i=0; i < currentSaladItemsModel.length; i++){
				var containType = parseInt(currentSaladItemsModel[i].salad_item_type);
				var containId = parseInt(currentSaladItemsModel[i].item_id);
				if(saladItemType == containType && containId === saladItemId){
					removeIndex = i;
					break;
				}
			};
			// delete currentSaladItemsModel[removeIndex];
			currentSaladItemsModel.splice(removeIndex, 1);
			// currentSaladItemsModel.remove(removeIndex);
			window.currentSaladItemsCollsection.trigger('itemremoved', saladItemId, saladItemType);
			currentTarget.parents('.each-selected-salad-item').remove();
		},
		//샐러드 아이템 추가/변경 취소
		saladItemCancel: function(e){
            window.currentSaladItemsCollsection.models[0].set('salad_items', window.undoSaladItemsModel);
            window.currentSaladItemsCollsection.models[0].set('price', window.undoSaladPrice);
            window.currentSaladItemsCollsection.models[0].set('calorie', window.undoSaladCalorie);
			this.changeStep(2);
		},
		//샐러드 아이템을 변경을 완성합니다 
		saladItemsChangeDone: function(e){
			console.log('saladItemsChangeDone');
			console.log('JSON.stringify(window.currentSaladItemsCollsection) : '+ JSON.stringify(window.currentSaladItemsCollsection));
			if(typeof window.orderItemsCollection === "undefined"){
				this.makeOrderItemsCollection();
			};
			var doneModel = window.currentSaladItemsCollsection.models[0];
			if(doneModel.get('salad_items').length < 1){
				swal({
                  title: "",
                  text: MES_EMPTY_SALAD_ITEM,
                  confirmButtonClass: "btn-warning",
                });
                return;
			};
			if(doneModel.get('price') <= mMinPricePerSalad){
				swal({
                  title: "",
                  text: MES_MIN_SALAD_PRICE,
                  confirmButtonClass: "btn-warning",
                });
                return;
			};
			doneModel.set({'order_item_type' : 1}); //1 (샐러드)
			doneModel.set({'quantity' : 1});

			//샐러드가 처음 추가되었다면 ADD 있던 것을 수정했다면 calculateAll
			if(!window.orderItemsCollection.contains(doneModel)){
				window.orderItemsCollection.add(doneModel , 
					{ 
						'order_itme_type' : 'salads',
						'is_recent_order' : false,
					}
				);
			}else{
				window.orderItemsCollection.trigger('calculateAll');
			}
			
			window.currentSaladItemsCollsection = null;
			this.changeStep(2);
		},
		//주문방법/시간 선택으로 돌아갑니다
		changeTimeBack : function(e){
			this.changeStep(1);
		},
		//주문 컬렉션을 만들고 이벤트 등록
		makeOrderItemsCollection : function(e){

			var mOrderView = this;
			window.orderItemsCollection = new OrderItemsCollection();
			window.orderItemsCollection.on({
			    "add" : function(e, collection, options) { 
			    	// console.log("event: " + JSON.stringify(e));
			    	// console.log("collection: " + JSON.stringify(collection));
			    	// console.log("window.orderItemsCollection: " + JSON.stringify(window.orderItemsCollection));
		    		var itemId = e.get('item_id');
		    		var name = e.get('name');
		    		var calorie = e.get('calorie');
		    		var price = e.get('price');
		    		var amount = e.get('amount');
		    		var orderItmeType = options.order_itme_type;
		    		var orderItemNum;
					var oredeItemTypeText;
					var cId = e.cid;
					switch(orderItmeType){
						case 'salads':
							orderItemNum = 1;
							oredeItemTypeText = '샐러드';
							var collectionName = 'saladItemsCollsection'+cId;

							//최근 주문목록에서 가져온경우 
							if(options.is_recent_order){
								if(typeof window[collectionName] == "undefined"){
									mOrderView.makeSaladItemsCollection();
								}
								window[collectionName] = window.currentSaladItemsCollsection;
								window.currentSaladItemsCollsection.add(e,{
									render_view:false 
								});
							}

							//샐러드 아이템을 선택완료하면서 orderItemsCollection에 추가되었을 경우 window[collectionName]에 저장
							window[collectionName] = window.currentSaladItemsCollsection;
							break;
						case 'soups':
							orderItemNum = 2;
							oredeItemTypeText = '스프';
							//스프일경우 price, caloire 재계산
							price = price * (amount/100);
							calorie = calorie * (amount/100);
							break;
						case 'others':
							orderItemNum = 3;
							oredeItemTypeText = '기타';
							break;
						case 'beverages':
							orderItemNum = 4;
							oredeItemTypeText = '음료';
							break;
					};

		    		console.log("name : " + name);
		    		console.log(orderItemsCollection.toJSON());

			    	var count = window.orderItemsCollection.length;
			    	console.log("collect count : " + count);
			    	var template = _.template(orderItemListTemplate)({
						item_cid: cId,
						item_type_num: orderItemNum,
						item_type_text: oredeItemTypeText,
						item_name: name,
						item_cal: calorie,
						item_price: price,
						item_id : itemId,
						// orderItemIndex : count,
					});
					// mOrderView.orderlistView = mOrderView.$el.find('#order_list_wrap');
			    	// mOrderView.orderlistView.prepend(template);
			    	
			    	// mOrderView.$el.find('#order_info_top_wrap').after(template);
			    	mOrderView.$el.find('#order_list_wrap .order-info-contents-wrap').prepend(template);
			    	

		    		console.log(orderItemsCollection.toJSON());
					// e.unset(["name"], { silent: true });
					// e.unset(["description"], { silent: true });
					// e.unset(["thumbnail"], { silent: true });
					// e.unset(["image"], { silent: true });
					
					this.trigger("calculateAll");

					//soup overlay 제거
					if(orderItemNum === 2){
						$('#soups_wrap_'+itemId).removeClass('item-selected');
						$('#soups_wrap_'+itemId).removeAttr('selecteditem');
					};

					this.trigger("changeBadgeCount");
			    	// console.log("window.orderItemsCollection: " + JSON.stringify(window.orderItemsCollection));
		    	},
		    	"changeBadgeCount": function(){
		    		var orderCollection = window.orderItemsCollection.models;
		    		var count = 0;
		    		for(var i=0; i< orderCollection.length; i++){
		    			var orderCount = orderCollection[i].get('quantity');
		    			count = count + orderCount;
		    		};
		    		if(count >= 1){
		    			$('#order_results_toggle button').addClass('active_badge');
		    			$('#order_results_toggle button').addClass('animation');
		    			setTimeout(function () {
						    	$('#order_results_toggle button').removeClass('animation');
					    }, 1400);
						$('#order_results_toggle').find('.badge').html(count);
		    		}else{
		    			$('#order_results_toggle button').removeClass('active_badge');
		    		};
		    	},
		    	"set" : function(e, collection, options) { 
			    	console.log(" set event: " + e); 
		    	},
		    	"change" : function(e) { 
			    	console.log(" changed quantity event: " + e); 
			    	console.log(" changed quantity cid: " + e.cid); 
			    	console.log(" changed quantity : " + e.changed.quantity); 
			    	if(typeof e.changed.quantity !== "undefined"){	//수량 변경
			    		$('#order_item_'+e.cid).find('.item-quantity').html(e.changed.quantity);
			    		this.trigger("calculateAll");
			    		console.log("quantity CHANGED"); 
			    		this.trigger("changeBadgeCount");
			    	}else if(typeof e.changed.amount_type !== "undefined"){	//soup의 amount변경
			    		var itemId = e.get('item_id');
			    		var soupWrapId = 'soups_wrap_'+itemId;
			    		var soupAmountText = '';
			    		if(e.changed.amount_type === 1){
			    			soupAmountText = 'Small';
			    		}else if(e.changed.amount_type === 2){
			    			soupAmountText = 'Large';
			    		};
			    		var calorie = e.get('calorie') * (e.changed.amount/100);
			    		var price = e.get('price') * (e.changed.amount/100);
			    		$('#'+soupWrapId).find('.soup-amount').html(soupAmountText);
			    		$('#order_item_'+e.cid).find('.item-cal').html(calorie);
			    		$('#order_item_'+e.cid).find('.item-price').html(price);
			    		this.trigger("calculateAll");
			    		console.log("amount_type CHANGED"); 
			    	}
		    	},
		    	"remove": function(e, collection, options) { 
		    		console.log('removed Item');
		    		console.log('removed Item e ' + JSON.stringify(e));
		    		var itemId = e.get('item_id');
		    		var itemType = e.get('order_item_type');
					var oredeItemTypeText;
					switch(itemType){
						case 1:
							oredeItemTypeText = 'salads';
							break;
						case 2:
							oredeItemTypeText = 'soups'
							break;
						case 3:
							oredeItemTypeText = 'others'
							break;
						case 4:
							oredeItemTypeText = 'beverages'
							break;
					};
					var selecId = oredeItemTypeText + '_wrap_'+itemId;
					var removeItem = $('#'+selecId);
					removeItem.removeClass('item-selected');
					removeItem.removeAttr('selecteditem');
					removeItem.find('.menu-add').removeAttr('disabled');

		    		var removedCid = options.removedCid;
		    		var removedItem = $('#order_item_'+removedCid);
		    		removedItem.remove();

		    		//window에 저장된 collection삭제
		    		var collectionName = 'saladItemsCollsection'+itemId;
					if(typeof window[collectionName] !== "undefined"){
						// window[collectionName] = null;
						delete window[collectionName];
					};

					this.trigger('calculateAll');
					this.trigger("changeBadgeCount");
		    	},
		    	"soupamountchange": function(){
		    		console.log('soupamountchange');
		    	},
		    	//현재 orderCollection의 총 칼로리/가격을 계산합니다
		    	"calculateAll": function(){
		    		console.log('calculateAll');
		    		var grossCalorie = 0;
		    		var grossPrice = 0;
		    		if(this.models.length > 0){
		    			for(var i=0; i < this.models.length; i++){
			    			var thisModel = this.models[i];
			    			var modelQuantity = thisModel.get('quantity');
			    			var calorie = thisModel.get('calorie');
			    			var price = thisModel.get('price');
			    			var amount = thisModel.get('amount');
			    			if(thisModel.get('order_item_type') === 2){
			    				calorie = calorie * (amount/100);
			    				price = price * (amount/100);
			    			};
                            var calPriceWrap = mOrderView.$el.find('.item-name-cal-price-wrap');
                            var calPriceWrapLength = calPriceWrap.length;
                            calPriceWrap.eq(calPriceWrapLength - i - 1).children().find('.item-cal').html(parseInt(calorie));
                            calPriceWrap.eq(calPriceWrapLength - i - 1).children().find('.item-price').html(price);
			    			var modelCal = calorie * modelQuantity;
			    			var modelPrice = price * modelQuantity;
			    			grossCalorie = grossCalorie + modelCal;
			    			grossPrice = grossPrice + modelPrice;
			    		};
		    		};
		    		mOrderView.$el.find('#order_gross_cal').children().html(parseInt(grossCalorie));
		    		mOrderView.$el.find('#order_gross_price').children().html(grossPrice);
		    		window.orderInfoModel.set('total_price',grossPrice);
		    	}
		    });
		},
		//orderCollection에 아이템을 추가합니다
		addItem: function(e){

			if($(e.currentTarget).hasClass('btn-add-soup')){
				var orderItemWrap =$(e.currentTarget).parents('.order-item-wrap');
				orderItemWrap.addClass('mousedown_menu');
				setTimeout(function () {
			    	console.log('remove mousedown_menu');
					orderItemWrap.removeClass('mousedown_menu');
			    }, 200);
			}

			e.stopPropagation();
			e.preventDefault();
			var orderItemNum;
			var oredeItemTypeText;
			var itemId = parseInt($(e.currentTarget).attr("itemId"));
			var orderItmeType = $(e.currentTarget).attr("orderItmeType");
			var amountType;
			//soup일경우
			if($(e.currentTarget).hasClass('btn-add-soup')){
				var soupItemWrap = $(e.currentTarget).parents('.order-item-wrap');
				itemId = parseInt(soupItemWrap.attr("itemId"));
				orderItmeType = soupItemWrap.attr("orderItmeType");
				amountType = parseInt(soupItemWrap.attr("amountType")); //soup일경우 add할때 사용됨
			};
			var model = window.menuCollection.models[0].get(orderItmeType);
			var clickedModel;
			for(var i=0; i < model.length; i++){
				if(model[i].item_id === itemId){
					clickedModel =model[i];
					break;
				}
			};
			switch(orderItmeType){
				case 'salads':
					orderItemNum = 1;
					oredeItemTypeText = '샐러드';
					break;
				case 'soups':
					orderItemNum = 2;
					oredeItemTypeText = '스프';
					break;
				case 'others':
					orderItemNum = 3;
					oredeItemTypeText = '기타';
					break;
				case 'beverages':
					orderItemNum = 4;
					oredeItemTypeText = '음료';
					break;
			};

			clickedModel.order_item_type = orderItemNum;
			clickedModel.quantity = 1;

			if(typeof window.orderItemsCollection === "undefined"){
				this.makeOrderItemsCollection();
			};
			console.log("clickedModel: " + JSON.stringify(clickedModel));
			var itemsModel = window.orderItemsCollection.models;
			var itemCount = itemsModel.length;
			console.log('itemCount : '  + itemCount);
			if(itemCount > 0){
				var sameItem = false;
				for(var i=0; i < itemCount; i++){
					var oneItem = itemsModel[i];
					if(oneItem.get('order_item_type') === orderItemNum
						&& oneItem.get('item_id') === itemId){
						var beforeQuantity = oneItem.get('quantity');
						var quantity = beforeQuantity + 1;
						if(orderItemNum === 2){
							if(oneItem.get('amount_type') === amountType){
								oneItem.set('quantity',quantity);
								sameItem = true;
								break;
							}
						}else{
							oneItem.set('quantity',quantity);
							sameItem = true;
							break;
						}
					}
				};
				if(sameItem){
					$('#soups_wrap_'+itemId).removeClass('item-selected');
					$('#soups_wrap_'+itemId).removeAttr('selecteditem');
				}else{
					window.orderItemsCollection.add(clickedModel, { 'order_itme_type' : orderItmeType });
				}
			}else{
				window.orderItemsCollection.add(clickedModel, { 'order_itme_type' : orderItmeType });
			}
			// console.log("window.orderItemsCollection: " + JSON.stringify(window.orderItemsCollection));
		},
		//orderCollection에서 아이템을 수정합니다(샐러드아이템만 가능)
		modifyItem: function(e){
			console.log("modifyItem click: ");
			var itemCid = $(e.currentTarget).parents('.order-list-item').attr('itemCid');
			var modifyItem	= window.orderItemsCollection.get(itemCid);
			var saladId = modifyItem.get('item_id');

			this.showSaladItems(saladId, itemCid);
		},
		//orderCollection에서 아이템을 제거합니다
		removeItem: function(e){
			console.log("removeItem click: ");
			var itemCid = $(e.currentTarget).parents('.order-list-item').attr('itemCid');
			var removeItem	= window.orderItemsCollection.get(itemCid);
			window.orderItemsCollection.remove(removeItem, {'removedCid' : itemCid});
		},
		//soup아이템의 양을 변경합니다
		soupAmountChange: function(e){
			console.log("soupAmountChange");
			e.stopPropagation();
			e.preventDefault();
			var clickedBtn = $(e.currentTarget);
			var soupType = 2;	//2 = soups
			var soupId = parseInt(clickedBtn.parents('.order-item-wrap').attr("itemId"));
			var amountType = 0;
			var soupAmountText = '';
			if(clickedBtn.hasClass('soup-amount-minus')){
				amountType = 1;
				soupAmountText = 'Small';
			}else if(clickedBtn.hasClass('soup-amount-plus')){
				amountType = 2;
				soupAmountText = 'Large';
			}
			console.log('soupId : ' + soupId);
			console.log('amountType : ' + amountType);
			var soups = window.menuCollection.models[0].get('soups');
			for(var i=0; i < soups.length; i++){
				var containId = parseInt(soups[i].item_id);
				if(containId === soupId){
					var amount1 = soups[i].amount1,
						amount2 = soups[i].amount2,
						amount = 0;
					if(amountType == 1){
						amount = amount1;
					}else if(amountType = 2){
						amount = amount2;
					};
					soups[i].amount_type = amountType;
					soups[i].amount = amount;
					break;
				};
			}
			
    		var soupWrapId = 'soups_wrap_'+soupId;
    		$('#'+soupWrapId).find('.soup-amount').html(soupAmountText);
    		$('#'+soupWrapId).attr('amountType',amountType);
		},
		//주문리스트 item의 수량을 변경합니다
		itemQuantityChange : function(e){
			var itemCid = $(e.currentTarget).parents('.order-list-item').attr('itemCid');
			var btnVal = parseInt($(e.currentTarget).attr('btnVal'));
			var currentQuantity	= parseInt(window.orderItemsCollection.get(itemCid).get('quantity'));
			
			if(currentQuantity < 2 && btnVal === -1){
				return;
			}
			var plusQuantity = currentQuantity + btnVal;
			//change event trigger
			window.orderItemsCollection.get(itemCid).set({
					'quantity':plusQuantity
				},{
					add:false //add event false
				});
		},
		//아이템 선택을 완료하고 실제 주문화면으로 넘어갑니다
		orderItemsSelDone: function(e){
			console.log('orderItemsSelDone click');

			// var mOrderView = this;
			if(typeof window.orderItemsCollection === "undefined"
				|| typeof window.orderItemsCollection.models === "undefined"
				|| window.orderItemsCollection.models.length < 1){
				// console.log("window.orderItemsCollection: " + JSON.stringify(window.orderItemsCollection));
				swal({
					title: "",
					text: MES_EMPTY_ORDER_ITEM,
					confirmButtonClass: "btn-warning",
				});	
				return;
			};
			
			var timeText = $('#order_time_info').html();
			var addrText = window.userCollection.models[0].get('user_info').addr;
			var order_list = window.orderItemsCollection.models;
			var gross_price = window.orderInfoModel.get('total_price');
			var orderType = window.orderInfoModel.get('order_type');
			if(orderType === ORDER_TYPE_DELIVERY){ //delivery
				if(gross_price < mMinDeliveryPrice){
					swal({
	                  title: "",
	                  text: MES_MIN_DELIVERY_PRICE,
	                  confirmButtonClass: "btn-warning",
	                });	
	                return;
				}
                if (gross_price < 8000) {
					swal({
	                  title: "",
	                  text: "7월 이벤트 : <strike>8000원</strike> 5000원 이상 배달 가능!",
	                  confirmButtonClass: "btn-warning",
                      html: true
	                });
                }
			}
			var discount = window.orderInfoModel.get('discount');
			if(gross_price >= mFirstDiscountMinPrice){
				discount = discount + mFirstDiscountRate;
			}else if(gross_price >= mSecondDiscountMinPrice){
				discount = discount + mSecondDiscountRate;
			}else if(gross_price >= mThirdDiscountMinPrice){
				discount = discount + mThirdDiscountRate;
			};
			var discount_price = gross_price;
			if(discount > 0){
				discount_price = gross_price * ((100-discount)/100);
			}
			
			var discount_cut_price = Math.floor(discount_price/100) * 100;

			var my_point =0;
			var final_payment_price = discount_cut_price;

			// console.log('timeText : ' + timeText);
			// console.log('addrText : ' + addrText);
			// console.log('order_list : ' + JSON.stringify(order_list));
			// console.log('gross_price : ' + gross_price);
			// console.log('discount : ' + discount);
			// console.log('discount_price : ' + discount_price);
			// console.log('discount_cut_price : ' + discount_cut_price);
			// console.log('my_point : ' + my_point);
			// console.log('final_payment_price : ' + final_payment_price);

			var template = _.template(finalOrderPageTemplate)({
				order_type : orderType,
				time_text : timeText,
				addr_text : addrText,
				order_list : order_list,
				gross_price : gross_price,
				discount : discount,
				discount_price : discount_price,
				discount_cut_price : discount_cut_price,
				my_point : my_point,
				final_payment_price: final_payment_price,
			});
	    	this.$el.find('#step_4_wrap').html(template);

            if (orderType == ORDER_TYPE_PICKUP) {
                window.orderInfoModel.set({payment_type: 8});
            } else if (orderType == ORDER_TYPE_DELIVERY) {
                window.orderInfoModel.set({payment_type: 9});
            }

	    	var userId = window.userCollection.models[0].get('user_info').id;
			var jwt = window.userCollection.models[0].get('jwt');
			var userType = window.userCollection.models[0].get('user_type');

			console.log('userId : ' + userId);
			console.log('jwt : ' + jwt);
			console.log('userType : ' + userType);
			if(userType === 'member'){
				$.ajax({
	                type:"GET",
	                url: mApiUrl + 'rewards.php?id='+userId,
	                headers: {
	                    "jwt": jwt
	                },
	                success: function(res) {
	                	console.log('유저 포인트 가져오기 성공');
	                	var reward = JSON.parse(res).reward;
	                    console.log('res : ' + res);
	                    console.log('reward : ' + reward);
	                    my_point = reward;
	                    window.userCollection.models[0].get('user_info').reward = reward;
	                    $('#order_my_point').html(reward);
	                },
	                error:function(error){
                        if (error['status'] == 440) {
                            swal({
                                title: "",
                                text: "장시간 입력이 없어 로그아웃되었습니다",
                                confirmButtonClass: "btn-warning",
                            },
                            function(isConfirm) {
                                window.utils.deleteCookie('saladgram_user_id');
                                window.utils.deleteCookie('saladgram_jwt');
                                location.href = '/';
                            });
                        }
	                    console.log('유저 포인트 가져오기 실패!!!');
	                    console.log('error : ' + JSON.stringify(error));
	                }
	            });
			};

			this.changeStep(4);
		},
		/****************************************STEP 3****************************************/
		//주문 결제 타입을 온라인(카드결제)로 변경
        checkOrderTypeOnline: function(e){
            swal({
                title: "",
                text: "온라인 결제 서비스 준비중입니다",
                confirmButtonClass: "btn-warning",
            });
            return;

			$('#check_order_type_online').addClass('active');
			$('#check_order_type_offline').removeClass('active');
			var orderType = window.orderInfoModel.get('order_type');
            window.orderInfoModel.set({payment_type: 7});
		},
		//주문 결제 타입을 오프라인(직접결제)로 변경
		checkOrderTypeOffline: function(e){
			$('#check_order_type_online').removeClass('active');
			$('#check_order_type_offline').addClass('active');
			var orderType = window.orderInfoModel.get('order_type');
            if (orderType == ORDER_TYPE_PICKUP) {
                window.orderInfoModel.set({payment_type: 8});
            } else if (orderType == ORDER_TYPE_DELIVERY) {
                window.orderInfoModel.set({payment_type: 9});
            }
		},
		//주문방법 시간설정으로 돌아갑니다
		changeOrderBack: function(e){
			console.log('changeOrderBack click');
			this.changeStep(2);
		},
		//실제 데이터를 보내서 주문합니다
		doOrder: function(e){
			console.log('doOrder click');
			$(e.currentTarget).unbind();
			var mOrderView = this;
			$.ajax({
                url: mApiUrl + 'server_time.php',
                method: 'GET',
                success: function(res) {
                    console.log('서버시간 체크 성공');
                    var serverTime = JSON.parse(res).server_time;
                    console.log('res : ' + res);
                    var currentDate = new Date((serverTime*1000));
                    var currentTimeStamp = currentDate.getTime() / 1000;
                    var reservationDate = window.orderInfoModel.get('reservation_time');
                    var orderTime = window.orderInfoModel.get('order_time');

                    var myOwnPoint = parseInt(window.userCollection.models[0].get('user_info').reward);
			        var useMyPoint = parseInt($('#input_user_my_point').val());
			        if(myOwnPoint < useMyPoint){
			        	swal({
							title: "",
							text: MES_VALID_USE_REWARD,
							confirmButtonClass: "btn-warning",
						});
			        	return;
			        };
			         if(useMyPoint%100 !== 0){
			        	swal({
							title: "",
							text: MES_MIN_UNIT_USE_REWARD,
							confirmButtonClass: "btn-warning",
						});
			        	return;
			        };

			        //point를 가져오고 최종 가격을 다시한번 재 계산
			        window.orderInfoModel.set('reward_use',useMyPoint);
			        var totalPrice = window.orderInfoModel.get('total_price');
					var discount = window.orderInfoModel.get('discount');
					if(totalPrice >= mFirstDiscountMinPrice){
						discount = discount + mFirstDiscountRate;
					}else if(totalPrice >= mSecondDiscountMinPrice){
						discount = discount + mSecondDiscountRate;
					}else if(totalPrice >= mThirdDiscountMinPrice){
						discount = discount + mThirdDiscountRate;
					};
					var discountPrice = totalPrice;
					if(discount > 0){
						discountPrice = totalPrice * ((100-discount)/100);
					};
					
					var discountCutPrice = Math.floor(discountPrice/100) * 100;
					var actualPrice = discountCutPrice - useMyPoint;
                    if (actualPrice < 0) {
                        swal({
                            title: "",
                            text: "구매 금액보다 많은 적립금을 사용하실 수 없습니다",
                            confirmButtonClass: "btn-warning",
                        });
                        return;
                    } else if (actualPrice == 0) {
                        window.orderInfoModel.set({payment_type: 10});
                    }
					window.orderInfoModel.set({actual_price: actualPrice});
					var placeOrderCollection = new PlaceOrderCollection();

					// console.log("window.orderItemsCollection: " + JSON.stringify(window.orderItemsCollection));
					var itemsModel = window.orderItemsCollection.models;
					var itemCount = itemsModel.length;
					console.log('itemCount : '  + itemCount);
                    var orderName = itemsModel[0].get('name');
                    if (itemCount != 1) {
                        orderName = orderName + " 외";
                    }
                    if (typeof orderName !== "undefined") {
                        window.orderInfoModel.set({order_name: orderName});
                    }
					for(var i=0; i < itemCount; i++){
						console.log('i : ' + i);
						var oneItem = itemsModel[i];
						oneItem.unset('name', { silent: true });
						oneItem.unset('description', { silent: true });
						oneItem.unset('thumbnail', { silent: true });
						oneItem.unset('image', { silent: true });
						oneItem.unset('available', { silent: true });
						oneItem.unset('hide', { silent: true });
						oneItem.unset('amount', { silent: true });
						if(oneItem.get('order_item_type') === 1){
							console.log('this is salad');
							var saladItemsModel = oneItem.get('salad_items');
							var saladItemCount = saladItemsModel.length;
							for(var j=0; j < saladItemCount; j++){
								var oneItem = saladItemsModel[j];
								delete oneItem["name"];
								delete oneItem["amount"];
								delete oneItem["salad_item_type"];
								delete oneItem["image"];
								delete oneItem["unit"];
								delete oneItem["price"];
								delete oneItem["calorie"];
							}
						} else if (oneItem.get('order_item_type') === 2) {
                            oneItem.set("price", oneItem.get("price") * oneItem.get("amount"+oneItem.get("amount_type")) / 100);
                            oneItem.set("calorie", parseInt(oneItem.get("calorie") * oneItem.get("amount"+oneItem.get("amount_type")) / 100));
                        }
					};

					window.orderInfoModel.set({order_items: orderItemsCollection});

                    if(orderTime != 0) {
                        if(currentTimeStamp > reservationDate){
                            swal({
                                title: "",
                                text: MES_WRAN_RESERVE_TIME,
                                confirmButtonClass: "btn-warning",
                            },
                            function(){
                                mOrderView.changeStep(1);
                            });
                            return;
                        }else{
                            console.log('예약 할 수 있습니다');
                        };
                    }

					//주문 직전 orderinfomodel을 placeOrderCollection에 추가 
                    window.orderInfoModel.unset('order_id');
                    window.orderInfoModel.unset('is_tomorrow');
					placeOrderCollection.add(window.orderInfoModel);
					console.log("placeOrderCollection: " + JSON.stringify(placeOrderCollection));
					console.log("placeOrderCollection.models: " + JSON.stringify(placeOrderCollection.models));
					console.log("placeOrderCollection.models[0]: " + JSON.stringify(placeOrderCollection.models[0]));

					var jwt = window.userCollection.models[0].get('jwt');
					
                    if (window.orderInfoModel.get('payment_type') == 7) {
                        // 온라인 결제
                        $.ajax({
                            type:"POST",
                            url: mApiUrl + 'place_order_inipay.php',
                            data : JSON.stringify(placeOrderCollection.models[0]),
                            processData: false,
                            contentType :'text/plain',
                            headers: {
                                "jwt":jwt
                            },
                            success: function(res) {
                                console.log('주문하기 성공');
                                console.log('res : ' + res);
                                var resParset = JSON.parse(res);

                                //window.cancelOrder = true;
                                var order_id = resParset['order_id'];
                                window.orderInfoModel.set({order_id: order_id});
                                console.log(JSON.stringify(window.orderInfoModel));
                                //location.href="/#ordercomplete"
                                var userType = window.userCollection.models[0].get('user_type');
                                var userName = '게스트';
                                if (userType == 'member') {
                                    userName = window.userCollection.models[0].get('user_info').name;
                                }
                                var iframe = document.createElement('iframe');
                                var paymentSrc = 'https://www.saladgram.com/inicis/INIStdPayRequest.php';
                                paymentSrc = paymentSrc + '?goodname=' + window.orderInfoModel.get('order_name');
                                paymentSrc = paymentSrc + '&oid=' + window.orderInfoModel.get('order_id');
                                paymentSrc = paymentSrc + '&price=' + window.orderInfoModel.get('actual_price');
                                paymentSrc = paymentSrc + '&buyername=' + userName;
                                paymentSrc = paymentSrc + '&buyertel=' + window.orderInfoModel.get('phone');
                                paymentSrc = paymentSrc + '&buyeremail=';
                                console.log(paymentSrc);

                                //iframe.style.display = "none";
                                iframe.style.position = 'absolute';
                                iframe.style.zIndex = 1500;
                                iframe.style.top = 0;
                                iframe.style.left = 0;
                                iframe.style.height = '100%';
                                iframe.style.width = '100%';
                                iframe.style.backgroundColor = 'transparent';
                                iframe.src = paymentSrc;
                                document.body.appendChild(iframe);
                            },
                            error:function(error){
                                if (error['status'] == 440) {
                                    swal({
                                        title: "",
                                        text: "장시간 입력이 없어 로그아웃되었습니다",
                                        confirmButtonClass: "btn-warning",
                                    },
                                    function(isConfirm) {
                                        window.utils.deleteCookie('saladgram_user_id');
                                        window.utils.deleteCookie('saladgram_jwt');
                                        location.href = '/';
                                    });
                                }
                                swal({
                                    title: "",
                                    text: "주문에 실패했습니다"+ JSON.parse(res).message,
                                    confirmButtonClass: "btn-warning",
                                });
                                console.log('주문하기 실패!!!');
                                console.log('error : ' + JSON.stringify(error));
                            }
		                });

                    } else {
                        // 현장 결제
                        $.ajax({
                            type:"POST",
                            url: mApiUrl + 'place_order.php',
                            data : JSON.stringify(placeOrderCollection.models[0]),
                            processData: false,
                            contentType :'text/plain',
                            headers: {
                                "jwt":jwt
                            },
                            success: function(res) {
                                console.log('주문하기 성공');
                                console.log('res : ' + res);
                                var resParset = JSON.parse(res);
                                window.cancelOrder = true;
                                var order_id = resParset['order_id'];
                                window.orderInfoModel.set({order_id: order_id});
                                location.href="/#ordercomplete"
                            },
                            error:function(error){
                                if (error['status'] == 440) {
                                    swal({
                                        title: "",
                                        text: "장시간 입력이 없어 로그아웃되었습니다",
                                        confirmButtonClass: "btn-warning",
                                    },
                                    function(isConfirm) {
                                        window.utils.deleteCookie('saladgram_user_id');
                                        window.utils.deleteCookie('saladgram_jwt');
                                        location.href = '/';
                                    });
                                }
                                swal({
                                    title: "",
                                    text: "주문에 실패했습니다"+ JSON.parse(res).message,
                                    confirmButtonClass: "btn-warning",
                                });
                                console.log('주문하기 실패!!!');
                                console.log('error : ' + JSON.stringify(error));
                            }
                        });
                    }
                },
                error:function(error){
                	swal({
						title: "",
						text: "주문에 실패했습니다",
						confirmButtonClass: "btn-warning",
					});
                    console.log('서버시간 체크 실패');
                    console.log('error : ' + JSON.stringify(error));
                }
            });
		},
		//주문성공 confirm
		confirmOrderComplete: function(e){
			window.cancelOrder = true;
			location.href = '/';
		},
		
    });
    return orderView;
});
