define(['underscore', 'backbone'], function(_, Backbone) {
    var orderInfoModel = Backbone.Model.extend({
    	initialize: function() {
			// this.on('all', function(e) { console.log(this.get('name') + " event: " + e); });
		},
		defaults: {
			// order_type: 1,
			// id: 'hantaejae',
			// total_price: 9000,
			// discount: 5,
			// reward_use : 300,
			// actual_price : 8230,
			// payment_type : 5,
			// order_time : '1464151116',
			// reservation_time : '1464151116',
			// order_items : [],
			order_type: 0,
			id: 'undefined',
			phone: 'undefined',
			addr: 'undefined',	//addr 추가 by tjhan 160611
			total_price: 0,
			discount: 0,
			reward_use : 0,
			actual_price : 0,
			payment_type : 0,
			order_time : 'undefined',
			reservation_time : 'undefined',
			order_items : [],
		},
    });
    return orderInfoModel;
});