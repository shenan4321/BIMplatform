var myApp = angular.module("myApp", []);
myApp.controller('treeCtrl', function ($scope, $http) {      
    $scope.Login=function() {  
        $http.get('./model/queryModelProjectTree.do?rid='+string).success(function (data,status) {  
        	console.log(data);
        	$scope.treeData = data.data.treeRoots;
        	console.log($scope.treeData);
        }).error(function (data,status) {  
        });  
    }  
    $scope.Login();
});

myApp.controller('pTableCtrl', function ($scope, $http) {
	if($scope.oid){
		alert(1);
	}
});