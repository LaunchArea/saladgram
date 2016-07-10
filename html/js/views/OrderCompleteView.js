define(['jquery', 'underscore', 'backbone','views/NavigationView'
	,'text!templates/order/orderCompleteTemplate.html'	]
	, function($, _, Backbone, NavigationView, orderCompleteTemplate) {
    var orderCompleteView = Backbone.View.extend({
        el: $("#page"),
        render: function() {
			var template = _.template(orderCompleteTemplate);
			this.$el.html(template);
        },
    });
    return orderCompleteView;
});