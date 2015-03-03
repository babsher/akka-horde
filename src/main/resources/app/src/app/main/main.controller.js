'use strict';

angular.module('app')
  .controller('MainCtrl', ['$scope', '$http', '$interval', '$q', '$log', 'agentSelection',
    function ($scope, $http, $interval, $q, $log, agentSelection) {
      var selected = [];
      $scope.agents = [];

      function updateCreator(i) {
        return function () {
          var name = selected[i];
          //$scope.agents[i] = {
          //    name: name + '-' + i,
          //    agentType: "testType",
          //    currentState: {state: "Start"},
          //    states: [
          //      {state: "Start", nextState: true},
          //      {state: "Next", nextState: true},
          //      {state: "Not Next", nextState: false}
          //    ]
          //  };
            return $http.get('/api/agent/' + window.btoa(name));
        }
      }

      function update() {
        var promises = [];
        for(var i = 0; i < selected.length; i++) {
          promises.push(updateCreator(i)());
        }
        $q.all(promises).then(function(data){
          $scope.agents = data.map(function(el){
            return el.data;
          });
          $log.debug($scope.agents);
        }, function(reason) {
          $log.debug('Failed: ', reason);
        });

      }

      $interval(update, 1000);

      agentSelection.addCallback(function(sel){
        console.log("Setting selection to " + selected);
        selected = sel;
        update();
      });
    }
  ]);
