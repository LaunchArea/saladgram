define(['jquery', 'underscore', 'backbone','text!templates/menuTemplate.html','views/NavigationView'
	,'collections/ItemCollection','views/MainModalView'], 
	function($, _, Backbone, menuTemplate,NavigationView,ItemCollection,MainModalView) {
    // var saladModel = new SaladModel();
    var menuView = Backbone.View.extend({
        el: $("#page"),
        render: function() {
			var that = this;
			if(typeof window.naviView === "undefined"){
				window.naviView = new NavigationView();
			};
			window.naviView.changeOptions('sub_menu'); 
			window.naviView.render();

			//main modal views(login, join, find...) render
			if(typeof window.mainModalView === "undefined"){
				window.mainModalView = new MainModalView();
			}
			window.mainModalView.render();

			if(typeof window.menuCollection === "undefined"){
				window.menuCollection = new ItemCollection(); 
				window.menuCollection.fetch({
					success: function () {
						console.log('fetch success!'); 
						console.log(JSON.stringify(window.menuCollection.models[0].get('salads')));
						var soups = window.menuCollection.models[0].get('soups');
						for(var i=0; i < soups.length; i++){
							soups[i].amount_type = 1;
							soups[i].amount = soups[i].amount1;
						}

						var models = window.menuCollection.models[0];
						that.$el.html(_.template(menuTemplate)({
							salads:models.get('salads'),
							salad_items:models.get('salad_items'),
							soups:models.get('soups'),
							others:models.get('others'),
							beverages:models.get('beverages')
						}));   
					}
				});
			}else{
				// console.log(JSON.stringify(window.menuCollection.models[0].get('salads')));
				var models = window.menuCollection.models[0];
				that.$el.html(_.template(menuTemplate)({
					salads:models.get('salads'),
					salad_items:models.get('salad_items'),
					soups:models.get('soups'),
					others:models.get('others'),
					beverages:models.get('beverages')
				}));
			}

			$('.main-nav-menu').addClass('nav-active');
        }
    });
    return menuView;
});