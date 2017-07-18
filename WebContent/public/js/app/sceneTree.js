var myApp = angular.module("myApp", []);



myApp.controller('myAppCtrl', function ($scope, $http,$compile) {
	
	$scope.menuClick = function(param){
		$scope[param]($scope, $http,$compile);
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
		if(!$scope.typeList){
			$http.get('./model/queryBuildingCells.do?rid='+string).success(function (data,status) {  
		    	$scope.typeList = data.data;
		    }).error(function (data,status) {
		    	
		    }); 
		}
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
	
	$scope.markCtrl = function($scope, $http,$compile){
		$scope.changColor=function(){
			$('.demo').minicolors();
		}
	}
	
	$scope.colorCtrl = function($scope, $http){
		
		$scope.IfcMType = localStorage.getItem("IfcMType") || 1;
		
	    $scope.colorData = [{id:1,name:'方案一'},{id:2,name:'方案二'},{id:3,name:'方案三'}];
	    
		$scope.colorClick = function(item){
			Ifc.Constants.materials=Ifc.Constants['materials'+item.id];
			localStorage.setItem("IfcMType",item.id);
			$scope.IfcMType = item.id;
			if(!$scope.typeList){
				$http.get('./model/queryBuildingCells.do?rid='+string).success(function (data,status) {  
			    	$scope.typeList = data.data;
			    	allChangeColor($scope.typeList);
			    }); 
			}else{
				allChangeColor($scope.typeList);
			}
		}
	}
	
	$scope.majorCtrl = function($scope, $http){
		
		$http.get('./model/queryAllModelAndOutputTemplateMap.do?rid='+string).success(function (data,status) {
	    	$scope.majorTypedata = data.data;
	    });
		
		$scope.initMajor = function(){
			$http.get('./model/newOutputTemplate.do?rid='+string).success(function (data,status) {
		    	$scope.majorFirstdata = data.data;
		    	$scope.majorTypedata.push(data.data);
		    	$scope.majorTypedata.indexNow = 0;
		    	dealMajorData();
		    });
		}
		
		$scope.selectDown = function(){
			$scope.majorTypedata.showTag = true;
		}
		
		$scope.allMajorValue = function(value){
			value.selected = !value.selected;
			
			//第一层选择选中都选中
			if(value.namespaceSelectorMap){
				if(value.selected){
					var tt = $scope.majorTypedata[$scope.majorTypedata.indexNow].ifcTypeSelectorMap;
					//angular.forEach(tt, function(ttdata,idx){
						//ttdata.selected = true;
						angular.forEach(value.namespaceSelectorMap, function(ttd,i){
							ttd.selected = true;
							angular.forEach(ttd.objectTypeContainerMap, function(td){
								td.selected = true;
							});
						});
					//});
				}else{
					/*var tt = $scope.majorTypedata[$scope.majorTypedata.indexNow].ifcTypeSelectorMap;
					angular.forEach(tt, function(ttdata,idx){
						ttdata.selected = false;*/
						angular.forEach(value.namespaceSelectorMap, function(ttd,i){
							ttd.selected = false;
							angular.forEach(ttd.objectTypeContainerMap, function(td){
								td.selected = false;
							});
						});
					//});
				}
			}

			
			//第二层选择选中都选中
			if(value.objectTypeContainerMap){
				if(value.selected){
					angular.forEach(value.objectTypeContainerMap, function(ttdata){
						ttdata.selected = true;
					});
				}else{
					angular.forEach(value.objectTypeContainerMap, function(ttdata){
						ttdata.selected = false;
					});
				}
			}
			
			checkTree();
			
		}
		
		$scope.saveBtn = function(){
			var dialog = new QAQ.AADialog({
			    compile:$compile,
			    scope:$scope,
		        backdrop: false,//默认点击dialog背景时 不关闭dialog
		        keyboard: true,//默认 按键盘escape 关闭dialog
		        title: "对话框标题11",
		        content: '<p>我是dialog的内容</p>',//dialog的模板 3种模式  1. content:'<p>我是dialog的内容</p>' 2.contentUrl:'/views/dialog模板.html' 3.contentSelector:'#模板id'
		        width: 420,//选填
		        zIndex: 999,//
		        cache: true,//是否对模板进行缓存
		        modal: true, //是否显示遮罩
		        renderTo: 'body',//暂时无用
		        drag: true,//是否可拖拽
		        winResize: false,//浏览器缩放时是否重新定位
			});
		}
		
		function dealMajorData(){
			var tt = $scope.majorTypedata[$scope.majorTypedata.indexNow].ifcTypeSelectorMap;
			angular.forEach(tt, function(ttdata,idx){
				ttdata.selected = true;
				angular.forEach(ttdata.namespaceSelectorMap, function(ttd,i){
					ttd.selected = true;
				});
			});
		}
		
		function checkTree(){
			var tt = $scope.majorTypedata[$scope.majorTypedata.indexNow].ifcTypeSelectorMap;
			angular.forEach(tt, function(ttdata){
				tt.selected = tt.selected;
				var s1 = 0;
				var count1 = 0;
				angular.forEach(ttdata.namespaceSelectorMap, function(tdata,key){
					if(key!='selected'){
						count1++;
					}
					var s2 = 0;
					var count2 = 0;
					angular.forEach(tdata.objectTypeContainerMap, function(t){
						if(t.objectType){
							count2++;
						}
						if(t.selected){
							s2++;
						}
					});	
					if(s2 == count2){
						tdata.selected  = true;
					}else{
						tdata.selected  = false;
					}
					if(tdata.selected){
						s1++;
					}					
				});
				if(s1 == count1){
					ttdata.selected  = true;
				}else{
					ttdata.selected  = false;
				}
			});
		}
		
		//不影响数据单纯数据操作		
		window.collosePand = function(){
			console.log(this)
			
		}
	}

	function allChangeColor(list){
		angular.forEach(list, function(item,index,array){
    		angular.forEach(item.oids, function(data,index,array){
    			var mt = Ifc.Constants.materials['Ifc'+item.name] || Ifc.Constants.materials['DEFAULT'];
    			if(xeogl.scene.components['ifc'+data]){
	    			xeogl.scene.components['ifc'+data].material.emissive = [mt.r,mt.g,mt.b];
	    			xeogl.scene.components['ifc'+data].material.baseColor = [mt.r,mt.g,mt.b]; 
	    			xeogl.scene.components['ifc'+data].material.opacity = mt.a;
    			}
			});
		});
	}
	

});