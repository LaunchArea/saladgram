// Filename: router.js
define([
  'jquery',
  'underscore',
  'backbone',
  'views/NavigationView',
  'views/CarouselView',
  'views/MainView',
  'views/MainModalView',
  'views/UserView',
  'views/MenuView',
  'views/OurstoryView',
  'views/StoreView',
  'views/OrderView',
  'views/OrderCompleteView',
  'models/OrderInfoModel',
  'models/TimeModel'
], function($, _, Backbone, NavigationView, CarouselView, MainView, MainModalView
  , UserView, MenuView, OurstoryView, StoreView, OrderView, OrderCompleteView, OrderInfoModel, TimeModel) {

  var AppRouter = Backbone.Router.extend({
    routes: {
      '': 'main',
      'menu' :    'menu',
      'ourstory': 'ourstory', 
      'store':    'store', 
      'mypage':   'mypage',
      'order':    'order', 
      'ordercomplete':    'ordercomplete', 
      'ordercomplete?*queryString' : 'ordercomplete',
      '*action' : 'allaction', 
      menu:function() {
        $('#saladgram_footer').removeClass('hidden');
        if(typeof window.menuView === "undefined"){
          window.menuView = new MenuView();
        }
        window.menuView.render();
      },
      ourstory:function() {
        $('#saladgram_footer').removeClass('hidden');
        if(typeof window.ourstoryView === "undefined"){
          window.ourstoryView = new OurstoryView();
        }
        window.ourstoryView.render();
      },
      store:function() {
        $('#saladgram_footer').removeClass('hidden');
        if(typeof window.storeView === "undefined"){
          window.storeView = new StoreView();
        }
        window.storeView.render();
      },
      mypage:function() {
          if(typeof window.mainView === "undefined"){
              window.mainView = new MainView();
          }
          window.mainView.render();
      },
      order:function() {
        $('#saladgram_footer').addClass('hidden');
        if(typeof window.naviView === "undefined"){
          window.naviView = new NavigationView({
            viewname: "order"
          });
        };
        window.naviView.changeOptions('order');
        window.naviView.render();
        if(typeof window.orderView === "undefined"){
          window.orderView = new OrderView();
        }
        window.orderView.render();
      },
      ordercomplete:function(queryString) {
        if (queryString) {
             queryString = queryString.substring( queryString.indexOf('?') + 1 );
             var params = {};
             var queryParts = decodeURI(queryString).split(/&/g);
             _.each(queryParts, function(val) {
                 var parts = val.split('=');
                 if (parts.length >= 1) {
                     var val = undefined;
                     if (parts.length == 2) {
                         val = parts[1];
                     }
                     params[parts[0]] = val;
                 }
             });
             if(typeof window.orderInfoModel === "undefined"){
                 window.orderInfoModel = new OrderInfoModel();
                 window.times = new TimeModel();
             };
             window.orderInfoModel.set('order_type', params['order_type']);
             window.orderInfoModel.set('payment_type', params['payment_type']);
             window.orderInfoModel.set('addr', params['addr']);
             window.orderInfoModel.set('actual_price', params['actual_price']);
             window.orderInfoModel.set('reservation_time', params['reservation_time']);
             window.orderInfoModel.set('order_time', params['order_time']);
             window.orderInfoModel.set('order_id', params['order_id']);
             console.log(JSON.stringify(params));
        }
        console.log('ordercomplete');
        $('#saladgram_footer').addClass('hidden');
        if(typeof window.naviView === "undefined"){
          window.naviView = new NavigationView({
            viewname: "order"
          });
        };
        window.naviView.changeOptions('order');
        window.naviView.render();
        if(typeof window.orderCompleteView === "undefined"){
          window.orderCompleteView = new OrderCompleteView();
        }
        $('#btn_order_nav_cancel').addClass('hidden');
        window.orderCompleteView.render();
        /*
        setTimeout(function () {
          location.href = '/';
        }, 7000);
        */
      },
      allaction:function() {
        console.log('nopage');
      },
    }
  });
  
  


  var initialize = function(){

    var app_router = new AppRouter;
    app_router.on('route:main', function () {
      if(typeof window.mainView === "undefined"){
        window.mainView = new MainView();
      }
      window.mainView.render();
    });
    Backbone.history.start();
  };
  return { 
    initialize: initialize
  };
});
