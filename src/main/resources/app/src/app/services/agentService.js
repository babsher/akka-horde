function AgentService($http) {

  return {
    getAgents: function(callback) {
      $http.get('//api/agents')
        .success(function(data, status, bla,bal){
          callback(data)
        })
    }
  }
}

angular.module('AgentServices', [])
  .service('agentsService', ['$http', AgentService]);
