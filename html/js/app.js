// Filename: app.js
define([
  'jquery', 
  'underscore', 
  'backbone',
  'router', // Request router.js
  'bootstrap',
], function($, _, Backbone, Router, Bootstrap){
  var initialize = function(){
    $.support.cors = true;
   
    // Pass in our Router module and call it's initialize function
    Router.initialize();
  };

  return { 
    initialize: initialize
  };
});
