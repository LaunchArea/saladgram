define(['jquery', 'underscore', 'backbone'
	,'text!templates/user/choiceLoginTypeTemplate.html','text!templates/user/loginTemplate.html'
	,'text!templates/user/joinTemplate.html','text!templates/user/findLoginInfoTemplate.html'
	,'text!templates/user/guestOrderLoginTemplate.html','views/NavigationView'
	,'models/UserModel','collections/UserCollection', 'collections/UserBeforeOrderCollection'
    , 'views/UserView', 'views/OrderView'], 
	function($, _, Backbone, choiceLoginTypeTemplate, loginTemplate, joinTemplate
		,findLoginInfoTemplate, guestOrderLoginTemplate, NavigationView
		,UserModel, UserCollection, UserBeforeOrderCollection, UserView, OrderView) {

	var mainModalView = Backbone.View.extend({
        el: $("#saladgramModal"),
        render: function() {
        	console.log('mainModalView render');
        	var that = this;
            $(document).ajaxError(function (e, xhr, options) {
                console.log('ERROR : ' + JSON.stringify(e));
                window.utils.deleteCookie('saladgram_user_id');
                window.utils.deleteCookie('saladgram_jwt');
                if (e.status == 401 || e.status == 403) {
                }
            });
            
            if(typeof window.modalTemplate === "undefined"){
            	//modals set add to view
    			var template1 = _.template(choiceLoginTypeTemplate);
    			that.$el.prepend(template1);
    			
    			var template2 = _.template(loginTemplate);
    			that.$el.prepend(template2);

    			var template3 = _.template(joinTemplate);
    			that.$el.prepend(template3);

    			var template4 = _.template(findLoginInfoTemplate);
    			that.$el.prepend(template4);

    			var template5 = _.template(guestOrderLoginTemplate);
    			that.$el.prepend(template5);
                window.modalTemplate = that.$el;

                // IE에서 placeholder set
                Placeholders.enable( $('#saladgramModal')[0] );
                //hide시 reset input
                $('#loginModal, #joinModal, #findLoginInfoModal, #guestOderLoginModal').on('hidden.bs.modal', function (e) {
                    $(this)
                    .find("input,textarea,select")
                        .val('')
                        .end()
                    .find("input[type=checkbox], input[type=radio]")
                        .prop("checked", "")
                        .end();
                    Placeholders.enable( $('#saladgramModal')[0] );
                });
                //enter키 처리
                $('#input_login_pw, #input_login_id').keypress(function(e){
                    if(e.keyCode==13){
                        e.preventDefault();
                        that.memberLogin();
                    }
                });

                //modal backdrop reset
                $('.modal').on('shown.bs.modal', function(e){
                    $(this).modal('handleUpdate'); //Update backdrop on modal show
                    $(this).scrollTop(0); //reset modal to top position
                });
            }
        },
        events: {
        	//show page
         	"click a#choice_login_type": "showLoginForm",
			"click a#choice_guest_type": "showGuestForm",
            "click a#btn_find_id_pwd": "showFindIdPWD",

            //login 
			"click button#btn_login": "memberLogin",

            //guest
			"click button#btn_send_guest_auth_num": "guestCheckPhone",
			"click button#btn_phone_guest_auth": "guestPhoneAuth",
            "click button#btn_guest_order": "guestLogin",

            //join
            "click button#btn_id_check": "checkId",
            "click button#btn_send_auth_num": "joinCheckPhone",
            "click button#btn_phone_auth": "joinPhoneAuth",
            "click button#btn_join_member": "join",
            "click button#login_now": "showLoginForm",

			//find
			"click button#btn_find_id": "findId",
			"click button#btn_find_pw_auth_num": "findPw",
			"click button#btn_change_pw": "changePw"

        },
        ajaxCheckPhone: function(phone, checkPhoneBtn) {
            if(phone === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_PW,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if ( phone.length < 10 || phone.length > 12 
            || phone.charAt(0) != '0' || phone.charAt(1) != '1') {
                swal({
                  title: "",
                  text: MES_REQUIRED_CORRECT_PHONE,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            $.ajax({
                url: mApiUrl + 'check_phone.php?phone='+phone,
                method: 'GET',
                success: function(res) {
                    if(JSON.parse(res).success){
                        swal({
                          title: "",
                          text: MES_PHONE_SEND_AUTH_SUCCESS,
                          confirmButtonClass: "btn-primary",
                        });
                    }else{
                        swal({
                          title: "",
                          text: JSON.parse(res).message,
                          confirmButtonClass: "btn-warning",
                        });
                    }
                },
                error:function(error){
                    swal({
                      title: "",
                      text: MES_ERROR,
                      confirmButtonClass: "btn-warning",
                    });
                    console.log('error : ' + JSON.stringify(error));
                }
            });
        },
        ajaxPhoneAuth: function(phone, authNum, authBtn) {
            if(phone === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_PHONE,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if(authNum === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_AUTH_NUM,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            $.ajax({
                url: mApiUrl + 'verify_phone.php?phone='+phone+'&key='+authNum,
                method: 'GET',
                success: function(res) {
                    console.log('폰 인증 성공');
                    console.log('res : ' + res);
                    if(JSON.parse(res).success){
                        console.log('JWT성공!!');
                        var jwtRes = JSON.parse(res).jwt;
                        if(JSON.parse(res).success){
                            if(typeof JSON.parse(res).jwt !== "undefined"){
                                swal({
                                  title: "",
                                  text: MES_PHONE_AUTH_SUCCESS,
                                  confirmButtonClass: "btn-primary",
                                });
                            }
                        }else{
                            swal({
                              title: "",
                              text: MES_PHONE_AUTH_FAIL + JSON.parse(res).message,
                              confirmButtonClass: "btn-warning",
                            });
                            return;
                        }
                        if(typeof window.userCollection === "undefined"){
                            var userModel = new UserModel();
                            window.userCollection = new UserCollection(userModel); 
                        };
                        // window.userCollection.set('jwt':jwtRes}); object로 넣으면 초기화됨 ..
                        window.userCollection.models[0].set('jwt',jwtRes);
                        console.log('window.userCollection : ' +  window.userCollection);
                        
                    }else{
                        console.log('JWT실패!!');
                        swal({
                          title: "",
                          text: MES_ERROR,
                          confirmButtonClass: "btn-warning",
                        });
                    }
                },
                error:function(error){
                    console.log('폰 인증 실패!!!');
                    console.log('error : ' + JSON.stringify(error));
                }
            });
        },
        guestCheckPhone: function(e) {
            console.log('guestCheckPhone!');
            var phone = this.$el.find('#input_guest_phone').val();
            var checkPhoneBtn = $(e.currentTarget);
            this.ajaxCheckPhone(phone, checkPhoneBtn);
        },
        guestPhoneAuth: function(e) {
            console.log('guestPhoneAuth!');
            var phone = this.$el.find('#input_guest_phone').val();
            var authNum = this.$el.find('#input_guest_phone_auth').val();
            var authBtn = $(e.currentTarget);
            this.ajaxPhoneAuth(phone, authNum, authBtn);
        },
        joinCheckPhone: function(e) {
            console.log('checkPhone!');
            var phone = this.$el.find('#input_phone').val();
            var checkPhoneBtn = $(e.currentTarget);
            this.ajaxCheckPhone(phone, checkPhoneBtn);
        },
        joinPhoneAuth: function(e) {
            console.log('phoneAuth!');
            var phone = this.$el.find('#input_phone').val();
            var authNum = this.$el.find('#input_phone_auth').val();
            var authBtn = $(e.currentTarget);
            this.ajaxPhoneAuth(phone, authNum, authBtn);
        },
        showFindIdPWD: function() {
            console.log('showFindIdPWD');
            $('#findLoginInfoModal').modal({
                show: true,
            });
            $('#findLoginInfoModal').show();
            $('#loginModal').hide();
        },
        showLoginForm: function() {
        	console.log('showLoginForm');
            $('#loginModal').modal({
                show: true,
            });
            $('#loginModal').show();
        	$('#choiceLoginTypeModal').hide();
            
        },
        showGuestForm: function() {
        	console.log('showGuestForm');
            $('#guestOderLoginModal').modal({
                show: true,
            });
            $('#guestOderLoginModal').show();
        	$('#choiceLoginTypeModal').hide();
        },
        changeStatusLogin: function(){

            console.log('changeStatusLogin');
            //main navi render
            if(typeof window.userView === "undefined"){
                window.userView = new UserView();
            }
            window.userView.render();

            if(typeof window.mainModalView === "undefined"){
                window.naviView = new NavigationView();
            }
            window.naviView.render();
        },
        memberLogin: function() {
        	console.log('memberLogin');
            var that = this;
			var id = this.$el.find('#input_login_id').val();
			var pwd = this.$el.find('#input_login_pw').val();
            console.log('id : '  + id);
            console.log('pwd : '  + pwd);
            if(id === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_ID,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if(pwd === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_PW,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            $.support.cors = true;
			$.ajax({
			    url: mApiUrl + 'sign_in.php',
			    data : "{\"id\":\""+id+"\",\"password\":\""+pwd+"\"}",
			    method: 'POST',
			    success: function(res) {
					var jwt = JSON.parse(res).jwt;
					console.log('res : ' + res);
                    that.setMemberInfo(id, jwt)
			    },
                error:function(xhr,status,error){
                    swal({
                      title: "",
                      text: MES_LOGIN_ERROR,
                      confirmButtonClass: "btn-warning",
                    });
                    console.log('error xhr : ' + JSON.stringify(xhr));
                    console.log('error status : ' + JSON.stringify(status));
                    console.log('error error : ' + JSON.stringify(error));
                    // console.log("code:"+request.status+"\n"+"message:"+request.responseText+"\n"+"error:"+error);
                }
			}).fail(function(xhr, status, error) {
                // var data = xhr.responseJSON;
                // console.log(data);
                console.log('fail xhr : ' + JSON.stringify(xhr));
                console.log('fail status : ' + JSON.stringify(status));
                console.log('fail error : ' + JSON.stringify(error));
            });
        },
        
        setMemberInfo: function(id, jwt){
            console.log('setMemberInfo');
            var that = this;
            
            window.utils.deleteCookie('saladgram_user_id');
            window.utils.deleteCookie('saladgram_jwt');
            window.utils.setCookie('saladgram_user_id',id,1);
            window.utils.setCookie('saladgram_jwt',jwt,1);

            console.log('document.cookie : ' + document.cookie);
            //user정보, 최근주문 내역, reward를 가져와서 각 collection에 저장
            if(typeof window.userCollection === "undefined"){
                var userModel = new UserModel();
                window.userCollection = new UserCollection(userModel);
            };
            var setHeader = function (xhr) {
                xhr.setRequestHeader('jwt', jwt);
            };
            window.userCollection.fetchById(
                id,{ 
                beforeSend: setHeader,
                success: function (model) {
                    console.log('fetch success!');
                    $('#loginModal').hide();
                    var resModel = model.models[0];
                    resModel.set({'jwt':jwt});
                    resModel.set({'user_type':'member'});
                    resModel.unset("success");
                    console.log('window.userCollection : ' + JSON.stringify(window.userCollection));
                    //사용자가 예전에 주문했던 리스트(사용자화면에서 최근 주문목록 조회에 쓰인다)
                    if(typeof window.userBeforeOrderCollection === "undefined"){
                        window.userBeforeOrderCollection = new UserBeforeOrderCollection(); 
                    };
                    var setHeader = function (xhr) {
                        xhr.setRequestHeader('jwt', jwt);
                    };
                    window.userBeforeOrderCollection.fetchById(
                        id,{ 
                        beforeSend: setHeader,
                        success: function (model) {
                            console.log('fetch success!');
                            // console.log('window.userBeforeOrderCollection : ' + JSON.stringify(window.userBeforeOrderCollection));
                            $.ajax({
                                type:"GET",
                                url: mApiUrl + 'rewards.php?id='+id,
                                headers: {
                                    "jwt": jwt
                                },
                                success: function(res) {
                                    console.log('유저 포인트 가져오기 성공');
                                    var test = '{"reward":5000,"rewards":[{"id":"real3334","time":1464604400,"reward_type":1,"description":"Open Event 적립금","amount":5000},{"id":"real3334","time":1464604400,"order_id":3,"reward_type":3,"description":"구매 적립금","amount":300}]}';
                                    var results = JSON.parse(test);
                                    var reward = results.reward;
                                    var rewards = results.rewards;
                                    console.log('reward : ' + reward);
                                    window.userCollection.models[0].get('user_info').reward = reward;
                                    window.userCollection.models[0].get('user_info').rewards = rewards;
                                    that.changeStatusLogin();
                                    // location.href = "#order";
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
                        }}
                    );
                    
                },
                error:function(collection, response, options) {
                    console.log('userCollection fetch ERROR');
                    console.log('collection : ' + JSON.stringify(collection));
                    console.log('response : ' + JSON.stringify(response));
                    console.log('options : ' + JSON.stringify(options));
                    if (response['status'] == 440) {
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

                    //로그인 실패시 cookie삭제
                    window.utils.deleteCookie('saladgram_user_id');
                    window.utils.deleteCookie('saladgram_jwt');
                    // alert(response.responseText);
                }
                }
            );
        },
        guestLogin: function() {
            console.log('guestLogin');
        	var phone = this.$el.find('#input_guest_phone').val();
            var addrVal = parseInt($("#input_guest_addr_sel option:selected").val());
            var addrText = $("#input_guest_addr_sel option:selected").text();
            var addrRest = this.$el.find('#input_guest_addr').val();
            var checkbox = this.$el.find('#input_guest_checkbox_privacy')[0].checked;
            var addr = addrText+' '+addrRest;

            if(phone === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_PHONE,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if ( phone.length < 10 || phone.length > 12 
            || phone.charAt(0) != '0' || phone.charAt(1) != '1') {
                swal({
                  title: "",
                  text: MES_REQUIRED_CORRECT_PHONE,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            var jwtOk = true;
            var jwt;
            if(typeof window.userCollection === "undefined"){
                jwtOk = false;
            }else{
                if(typeof window.userCollection.models[0] === "undefined"){
                    jwtOk = false;
                }else{
                    if(typeof window.userCollection.models[0].get === "undefined"){
                         jwtOk = false;
                    }else{
                        jwt = window.userCollection.models[0].get('jwt');
                        if(jwt == null || jwt === '' || jwt == 'undefined'){
                            jwtOk = false;
                        }
                    }
                }
            };
            if(!jwtOk){
                swal({
                  title: "",
                  text: MES_REQUIRED_PHONE_AUTH,
                  confirmButtonClass: "btn-warning",
                });
                return;
            }
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
            if(checkbox === false){
                swal({
                  title: "",
                  text: MES_REQUIRED_PRIVACY,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };

            console.log('addr : '  + addr);
            if(typeof window.userCollection === "undefined"){
                var userModel = new UserModel();
                window.userCollection = new UserCollection(userModel); 
            };
			window.userCollection.models[0].set('user_type','guest');
            window.userCollection.models[0].set('user_info',{
				id: 'undefined',
				name: 'undefined',
				phone: phone,
				addr: addr,
			});
            console.log('window.userCollection : '  + JSON.stringify(window.userCollection));
            $('#guestOderLoginModal').hide();
            location.href = "#order";
        },
        findId: function() {
            console.log('findId!');
            var phone = this.$el.find('#input_find_id_by_phone').val();
            if(phone === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_PHONE,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if ( phone.length < 10 || phone.length > 12 
            || phone.charAt(0) != '0' || phone.charAt(1) != '1') {
                swal({
                  title: "",
                  text: MES_REQUIRED_CORRECT_PHONE,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            $.ajax({
                type:"GET",
                url: mApiUrl + 'find_id.php?phone='+phone,
                success: function(res) {
                    if(JSON.parse(res).success){
                        swal({
                          title: "",
                          text: "아이디 : "+JSON.parse(res).message,
                          confirmButtonClass: "btn-primary",
                        });
                    }else{
                        swal({
                          title: "",
                          text: JSON.parse(res).message,
                          confirmButtonClass: "btn-warning",
                        });
                    }
                },
                error:function(error){
                    swal({
                      title: "",
                      text: MES_ERROR,
                      confirmButtonClass: "btn-warning",
                    });
                    console.log('error : ' + JSON.stringify(error));
                }
            });
        },
        findPw: function() {
            console.log('findId!');
            var id = this.$el.find('#input_find_pw_by_id').val();
            var phone = this.$el.find('#input_find_pw_by_phone').val();

            if(id === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_ID,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if(phone === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_PHONE,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if ( phone.length < 10 || phone.length > 12 
            || phone.charAt(0) != '0' || phone.charAt(1) != '1') {
                swal({
                  title: "",
                  text: MES_REQUIRED_CORRECT_PHONE,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            $.ajax({
                type:"GET",
                url: mApiUrl + 'find_password.php?id='+id+'&phone='+phone,
                success: function(res) {
                    if(JSON.parse(res).success){
                        // $(e.currentTarget).html('사용가능한 ID');
                        swal({
                          title: "",
                          text: MES_PHONE_SEND_AUTH_SUCCESS,
                          confirmButtonClass: "btn-primary",
                        });
                    }else{
                        swal({
                          title: "",
                          text: JSON.parse(res).message,
                          confirmButtonClass: "btn-warning",
                        });
                    }
                },
                error:function(error){
                    swal({
                      title: "",
                      text: MES_ERROR,
                      confirmButtonClass: "btn-warning",
                    });
                }
            });
        },
        changePw: function() {
        	console.log('changePw');
            var id = this.$el.find('#input_find_pw_by_id').val();
            var phone = this.$el.find('#input_find_pw_by_phone').val();
            var phoneAuth = this.$el.find('#input_find_pw_auth_phone').val();
            var pwd = this.$el.find('#input_find_change_pwd').val();
            var pwdc = this.$el.find('#input_find_change_pwd_confirm').val();
            if(id === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_ID,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if(phone === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_PHONE,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if ( phone.length < 10 || phone.length > 12 
            || phone.charAt(0) != '0' || phone.charAt(1) != '1') {
                swal({
                  title: "",
                  text: MES_REQUIRED_CORRECT_PHONE,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if(phoneAuth === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_AUTH_NUM,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if(pwd === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_PW,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if(pwdc === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_PW_CONFIRM,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if(pwd !== pwdc){
                swal({
                  title: "",
                  text:MES_REQUIRED_PW_SAME,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            var data = '{'
                        +'"id":"'+id+'", '
                        +'"phone":"'+phone+'", '
                        +'"password":"'+pwd+'", '
                        +'"key":"'+phoneAuth+'"}';
                        
            console.log('data : ' + data);
            $.ajax({
                type:"POST",
                url: mApiUrl + 'change_password.php',
                data : data,
                crossDomain: true,
                processData: false,
                contentType :'text/plain',
                success: function(res) {
                    if(JSON.parse(res).success){
                        swal({
                          title: "",
                          text: MES_PWD_CHANGE_SUCCESS,
                          confirmButtonClass: "btn-primary",
                        });
                    }else{
                        swal({
                          title: "",
                          text: MES_PWD_CHANGE_FAIL + JSON.parse(res).message,
                          confirmButtonClass: "btn-warning",
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
                    console.log('패스워드 변경 실패!!!');
                    console.log('error : ' + JSON.stringify(error));
                }
            });
        },
        checkId: function(e) {
            console.log('checkId!');
            var id = this.$el.find('#input_user_id').val();
            if(id === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_ID,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            $.ajax({
                url: mApiUrl + 'check_id.php?id='+id,
                method: 'GET',
                success: function(res) {
                    console.log('res : ' + res);
                    if(JSON.parse(res).success){
                        swal({
                          title: "",
                          text: MES_AVAILABLE_ID,
                          confirmButtonClass: "btn-primary",
                        });
                    }else{
                        swal({
                          title: "",
                          text: MES_UNAVAILABLE_ID + JSON.parse(res).message,
                          confirmButtonClass: "btn-warning",
                        });
                    }
                },
                error:function(error){
                    swal({
                      title: "",
                      text: MES_ERROR,
                      confirmButtonClass: "btn-warning",
                    });
                    console.log('error : ' + JSON.stringify(error));
                }
            });
        },
        join: function() {
            console.log('join!');
            var id = this.$el.find('#input_user_id').val();
            var name = this.$el.find('#input_user_name').val();
            var pwd = this.$el.find('#input_pwd').val();
            var pwdc = this.$el.find('#input_pwd_confirm').val();
            var phone = this.$el.find('#input_phone').val();
            var phoneAuth = this.$el.find('#input_phone_auth').val();
            var addrVal = parseInt($("#input_addr_sel option:selected").val());
            var addrText = $("#input_addr_sel option:selected").text();
            var addrRest = this.$el.find('#input_addr').val();
            var addr = addrText+' '+addrRest;
            var checkbox = this.$el.find('#input_checkbox_privacy')[0].checked;
            
            if(id === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_ID,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if(name === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_NAME,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if(pwd === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_PW,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if(pwdc === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_PW_CONFIRM,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if(pwd !== pwdc){
                swal({
                  title: "",
                  text:MES_REQUIRED_PW_SAME,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if(phone === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_PHONE,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if ( phone.length < 10 || phone.length > 12 
            || phone.charAt(0) != '0' || phone.charAt(1) != '1') {
                swal({
                  title: "",
                  text: MES_REQUIRED_CORRECT_PHONE,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            if(phoneAuth === ''){
                swal({
                  title: "",
                  text: MES_REQUIRED_AUTH_NUM,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            var jwtOk = true;
            var jwt;
            if(typeof window.userCollection === "undefined"){
                jwtOk = false;
            }else{
                if(typeof window.userCollection.models[0] === "undefined"){
                    jwtOk = false;
                }else{
                    if(typeof window.userCollection.models[0].get === "undefined"){
                         jwtOk = false;
                    }else{
                        jwt = window.userCollection.models[0].get('jwt');
                        if(jwt == null || jwt === '' || jwt == 'undefined'){
                            jwtOk = false;
                        }
                    }
                }
            };
            if(!jwtOk){
                swal({
                  title: "",
                  text: MES_REQUIRED_PHONE_AUTH,
                  confirmButtonClass: "btn-warning",
                });
                return;
            }
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
            if(checkbox === false){
                swal({
                  title: "",
                  text: MES_REQUIRED_PRIVACY,
                  confirmButtonClass: "btn-warning",
                });
                return;
            };
            
            console.log('jwt : ' + jwt);
            var data = '{"name":"'+name+'", '
                        +'"id":"'+id+'", '
                        +'"phone":"'+phone+'", '
                        +'"password":"'+pwd+'", '
                        +'"addr":"'+addr+'"}';
                        
            console.log('data : ' + data);
            $.ajax({
                type:"POST",
                url: mApiUrl + 'sign_up.php',
                data : data,
                crossDomain: true,
                processData: false,
                contentType :'text/plain',
                headers: {
                    "jwt":jwt
                },
                success: function(res) {
                    console.log('res : ' + res);
                    swal({
                      title: "",
                      text: MES_THANKS_FOR_JOIN,
                      // type: "warning",
                      // showCancelButton: true,
                      confirmButtonClass: "btn-primary",
                      confirmButtonText: "지금 로그인하기",
                    },
                    function(){
                        $('#loginModal').modal({
                            show: true,
                        });
                        $('#loginModal').show();
                        $('#joinModal').hide();
                    });
                },
                error:function(error){
                    swal({
                      title: "",
                      text: MES_ERROR,
                      confirmButtonClass: "btn-warning",
                    });
                    console.log('error : ' + JSON.stringify(error));
                }
            });
        }

    });
    return mainModalView;
});
