'use strict';

/**
 * Settings registration management page controller.
 */
angular.module('docs').controller('SettingsRegistration', function($scope, $rootScope, Restangular, $translate, $dialog) {
  // Load pending registration requests
  $scope.loadPendingRequests = function() {
    Restangular.one('user/registration/pending').get().then(function(data) {
      $scope.pendingRequests = data.requests;
    });
  };

  // Load pending requests on page load
  $scope.loadPendingRequests();

  /**
   * Approve a registration request.
   */
  $scope.approve = function(request) {
    var title = $translate.instant('settings.registration.approve_confirm_title');
    var msg = $translate.instant('settings.registration.approve_confirm_message', { username: request.username });
    var btns = [
      { result: 'cancel', label: $translate.instant('cancel') },
      { result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary' }
    ];

    $dialog.messageBox(title, msg, btns, function(result) {
      if (result === 'ok') {
        Restangular.one('user/registration/' + request.id + '/approve').post().then(function() {
          // Show success message
          $scope.successApproval = true;
          
          // Refresh the list of pending requests
          $scope.loadPendingRequests();
        }, function(e) {
          // Show error message
          $scope.errorApproval = true;
        });
      }
    });
  };

  /**
   * Reject a registration request.
   */
  $scope.reject = function(request) {
    var title = $translate.instant('settings.registration.reject_confirm_title');
    var msg = $translate.instant('settings.registration.reject_confirm_message', { username: request.username });
    var btns = [
      { result: 'cancel', label: $translate.instant('cancel') },
      { result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-danger' }
    ];

    $dialog.messageBox(title, msg, btns, function(result) {
      if (result === 'ok') {
        Restangular.one('user/registration/' + request.id + '/reject').post().then(function() {
          // Show success message
          $scope.successRejection = true;
          
          // Refresh the list of pending requests
          $scope.loadPendingRequests();
        }, function(e) {
          // Show error message
          $scope.errorRejection = true;
        });
      }
    });
  };
});