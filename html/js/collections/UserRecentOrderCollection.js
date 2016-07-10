define(['jquery', 'underscore', 'backbone', 'models/UserModel'], function($, _, Backbone, UserModel) {

    var userRecentOrders = Backbone.Collection.extend({
        initialize: function() {
          this.userId = 1;
        },
        url: function() {
          return mApiUrl + 'recent_salads.php?id='+this.userId;
        },
        fetchById: function(userId, options) {
          if (userId) {
            this.userId = userId;
          };
          return this.fetch(options);
        },
    });
    return userRecentOrders;
});