'use strict';

angular.module('app')
  .controller('SidenavCtrl', function($scope, $mdSidenav, $log) {
    $scope.toggleLeft = function() {
      $mdSidenav('left').toggle()
        .then(function(){
          $log.debug("toggle left is done");
        });
    };
  })
  .controller('LeftCtrl', function($scope, $mdSidenav, $log) {
    $scope.close = function() {
      $mdSidenav('left').close()
        .then(function(){
          $log.debug("close LEFT is done");
        });
    };
  });
