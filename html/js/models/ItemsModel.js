define(['underscore', 'backbone'], function(_, Backbone) {
    var itemsModel = Backbone.Model.extend({
    	initialize: function() {
			// this.on('all', function(e) { console.log(this.get('name') + " event: " + e); });
		},
		defaults: {
			salads:[{
				item_id: 'undefined',
			}],
			salad_items:[{
				item_id: 'undefined',
			}],
			others: [{
				item_id: 'undefined',
			}],
			soups: [{
				item_id: 'undefined',
			}],
			beverages: [{
				item_id: 'undefined',
			}],
		},
    });
    return itemsModel;
});