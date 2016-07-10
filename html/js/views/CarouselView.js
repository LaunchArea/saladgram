define(['jquery', 'underscore', 'backbone','text!templates/carouselTemplate.html'
  ,'collections/ItemCollection'], 
	function($, _, Backbone, carouselTemplate, ItemCollection) {
    var carouselView = Backbone.View.extend({
        el: $("#page"),
        render: function() { 
          var that = this;
          var template = _.template(carouselTemplate);
          that.$el.html(template);
          console.log('render');
        },
    });
    return carouselView;
});