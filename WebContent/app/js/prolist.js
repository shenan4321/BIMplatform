'use strict';

(function (app) {

    app.controller( 'prolist', ['$routeParams', '$timeout', '$http','$scope','$rootScope','tips', function ($routeParams, $timeout, $http,$scope,$rootScope,tips) {

        $http.get('./queryAllProject.do').then(function(res) {
            $scope.feifei = res.data;
        }, function(response) {

        });

    }]);

})(angular.module('prolist', []));
