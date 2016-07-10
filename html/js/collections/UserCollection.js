define(['jquery', 'underscore', 'backbone', 'models/UserModel'], function($, _, Backbone, UserModel) {

    var user = Backbone.Collection.extend({
        initialize: function() {
          this.userId = 1; 
        },
        url: function() {
          return mApiUrl + 'user_info.php?id='+this.userId;
        },
        fetchById: function(userId, options) {
          if (userId) {
            this.userId = userId;
          };
          return this.fetch(options);
        },
        model: UserModel
    });
    return user;
});