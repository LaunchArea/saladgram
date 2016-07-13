define(['jquery', 'underscore', 'backbone','text!templates/user/myPageTemplate.html'
    ,'text!templates/user/editUserTemplate.html']
    ,function($, _, Backbone, myPageTemplate, editUserTemplate) {
    var userView = Backbone.View.extend({
        el: $("#saladgramModal"),
        initialize: function () {
            this.template = $('#list-template').children();
        },
        render: function() {

            //로그인된 회원정보를 가져와 view에 set
            if(typeof window.userCollection !== "undefined"){
                console.log('로그인 되었습니다');
                // console.log('JSON.stringify(window.userCollection.models[0]) : ' + JSON.stringify(window.userCollection.models[0]));
                var userInfoModal = window.userCollection.models[0].get('user_info');
                var userBeforeOrders = window.userBeforeOrderCollection.models;

                for(var i=0; i < userBeforeOrders.length; i++){
                    var reservationTime = userBeforeOrders[i].get('reservation_time');
                    var displayText = window.utils.getDateTextByTimeStamp(reservationTime,'date');

                    var displayOrderType = '';
                    var orderType = userBeforeOrders[i].get('order_type');
                    switch(orderType){
                        case ORDER_TYPE_PICKUP:
                            displayOrderType = "픽업";
                            break;
                        case ORDER_TYPE_DELIVERY:
                            displayOrderType = "배달";
                            break;
                        case ORDER_TYPE_SUBSCRIBE:
                            displayOrderType = "정기배송";
                            break;
                    };
                    displayOrderType = " 주문 번호 : " + userBeforeOrders[i].get('order_id') + " / " + displayOrderType;
                    userBeforeOrders[i].set('reservation_text',displayText);
                    userBeforeOrders[i].set('order_type_text',displayOrderType);
                };

                var rewards = window.userCollection.models[0].get('user_info').rewards;
                var gross_reward = window.userCollection.models[0].get('user_info').reward;
                for(var i=0; i < rewards.length; i++){
                    var reservationTime = rewards[i].time;
                    var displayText = window.utils.getDateTextByTimeStamp(reservationTime,'date');
                    var displayText = displayText;
                    rewards[i].time_text = displayText;
                };


                console.log('rewards : '  + rewards);
                console.log('rewards stringify : '  + JSON.stringify(rewards));
                this.$el.prepend(_.template(editUserTemplate)({
                    user_info:userInfoModal,
                }));   
                this.$el.prepend(_.template(myPageTemplate)({
                    user_info:userInfoModal,
                    user_orders:userBeforeOrders,
                    rewards:rewards,
                    gross_reward:gross_reward,
                }));

                // IE에서 placeholder set
                Placeholders.enable( $('#saladgramModal')[0] );

                //modal backdrop reset
                $('.modal').on('shown.bs.modal', function(e){
                    $(this).modal('handleUpdate'); //Update backdrop on modal show
                    $(this).scrollTop(0); //reset modal to top position
                });

                //hide시 reset input
                $('#editUserModal').on('hidden.bs.modal', function (e) {
                    $(this)
                    .find("input,textarea,select")
                        .val('')
                        .end()
                    .find("input[type=checkbox], input[type=radio]")
                        .prop("checked", "")
                        .end();
                    Placeholders.enable( $('#saladgramModal')[0] );
                });   
            }else{
                console.log('로그인되지 않았습니다');
            }
        },
        events: {
            //mypage view
            "click button#btn_show_money": "showMoney",
            "click button#btn_show_order_list": "showOrderList",
            "click a#show_edit_user_modal": "showEditUserModal",
            "click a.show-reward-detail": "showRewardDetail",
            "click a.btn-show-detail-salad-order": "showDetailSaladOrder",
            "click button.btn-detail-salad-close": "closeDetailModal",

            //edit user view
            "click button#btn_change_pwd": "changePwd",
            "click button#btn_change_addr": "changeAddr",
            "click button#btn_change_send_auth_num": "changeSendAuthNum",
            "click button#btn_change_phone": "changePhone",
        },
        changeAddr: function(e){
            console.log('changeAddr');
            e.stopPropagation();
            e.preventDefault();

            var addrVal = parseInt($("#input_change_addr_sel option:selected").val());
            var addrText = $("#input_change_addr_sel option:selected").text();
            var addrRest = this.$el.find('#input_change_addr').val();
            var addr = addrText+' '+addrRest;
            var id = window.userCollection.models[0].get('user_info').id;
            var jwt = window.userCollection.models[0].get('jwt');

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
            console.log('id : '  + id);
            console.log('addr : '  + addr);
            console.log('jwt : '  + jwt);
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
                        window.userCollection.models[0].get('user_info').addr = addr;
                        swal({
                          title: "",
                          text: MES_ADDR_CHANGE_SUCCESS,
                          confirmButtonClass: "btn-primary",
                        });
                    }else{
                        swal({
                          title: "",
                          text: MES_ADDR_CHANGE_FAIL + JSON.parse(res).message,
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
                    console.log('error : ' + JSON.stringify(error));
                }
            });
        },
        changeSendAuthNum: function(e){
            console.log('changeSendAuthNum');
            e.stopPropagation();
            e.preventDefault()
            var phone = this.$el.find('#input_change_phone').val();
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
        changePhone: function(e){
            console.log('changePhone');
            e.stopPropagation();
            e.preventDefault()
            var id = window.userCollection.models[0].get('user_info').id;
            var jwt = window.userCollection.models[0].get('jwt');
            var phone = this.$el.find('#input_change_phone').val();
            var phoneAuth = this.$el.find('#input_change_phone_auth').val();
            console.log('id : '  + id);
            console.log('phone : '  + phone);
            console.log('phoneAuth : '  + phoneAuth);
            console.log('jwt : '  + jwt);

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
            var data = '{'
                        +'"id":"'+id+'", '
                        +'"phone":"'+phone+'", '
                        +'"key":"'+phoneAuth+'"}';

            $.ajax({
                url: mApiUrl + 'verify_phone.php?phone='+phone+'&key='+phoneAuth,
                method: 'GET',
                success: function(res) {
                    console.log('폰 인증 성공');
                    console.log('res : ' + res);
                    if(JSON.parse(res).success){
                        console.log('JWT성공!!');
                        var jwtRes = JSON.parse(res).jwt;
                        console.log('login jwt : ' + jwt);
                        console.log('jwtRes : ' + jwtRes);
                        $.ajax({
                            type:"POST",
                            url: mApiUrl + 'change_phone.php',
                            data : data,
                            headers: {
                                "jwt":jwt
                            },
                            success: function(res) {
                                if(JSON.parse(res).success){
                                    window.userCollection.models[0].get('user_info').phone = phone;
                                    swal({
                                      title: "",
                                      text: MES_PHONE_CHANGE_SUCCESS,
                                      confirmButtonClass: "btn-primary",
                                    });
                                }else{
                                    swal({
                                      title: "",
                                      text: MES_PHONE_CHANGE_FAIL + JSON.parse(res).message,
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
                                console.log('휴대폰 변경 실패!!!');
                                console.log('error : ' + JSON.stringify(error));
                            }
                        });
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
        changePwd : function(){
            console.log('changePwd!!');
            var id = window.userCollection.models[0].get('user_info').id;
            var jwt = window.userCollection.models[0].get('jwt');
            var pwd = this.$el.find('#input_change_pwd').val();
            var pwdc = this.$el.find('#input_change_pwd_confirm').val();
            console.log('id : '  + id);
            console.log('pwd : '  + pwd);
            console.log('jwt : '  + jwt);
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
                        +'"password":"'+pwd+'"}';

            $.ajax({
                type:"POST",
                url: mApiUrl + 'change_password.php',
                data : data,
                headers: {
                    "jwt":jwt
                },
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
                    console.log('error : ' + JSON.stringify(error));
                }
            });
        },
        showEditUserModal : function(){
            console.log('showEditUserModal');
            $('#myPageModal').hide();
        },
        showRewardDetail : function(){
            console.log('showRewardDetail');
            $('#myPageRewardModal').modal({
                show: true,
            });
            $('#myPageRewardModal').show();
        },
        showDetailSaladOrder : function(e){
            console.log('showDetailSaladOrder');
            var currentModal = $(e.currentTarget).parents('.list-group-item').find('.detail_salad_order');
            currentModal.modal({
                show: true,
            });
            currentModal.show();
        },
        closeDetailModal : function(e){
            console.log('closeDetailModal');
            var currentModal = $(e.currentTarget).parents('.detail_salad_order');
            currentModal.modal({
                show: false,
            });
            currentModal.hide();
        }
    });
    return userView;
});

