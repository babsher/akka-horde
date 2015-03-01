'use strict';

angular.module('app')
  .controller('MainCtrl', ['$scope', '$http', '$interval', '$animate', 'agentSelection',
    function ($scope, $http, $interval, $animate, agentSelection) {
      var selected = [];
      $scope.agents = [];

      function updateCreator(i) {
        return function () {
          var name = selected[i];
          //$scope.agents[i] = {
          //  name: "test" + i,
          //  agentType: "testType",
          //  currentState: {state: "Start"},
          //  states: [
          //    {state: "Start", nextState: true},
          //    {state: "Next", nextState: true},
          //    {state: "Not Next", nextState: false}
          //  ]
          //};
          $http.get('/api/agent/' + window.btoa(name)).
            success(function (data, status, headers, config) {
              $animate.enabled(false);
              $scope.agents[i] = data;
              $animate.enabled(true);
            });
        }
      }

      function update() {
        for(var i = 0; i < selected.length; i++) {
          updateCreator(i)();
        }
      }

      $interval(update, 250);

      agentSelection.addCallback(function(sel){
        selected = sel;
        update();
      });
    }
  ]);
