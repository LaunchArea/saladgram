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
  'views/OrderCompleteView'
], function($, _, Backbone, NavigationView, CarouselView, MainView, MainModalView
  , UserView, MenuView, OurstoryView, StoreView, OrderView, OrderCompleteView) {

  var AppRouter = Backbone.Router.extend({
    routes: {
      '': 'main',
      'menu' :    'menu',
      'ourstory': 'ourstory', 
      'store':    'store', 
      'order':    'order', 
      'ordercomplete':    'ordercomplete', 
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
      ordercomplete:function() {
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
        setTimeout(function () {
          location.href = '/';
        }, 7000);
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
