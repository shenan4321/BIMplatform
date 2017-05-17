var myApp = angular.module("myApp", []);
myApp.controller('treeCtrl', function ($scope, $http) {      
    $scope.Login=function() {  
        $http.get('./model/queryModelProjectTree.do?rid='+string).success(function (data,status) {  
        	$scope.treeData = data.data.treeRoots;
        }).error(function (data,status) {  
        });  
    }  
    $scope.Login();
});

myApp.controller('pTableCtrl', function($scope){});

myApp.controller('fileCtrl', function ($scope, $http) {
	$http.get('./model/queryModelInfoByRid.do?rid='+string).success(function (data,status) {
    	$scope.data = data.data;
    }); 
	$scope.enableTag = function(item){
		$('#muiSwitch').toggleClass('checked');
		SceneJS.getScene().getNode("myEnable",function(myEnable){
			myEnable.setEnabled(!myEnable.getEnabled());
	   	});
	}
});

myApp.controller('floorCtrl', function ($scope, $http) {
	$http.get('./model/queryModelBuildingStorey.do?rid='+string).success(function (data,status) {
		console.log(data);
		$scope.data = data.data;
    }); 
	
});