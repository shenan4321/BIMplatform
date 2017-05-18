var myApp = angular.module("myApp", []);

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

myApp.controller('treeCtrl', function ($scope, $http) {      
    $scope.Login=function() {  
        $http.get('./model/queryModelProjectTree.do?rid='+string).success(function (data,status) {  
        	$scope.treeData = data.data.treeRoots;
        }).error(function (data,status) {  
        });  
    }  
    $scope.Login();
});

myApp.controller('typeCtrl', function($scope, $http){
	
	$http.get('./model/queryBuildingCells.do?rid='+string).success(function (data,status) {  
    	$scope.typeList = data.data;
    	console.log($scope.typeList);
    }).error(function (data,status) {  
    }); 
	$scope.typeShowTag = function(item){
		item.checked = !item.checked;
		angular.forEach(item.oids, function(data,index,array){
			scene.getNode("flags"+data,function (myEnable) {
				myEnable.setEnabled(!myEnable.getEnabled());  
			});
		});
	}
});



myApp.controller('pTableCtrl', function($scope){});


myApp.controller('floorCtrl', function ($scope, $http) {
	$http.get('./model/queryModelBuildingStorey.do?rid='+string).success(function (data,status) {
		$scope.floorData = data.data;
		$scope.enableTag = function(item){
			$('#muiFloorSwitch').toggleClass('checked');
			SceneJS.getScene().getNode("myEnable",function(myEnable){
				myEnable.setEnabled(!myEnable.getEnabled());
		   	});
		}
		$scope.floorClick = function(item,obj){
			item.isActive = !item.isActive;
			angular.forEach(item.oidContains, function(data,index,array){
				scene.getNode("flags"+data,function (myEnable) {
					myEnable.setEnabled(!myEnable.getEnabled());  
				});
			});
		}
    }); 
	
});

myApp.controller('searchCtrl', function ($scope, $http) {
	$http.get('./model/queryModelBuildingStorey.do?rid='+string).success(function (data,status) {
		console.log(data);
		$scope.data = data.data;
    }); 
	
});