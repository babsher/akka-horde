'use strict';

// TODO add interval https://docs.angularjs.org/api/ng/service/$interval
angular.module('app')
  .controller('LeftCtrl', function($scope, $http, $log, $interval) {
    function updateState(state) {
      console.log("Updating state to " + state);
      if("Running" === state) {
        $scope.start = true;
      } else if ("Stopped" === state) {
        $scope.start = false;
      }
    }

    $scope.start = false;
    $scope.run = function() {
      $http.put('/api/system/run', {connect: true})
        .success(function(data, status, headers, config){
          updateState(data.state.state);
        })
        .error(function(data, status, headers, config) {
          console.log("Error running");
          console.log(data);
          console.log(status);
        });
    };
    $scope.stop = function() {
      $http.put('/api/system/run')
        .success(function(data, status, headers, config){
          updateState(data.state.state);
        })
        .error(function(data, status, headers, config) {
          console.log("Error running");
          console.log(data);
          console.log(status);
        });
    };

    $interval(function() {
      $http.get('/api/system').
        success(function (data, status, headers, config) {
          updateState(data.state.state);
        });
    }, 5000);

    $interval(function() {
      $http.get('/api/agents').
        success(function (data, status, headers, config) {
          $scope.agents = data;
        });
    }, 10000);
  });
