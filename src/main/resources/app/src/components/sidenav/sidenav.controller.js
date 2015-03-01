'use strict';

angular.module('app')
  .controller('LeftCtrl', function ($scope, $http, $log, $interval, agentSelection) {
    function updateState(state) {
      console.log("Updating state to " + state);
      if ("Running" === state) {
        $scope.start = true;
      } else if ("Stopped" === state) {
        $scope.start = false;
      }
    }

    $scope.agentSelection = [];

    $scope.$watchCollection('agentSelection', function (newSelection, oldSelection) {
      var sel = [];
      for (var i = 0; i < newSelection.length; i++) {
        if (newSelection[i]) {
          sel.push($scope.agents[i].name);
        }
      }
      agentSelection.setSelected(sel);
    });

    $scope.start = false;
    $scope.run = function () {
      $http.put('/api/system/run', {connect: true})
        .success(function (data, status, headers, config) {
          updateState(data.state.state);
        })
        .error(function (data, status, headers, config) {
          console.log("Error running");
          console.log(data);
          console.log(status);
        });
    };
    $scope.stop = function () {
      $http.put('/api/system/run')
        .success(function (data, status, headers, config) {
          updateState(data.state.state);
        })
        .error(function (data, status, headers, config) {
          console.log("Error running");
          console.log(data);
          console.log(status);
        });
    };

    function updateSystem() {
      $http.get('/api/system').
        success(function (data, status, headers, config) {
          updateState(data.state.state);
        });
    }

    function updateAgents() {
      $http.get('/api/agents').
        success(function (data, status, headers, config) {
          if (data.agents.length > 0) {
            $scope.agents = data.agents;
          }
        });
    }

    //$scope.agents = [
    //  {typeName: "test", name: "test1"},
    //  {typeName: "test", name: "test2"},
    //  {typeName: "test", name: "test3"},
    //  {typeName: "test", name: "test4"},
    //  {typeName: "test", name: "test5"},
    //  {typeName: "test", name: "test6"}];

    $interval(updateSystem, 250);
    $interval(updateAgents, 250);
  });
