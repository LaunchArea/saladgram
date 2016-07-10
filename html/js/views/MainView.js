define(['jquery', 'underscore', 'backbone','text!templates/carouselTemplate.html'
	,'views/NavigationView', 'views/MainModalView'
	,'views/OrderView', 'collections/ItemCollection'], 
	function($, _, Backbone, carouselTemplate, NavigationView, MainModalView, OrderView, ItemCollection) {

	var mainView = Backbone.View.extend({
        el: $("#page"),
        pop: $("#saladgramModal"),
        render: function() {
        	
        	
        	$('[data-toggle="popover"]').popover({html : true,'placement':'top'});  

        	$('html').on('click', '[data-toggle="popover"]', function (e) {
	            console.log('data-toggle="popover"');
	        	$(e.currentTarget).toggleClass('btn-footer-wrap-active');
	        });
        	$('html').on('click', '[data-dismiss="popover"]', function (e) {
	            console.log('popoverClose');
	        	$(e.currentTarget).parents('.popover').popover('hide');
	        	$('.btn-footer-wrap').removeClass('btn-footer-wrap-active');
	        });

        	$('#saladgram_footer').removeClass('hidden'); 
			var that = this;

			//carousel view set
			var template = _.template(carouselTemplate);
			that.$el.html(template);

			//main navi render
			if(typeof window.naviView === "undefined"){
				window.naviView = new NavigationView();
			}
			window.naviView.changeOptions('main'); 
			window.naviView.render();

			//main modal views(login, join, find...) render
			if(typeof window.mainModalView === "undefined"){
				window.mainModalView = new MainModalView();
			}
			window.mainModalView.render();

			//cookie에 저장된 값이 있으면 로그인처리
			var id = window.utils.getCookie('saladgram_user_id');
			var jwt = window.utils.getCookie('saladgram_jwt');
			console.log('id : ' + id);
			console.log('jwt : ' + jwt);
			if(id !== '' && jwt !== ''){
				window.mainModalView.setMemberInfo(id,jwt);
			};

			
			//menu list미리 가져오기
			if(typeof window.menuCollection === "undefined"){
				window.menuCollection = new ItemCollection(); 
				window.menuCollection.fetch({
					success: function () {
						console.log('menu fetch success!');   
						// console.log('menu  : ' + JSON.stringify(window.menuCollection));
						var soups = window.menuCollection.models[0].get('soups');
						for(var i=0; i < soups.length; i++){
							soups[i].amount_type = 1;
							soups[i].amount = soups[i].amount1;
						};
					}
				});
			}
        },
        events: {
        	//login /join
         	"click a.btn-start-order": "startOrder",
         	"click button.btn-start-order": "startOrder",
         	"click li.btn-footer-wrap": "toggleBtnFooter"
        },
        startOrder: function() {
            console.log('startOrder');
            if(typeof window.userCollection !== "undefined"
            	&&  window.userCollection.get('jwt') !== null){
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
        toggleBtnFooter: function(e){
        	console.log('toggleBtnFooter');
        	$(e.currentTarget).toggleClass('btn-footer-wrap-active');
        }

    });
    return mainView;
});