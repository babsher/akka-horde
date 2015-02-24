'use strict';

// TODO add interval https://docs.angularjs.org/api/ng/service/$interval
angular.module('app')
  .controller('LeftCtrl', function($scope, $http, $log) {
    $scope.start = false;
    $scope.run = function() {
      $http.put('/api/system/run', {connect: true})
        .error(function(data, status, headers, config) {
          console.log("Error running");
          console.log(data);
          console.log(status);
        });
    };
    $scope.stop = function() {
      $http.put('/api/system/run')
        .error(function(data, status, headers, config) {
          console.log("Error running");
          console.log(data);
          console.log(status);
        });
    };

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
