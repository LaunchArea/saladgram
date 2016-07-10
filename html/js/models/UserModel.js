define(['underscore', 'backbone'], function(_, Backbone) {
    var UserModel = Backbone.Model.extend({
    	initialize: function() {
			this.on('all', function(e) { console.log(this.get('name') + " event: " + e); });
		},
    	defaults: {
			jwt: 'undefined',
			user_type : 'undefined',
			user_info : {
				id: 'undefined',
				name: 'undefined',
				phone: 'undefined',
				addr: 'undefined',
				reward: 0,
			}
		},
    });
    return UserModel;
});