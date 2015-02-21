'use strict';

angular.module('app')
  .controller('MainCtrl', ['$scope', 'AgentServices',
    function ($scope, agentService) {
      agentService.getAgents(function(agents){
        $scope.agents = agents;
      });
    }
  ]);
