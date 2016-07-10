define(['jquery', 'underscore', 'backbone', 'models/SaladItemsModel'], function($, _, Backbone, SaladItemsModel) {

    var orders = Backbone.Collection.extend({
        model: SaladItemsModel
    });
     
    return orders;
});
