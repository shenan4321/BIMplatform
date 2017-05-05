'use strict';

(function (app) {

    app.controller( 'websoket', ['$routeParams', '$timeout', '$http','$scope','$rootScope','tips', function ($routeParams, $timeout, $http,$scope,$rootScope,tips) {

        $http.get({
            method: 'GET',
            url: '/queryAllProject.do'
        }).then(function(res) {
            console.log(res);
        }, function(response) {

        });




    }]);

})(angular.module('websoket', []));
