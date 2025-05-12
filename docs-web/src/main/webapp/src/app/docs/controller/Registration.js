'use strict';

/**
 * Registration controller.
 */
angular.module('docs').controller('Registration', function($scope, Restangular, $state, $translate) {
  $scope.user = {};
  $scope.alerts = [];

  /**
   * Close an alert.
   */
  $scope.closeAlert = function(index) {
    $scope.alerts.splice(index, 1);
  };

  /**
   * Register a new account request.
   */
  $scope.register = function() {
    Restangular.one('user/registration').post('', {
      username: $scope.user.username,
      password: $scope.user.password,
      email: $scope.user.email
    }).then(function() {
      // Success message
      $scope.alerts.push({
        type: 'success',
        msg: $translate.instant('registration.success')
      });

      // Reset the form
      $scope.user = {};
      $scope.registrationForm.$setPristine();

      // Redirect to login after a delay
      setTimeout(function() {
        $state.go('login');
      }, 3000);
    }, function(e) {
      // Error handling
      if (e.data.type === 'AlreadyExistingUsername') {
        $scope.alerts.push({
          type: 'danger',
          msg: $translate.instant('registration.error.already_registered')
        });
      } else if (e.data.type === 'PendingRegistrationExists') {
        $scope.alerts.push({
          type: 'warning',
          msg: $translate.instant('registration.error.pending_exists')
        });
      } else {
        $scope.alerts.push({
          type: 'danger',
          msg: $translate.instant('registration.error.server_error')
        });
      }
    });
  };
});