define(['jquery', 'underscore', 'backbone', 'models/ItemsModel'], function($, _, Backbone, ItemsModel) {
    var menus = Backbone.Collection.extend({
        url: mApiUrl + 'menu_list.php',
        model: ItemsModel
    });
     
    return menus;
});
