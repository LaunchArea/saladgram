define(['jquery', 'underscore', 'backbone'], function($, _, Backbone) {
    var placeOrders = Backbone.Collection.extend({
        url: mApiUrl + 'place_order.php',
    });
    return placeOrders;
});
