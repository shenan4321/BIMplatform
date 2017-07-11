var myApp = angular.module("myApp", []);



myApp.controller('myAppCtrl', function ($scope, $http) {
	
	$scope.menuClick = function(param){
		$scope[param]($scope, $http);
	}
	
	
	$scope.fileCtrl = function($scope, $http){
		$http.get('./model/queryModelInfoByRid.do?rid='+string).success(function (data,status) {
	    	$scope.data = data.data;
	    }); 
		$scope.enableTag = function(item){
			$('#muiSwitch').toggleClass('checked');
			$('#myCanvas').toggle();
		}
	}
	
	$scope.treeCtrl = function($scope, $http){
		$http.get('./model/queryModelProjectTree.do?rid='+string).success(function (data,status) {  
	    	$scope.treeData = data.data.treeRoots;
	    }).error(function (data,status) {  
	    
	    });  
	    
		$scope.appnedChoose = function(item){
			item.show=true;
		}
		
		
	    $scope.setOidShow = function(item){
	    	console.log(item);
	    	if($scope.treeClick){
	    		$scope.treeClick.checked = !$scope.treeClick.checked
	    	}
	    	$scope.treeClick = item;
	    	item.checked = !item.checked;
	    	var itemBox = xeogl.scene.components['ifc'+item.oid];
			cameraFlightAnimation.flyTo({
                aabb: itemBox.worldBoundary.aabb
            });
            if(hisPick.name){
				xeogl.scene.components[hisPick.name].material.emissive =  xeogl.scene.components[hisPick.name].material.baseColor;
            }
            itemBox.material.emissive = new Float32Array([0, 1, 0]);
            hisPick = {name:'ifc'+item.oid};
            if(window.tt){
            	var pTableScope= $('#pTable').scope();
                pTableScope.oid = item.oid ;
                $.ajax({
              	  url:'./model/queryProperty.do',
              	  type:'GET',
              	  data:{oid:'ifc'+item.oid,rid:string}
                }).done(function(data){
              	  pTableScope.list = data.data; 
              	  $('#pTable').scope().$apply();
                })
            }
	    }
	}
	
	
	$scope.typeCtrl = function($scope, $http){
		$http.get('./model/queryBuildingCells.do?rid='+string).success(function (data,status) {  
	    	$scope.typeList = data.data;
	    }).error(function (data,status) {  
	    }); 
		$scope.typeShowTag = function(item){
			item.checked = !item.checked;
			angular.forEach(item.oids, function(data,index,array){
				if(!xeogl.scene.components['ifc'+data].visibility.showFloor){
					xeogl.scene.components['ifc'+data].visibility.visible = item.checked;
					xeogl.scene.components['ifc'+data].visibility.showType = !item.checked;//默认此参数是空，所以某类显示出来的时候
				}
			});
		}
		/*$scope.selectedPlaneBoxList = [];
		$scope.selectedPlaneBoxEventList = [];
		$scope.typeShowOpearate = function(item){
			if($scope.selectedPlaneBoxList.indexOf(item.name)==-1){
					var MenuType = function () {
			            this.message = "Directional light";
			            this["alpha.a"] = 0.6;
			            this["color.r"] = Ifc.Constants.materials['Ifc'+item.name].r;
			            this["color.g"] = Ifc.Constants.materials['Ifc'+item.name].g;
			            this["color.b"] = Ifc.Constants.materials['Ifc'+item.name].b;
			            var self = this;
			            var update = function () {
			            	angular.forEach(item.oids, function(data,index,array){
			         			scene.getNode(data+'geometry',function(mt){
			         				mt.setColor({r:self["color.r"],g:self["color.g"],b:self["color.b"]});
			         				mt.setAlpha(self["alpha.a"]);
			         			})
			            	});
			                requestAnimationFrame(update);
			            };
			            update();
			        };
			        var $closeButton = $('.dg .close-button');
			        if($closeButton.length==1){
			        	$closeButton.css('position','relative').after($closeButton.clone().css('position','relative').addClass('cover-button').html('还原'));
			        }
					var menuType = new MenuType();
					$scope.selectedPlaneBoxList.push(item.name);	
					var menubox = gui.addFolder(item.name+'类');
			        menubox.add(menuType, 'alpha.a', 0.0, 1.0);
			        menubox.add(menuType, 'color.r', 0.0, 1.0);
			        menubox.add(menuType, 'color.g', 0.0, 1.0);
			        menubox.add(menuType, 'color.b', 0.0, 1.0);
			        menubox.open();
			}
			
		}*/
	}
	
	$scope.pTableCtrl = function($scope, $http){
		
	}
	
	$scope.floorCtrl = function($scope, $http){
		$http.get('./model/queryModelBuildingStorey.do?rid='+string).success(function (data,status) {
			$scope.floorData = data.data;
			
			/*$scope.enableTag = function(item){
				$('#muiFloorSwitch').toggleClass('checked');
				SceneJS.getScene().getNode("myEnable",function(myEnable){
					myEnable.setEnabled(!myEnable.getEnabled());
			   	});
			}*/
			$scope.floorClick = function(item,obj){
				console.log('全部的oid',item.oidContains);
				item.isActive = !item.isActive;
				angular.forEach(item.oidContains, function(data,index,array){
					//和类型不要互相冲突
					if(!xeogl.scene.components['ifc'+data].visibility.showType){
						xeogl.scene.components['ifc'+data].visibility.visible = item.isActive;
						xeogl.scene.components['ifc'+data].visibility.showFloor = !item.isActive;
					}
				});
			}
	    }); 
	}
	
	
	$scope.searchCtrl = function($scope, $http){
		$scope.bimSearch = function(){
			if($.html5Validate.isAllpass($('#searchFrom'))){
				$http.get('./model/searchRecord.do?rid='+string+'&keyword='+$('#searchText').val()).success(function (data,status) {
					$scope.searchList = data.data;
			    }); 
			}
			$scope.searchShow = function(item){
				item.checked = !item.checked;
				var itemBox = xeogl.scene.components['ifc'+item.oid];
				if(itemBox){
					cameraFlightAnimation.flyTo({
	                    aabb: itemBox.worldBoundary.aabb
	                });
	                if(hisPick.name){
						xeogl.scene.components[hisPick.name].material.emissive =  xeogl.scene.components[hisPick.name].material.baseColor;
	                }                
	                itemBox.material.emissive = new Float32Array([0, 1, 0]);
	                hisPick = {name:'ifc'+item.oid};
				}
                
                if(window.tt){
                	var pTableScope= $('#pTable').scope();
                    pTableScope.oid = item.oid ;
                    $.ajax({
                  	  url:'./model/queryProperty.do',
                  	  type:'GET',
                  	  data:{oid:'ifc'+item.oid,rid:string}
                    }).done(function(data){
                  	  pTableScope.list = data.data; 
                  	  $('#pTable').scope().$apply();
                    })
                }
				
			}
		}
	}
	
	$scope.markCtrl = function($scope, $http){
		$scope.changColor=function(){
			$('.demo').minicolors();
		}
	}
	
	
});