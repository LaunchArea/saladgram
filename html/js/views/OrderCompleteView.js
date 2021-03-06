define(['jquery', 'underscore', 'backbone','views/NavigationView'
	,'text!templates/order/orderCompleteTemplate.html'	]
	, function($, _, Backbone, NavigationView, orderCompleteTemplate) {
    var orderCompleteView = Backbone.View.extend({
        el: $("#page"),
        render: function() {
			var template = _.template(orderCompleteTemplate) ({
                order_type : window.orderInfoModel.get('order_type'),
                payment_type : window.orderInfoModel.get('payment_type'),
                addr : window.orderInfoModel.get('addr'),
                actual_price : window.orderInfoModel.get('actual_price'),
                reservation_time : window.orderInfoModel.get('reservation_time'),
                order_time : window.orderInfoModel.get('order_time'),
                order_id : window.orderInfoModel.get('order_id'),
            });
            console.log(JSON.stringify(window.orderInfoModel));
            window.orderInfoModel = undefined;
            window.orderItemsCollection = undefined;
            $('#navbar_order_center_title').html("주문 완료");
			this.$el.html(template);
        },
    });
    return orderCompleteView;
});
