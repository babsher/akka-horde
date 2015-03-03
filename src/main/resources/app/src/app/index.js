'use strict';

angular.module('app', ['ngMaterial', 'ngAnimate', 'ui.router'])
  .config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider
      .state('home', {
        url: '/',
        templateUrl: 'app/main/main.html',
        controller: 'MainCtrl'
      });

    $urlRouterProvider.otherwise('/');
  })
  .service('agentSelection', function() {
    var selected = [];
    var callbacks = [];

    function update() {
      for(var i = 0; i < callbacks.length; i++) {
        callbacks[i](selected);
      }
    }

    return {
      addCallback: function(func) {
        callbacks.push(func);
      },

      getSelected: function() {
        return selected;
      },

      setSelected: function(sel) {
        selected = sel;
        update();
      }
    }
  })
;
