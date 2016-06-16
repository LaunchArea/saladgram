define(['jquery', 'underscore', 'backbone','views/NavigationView','text!templates/storeTemplate.html'
	,'views/MainModalView']
	, function($, _, Backbone, NavigationView, storeTemplate, MainModalView) {
    var storeView = Backbone.View.extend({
        el: $("#page"),
        render: function() {
        	if(typeof window.naviView === "undefined"){
				window.naviView = new NavigationView();
			};
			window.naviView.changeOptions('sub'); 
			window.naviView.render();

			//main modal views(login, join, find...) render
			if(typeof window.mainModalView === "undefined"){
				window.mainModalView = new MainModalView();
			}
			window.mainModalView.render();

			var that = this;
			var template = _.template(storeTemplate);
			that.$el.html(template);

			$('.main-nav-store').addClass('nav-active');
			$("#page").addClass('page-sub-store-background');
        },
    });
    return storeView;
});