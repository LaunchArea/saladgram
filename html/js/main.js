require.config({
    paths: {
        jquery: '../assets/js/jquery-1.12.4.min',
        underscore: '../assets/js/underscore',
        backbone: '../assets/js/backbone',
        bootstrap: '../assets/js/bootstrap.min',
        templates: '../templates',
    },
    shim: {
        'backbone': {
            deps: ['underscore', 'jquery'],
            exports: 'Backbone'
        },
        'jquery': {
            exports: '$'
        },
		'bootstrap': {
            deps: ['jquery'],
            exports: '$'
        },
        'underscore': {
            exports: '_'
        },
    }
});
require(['app', ], function(App) {
    App.initialize();
});