define(['underscore', 'backbone'], function(_, Backbone) {
    var orderItemsModel = Backbone.Model.extend({
		// idAttribute: 'type_id_item_id',
		// initialize: function() {
		// 	// // this.on('all', function(e) { console.log(this.get('name') + " event: " + e); });
		// 	// this.idAttribute = 'type_id_item_id';
		// 	// this.set({'id':this.get('order_item_type')+'-'+this.get('item_id')});
		// },
		// defaults: {
		// 	// name: 'order_items_model',
		// },
    });
    return orderItemsModel;
});