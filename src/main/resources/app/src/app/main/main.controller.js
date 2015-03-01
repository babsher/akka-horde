'use strict';

angular.module('app')
  .controller('MainCtrl', ['$scope', '$http', '$interval', 'agentSelection',
    function ($scope, $http, $interval, agentSelection) {
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
          $http.get('/api/agents/agent/' + name).
            success(function (data, status, headers, config) {
              $scope.agents[i] = data;
            });
        }
      }

      function update() {
        for(var i = 0; i < selected.length; i++) {
          updateCreator(i)();
        }
      }

      $interval(update, 5000);

      agentSelection.addCallback(function(sel){
        selected = sel;
        update();
      });
    }
  ]);
