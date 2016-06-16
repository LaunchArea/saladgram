define(['jquery', 'underscore', 'backbone', 'models/OrderItemsModel'], function($, _, Backbone, OrderItemsModel) {

    var orders = Backbone.Collection.extend({
        model: OrderItemsModel
    });
     
    return orders;
});
