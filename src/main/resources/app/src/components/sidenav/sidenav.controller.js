'use strict';

angular.module('app')
  .controller('LeftCtrl', function($scope, $http, $log) {
    $scope.start = false;
    $http.get('/api/system').
      success(function(data, status, headers, config){
        if("Running" === data) {
          $scope.start = true;
        } else if ("Stopped" === data) {
          $scope.start = false;
        }
      });

    $http.get('/api/agents').
      success(function(data, status, headers, config){
        $scope.agents = data;
      });
  });
