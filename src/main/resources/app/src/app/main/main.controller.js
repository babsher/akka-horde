'use strict';

angular.module('app')
  .controller('MainCtrl', ['$scope', 'agentSelection',
    function ($scope, agentSelection) {
      agentSelection.addCallback(function(selected){
        $scope.selected = selected;
      });
    }
  ]);
