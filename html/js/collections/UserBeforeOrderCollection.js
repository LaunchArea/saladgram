define(['jquery', 'underscore', 'backbone', 'models/UserModel'], function($, _, Backbone, UserModel) {

    var userBeforeOrders = Backbone.Collection.extend({
        initialize: function() {
          this.userId = 1;
        },
        url: function() {
          return mApiUrl + 'orders.php?id='+this.userId;
        },
        fetchById: function(userId, options) {
          if (userId) {
            this.userId = userId;
          };
          return this.fetch(options);
        },
    });
    return userBeforeOrders;
});