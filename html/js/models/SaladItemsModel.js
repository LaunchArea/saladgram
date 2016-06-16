define(['underscore', 'backbone'], function(_, Backbone) {
    var saladItemsModel = Backbone.Model.extend({
		// initialize: function() {
		// 	this.on('all', function(e) { console.log(this.get('name') + " event: " + e); });
		// },
    });
    return saladItemsModel;
});