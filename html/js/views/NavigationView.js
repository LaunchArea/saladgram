define(['jquery', 'underscore', 'backbone','text!templates/mainNavigationTemplate.html'
	,'text!templates/order/orderNavigationTemplate.html']
	, function($, _, Backbone, mainNavigationTemplate, orderNavigationTemplate) {
    var navigationView = Backbone.View.extend({
      	initialize: function (options) {
    		    this.options = options;
    		},
        changeOptions: function (options) {
            this.options = {viewname:options};
        },
        el: $("#top_navigation"),
        render: function() {
          var that = this;
          var template = _.template(mainNavigationTemplate);
          if(this.options != null ){
            // that.$el.remove();
            if(that.options.viewname === 'order'){
              template = _.template(orderNavigationTemplate);
            }
          };

          that.$el.html(template);

          if(this.options != null ){
            console.log('that.options.viewname : '+ that.options.viewname);
            if(that.options.viewname === 'main'){
              $("#page").attr("class","");
              $("#page").removeClass('page-sub-background');
              $("#page").removeClass('page-sub-store-background');
              $('#top_navigation').removeClass('navbar-saladgram-sub');
              $('#saladgram_footer').removeClass('hidden');
            }else if(that.options.viewname === 'sub'){
              $("#page").addClass('page-sub-background');
              $("#page").removeClass('page-sub-store-background');
              $('#top_navigation').addClass('navbar-saladgram-sub');
              $('#btn_nav_order_wrap').addClass('active');
              $('#saladgram_footer').removeClass('hidden');
            }else if(that.options.viewname === 'sub_menu'){
              $('#top_navigation').addClass('navbar-saladgram-sub');
              $('#btn_nav_order_wrap').addClass('active');
              $('#saladgram_footer').removeClass('hidden');
            }else if(that.options.viewname === 'order'){
              $("#page").attr("class","order-page");
              $("#page").addClass('page-sub-background');
              $("#page").removeClass('page-sub-store-background');
              $('#top_navigation').addClass('navbar-saladgram-sub');
              $('#btn_nav_order_wrap').addClass('active');
              $('#saladgram_footer').addClass('hidden');
            }
          };
          if(typeof window.userCollection !== "undefined"
            &&  window.userCollection.get('jwt') !== null){
            // window.userCollection = new UserCollection(); 
            console.log('logined!');
            $('#btn_nav_mypage').removeClass('hide');
            $('#btn_nav_logout').removeClass('hide');
            $('#btn_nav_login').addClass('hide');
            $('#btn_nav_join').addClass('hide');
            
          }else{
            console.log('no login!');
            $('#btn_nav_mypage').addClass('hide');
            $('#btn_nav_logout').addClass('hide');
            $('#btn_nav_login').removeClass('hide');
            $('#btn_nav_join').removeClass('hide');
          }
        },
        events: {
          "click li#btn_nav_order_wrap": "startOrderNav",
          "click a#btn_nav_order_wrap_mo": "startOrderNav",
          "click a#btn_order_nav_cancel" : "orderCancel",
          "click a#btn_nav_logout" : "logout",
        },
        startOrderNav:function() {
            console.log('startOrder');
            $('#btn_nav_order_wrap').addClass('active');
            if(typeof window.userCollection !== "undefined"
                &&  window.userCollection.get('jwt') !== null){
                // window.userCollection = new UserCollection(); 
                console.log('logined!');
                location.href = "#order";
            }else{
                console.log('no login!');
                $('#choiceLoginTypeModal').modal({
                    show: true,
                });
                $('#choiceLoginTypeModal').show();
            }
        },
        startOrderNavMo:function(e){
          $(e.currentTarget).toggleClass('btn-mobile-order-active');
          this.startOrderNav();
        },
        orderCancel: function(e){
          //샐러드 아이템 구성에서는 주문 취소가 아닌 전 스탭으로 back
          if(parseInt($('#order_full_wrap').attr('stepnum')) === 3){
            window.orderView.saladItemCancel();
          }else{
            swal({
              title: "",
              text: "진행중이던 주문을 취소하시겠습니까?",
              confirmButtonClass: "btn-warning",
              cancelButtonText: "취소",
              showCancelButton: true,
            },
            function(){
              window.cancelOrder = true;
              location.href = '/';
            }); 
          }
        },
        logout : function(e){
          console.log('logout');
          if(typeof window.userCollection !== "undefined"){
             delete window['userCollection'];
          };
          window.utils.deleteCookie('saladgram_user_id');
          window.utils.deleteCookie('saladgram_jwt');
          location.href = '/';
        }
    });
    return navigationView;
});