define(['jquery', 'underscore', 'backbone','views/NavigationView','text!templates/ourstoryTemplate.html'
	,'views/MainModalView']
	, function($, _, Backbone, NavigationView, ourstoryTemplate, MainModalView) {
    var ourstoryView = Backbone.View.extend({
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
			var template = _.template(ourstoryTemplate);
			that.$el.html(template);

			$('.main-nav-ourstory').addClass('nav-active');
        },
    });
    return ourstoryView;
});