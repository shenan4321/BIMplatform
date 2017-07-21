var myApp = angular.module("myApp", []);



myApp.controller('myAppCtrl', function ($scope, $http,$compile) {
	
	
	$scope.IfcMType = localStorage.getItem("IfcMType") || 1;

	$scope.colorData = [{id:1,name:'方案一',isActive:true},{id:2,name:'方案二',isActive:true},{id:3,name:'方案三',isActive:true}];
	
	
	//marjord 数据整理成罗盘数据
	function formatterMajorToLuoPan(md){
		var luopanData = [];
		$.each(md,function(index,item){
    		
			luopanData.push({name:this.name,onClick:function(){
				$http.get('./model/queryOutputTemplate.do?rid='+string+'&otid='+item.otid).success(function (res) {
					$('.svg-item-selected')[0].setAttribute('class','svg-item');
					$('.svg-item').eq(index)[0].setAttribute('class','svg-item-selected');
					$scope.majorTypedata.indexNow = index;
					$scope.majorTypedata[index].ifcTypeSelectorMap = res.data.ifcTypeSelectorMap;
					checkTree();
	    		});
			},selected: ($scope.majorTypedata.indexNow==index )? true:false});
    				
    	});
		
		
		return luopanData;
	}
	
	
	$http.get('./model/queryModelAndOutputTemplateMap.do?rid='+string).success(function (data,status) {
    	$scope.majorTypedata = data.data;
    	$scope.majorTypedata.indexNow = 0; //当前是第几个专业
    	
    	/*$http.get('./model/queryOutputTemplate.do?rid='+string+'&otid='+item.otid).success(function (res) {
			$scope.majorTypedata[index].ifcTypeSelectorMap = res.data.ifcTypeSelectorMap;
			checkTree();
		});*/
    	var luopanData = [];
    	$http.get('./model/queryOutputTemplate.do?rid='+string+'&otid='+$scope.majorTypedata[$scope.majorTypedata.indexNow].otid).success(function (res) {
			$scope.majorTypedata[$scope.majorTypedata.indexNow].ifcTypeSelectorMap = res.data.ifcTypeSelectorMap;
			checkTree();
		});
    	
    	
    	luopanBox = $.luopan({data:formatterMajorToLuoPan($scope.majorTypedata)});
    	
    });
	
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
				if(xeogl.scene.components['ifc'+data]){
					xeogl.scene.components['ifc'+data].visibility.showType = !item.checked;//默认此参数是空，所以某类显示出来的时候
					
					if(item.checked){
						if(!xeogl.scene.components['ifc'+data].visibility.showType && !xeogl.scene.components['ifc'+data].visibility.showTypeType && !xeogl.scene.components['ifc'+data].visibility.showFloor){
							xeogl.scene.components['ifc'+data].visibility.visible = true;
						}
					}else{
						xeogl.scene.components['ifc'+data].visibility.visible = false;
					}
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
				item.isActive = !item.isActive;
				angular.forEach(item.oidContains, function(data,index,array){
					//和类型不要互相冲突
					//如果他是true
					if(xeogl.scene.components['ifc'+data]){
						xeogl.scene.components['ifc'+data].visibility.showFloor = !item.isActive;
						if(item.isActive){
							if(!xeogl.scene.components['ifc'+data].visibility.showType && !xeogl.scene.components['ifc'+data].visibility.showTypeType && !xeogl.scene.components['ifc'+data].visibility.showFloor){
								xeogl.scene.components['ifc'+data].visibility.visible = true;
							}
						}else{
							xeogl.scene.components['ifc'+data].visibility.visible = false;
						}
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
		
		var temp = [];
		
		
		$scope.initMajor = function(){
			$.ajax({
				url:'./model/newOutputTemplate.do?rid='+string, //拿默认模板
				type :'get'
			}).done(function(data){
		    	$scope.majorTypedata.push(data.data);
		    	$scope.majorTypedata.indexNow = $scope.majorTypedata.length-1;
		    	luopanBox.updateDom(formatterMajorToLuoPan($scope.majorTypedata));
				$.ajax({
					type:"POST",
					url:'./model/saveOutputTemplate/'+string+'.do',
			        contentType: "application/json",
					data:JSON.stringify(data.data)
				}).done(function(res){
					$scope.majorTypedata[$scope.majorTypedata.indexNow].otid = res.otid;
			    	dealMajorData();
			    	$scope.$apply();
				});
			})
		}
		
		$scope.selectDown = function(){
			$('.select-down-box').toggle();
		}
		
		$scope.allMajorValue = function(value){
			value.selected = !value.selected;
			
			//第一层选择选中都选中
			if(typeof(value.namespaceSelectorMap) !== 'undefined' && value.namespaceSelectorMap){
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
			if(typeof(value.namespaceSelectorMap) !== 'undefined' && value.objectTypeContainerMap){
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
		
		$scope.saveMajor = function(){
			$scope.majorTypedata[$scope.majorTypedata.indexNow].name = $('#txt').html();
			var tempMd = delSelectedMap($scope.majorTypedata[$scope.majorTypedata.indexNow]);
			$.ajax({
				type:"POST",
				url:'./model/modifyOutputTemplate/'+string+'.do',
				datatype:"json",
		        contentType: "application/json",
		        data:JSON.stringify(tempMd)
			}).done(function(res){
				if(res.success){
					QAQ.Dialog.info('修改成功','info');
					luopanBox.updateDom(formatterMajorToLuoPan($scope.majorTypedata));
					setTimeout(function(){
						$('.sb_dialog_modal').remove();
						$('.qaq').remove();
					},1500);
				}else{
					QAQ.Dialog.info('增加失败');
				}
			});
		}
		
		$scope.removeMajorType =  function(item,num){
			QAQ.Dialog.confirm('确认不要我了?',function(){
				$http.post('./model/deleteOutputTemplate.do?rid='+string+'&otid='+item.otid).success(function (res) {
					if(res.success){
						QAQ.Dialog.info('删除成功','info');
						luopanBox.updateDom(formatterMajorToLuoPan($scope.majorTypedata));
						setTimeout(function(){
							$('.sb_dialog_modal').remove();
							$('.qaq').remove();
							$scope.majorTypedata.splice(num,1);
						},1500);
					}else{
						QAQ.Dialog.info('删除成功');
					}
	    		});
		    });
		}   
		
		
		$scope.chooseMajorType = function(item,num){
			$('.select-down-box').hide();
			$scope.majorTypedata.indexNow = num;
			if(num<=12){
				$('.svg-item-selected')[0].setAttribute('class','svg-item');
				$('.svg-item').eq(num)[0].setAttribute('class','svg-item-selected');
			}
			$http.get('./model/queryOutputTemplate.do?rid='+string+'&otid='+item.otid).success(function (res) {
				$scope.majorTypedata[$scope.majorTypedata.indexNow].ifcTypeSelectorMap = res.data.ifcTypeSelectorMap;
				checkTree();
    		});
		}
		
		//后台默认没带，树形前面两个大类标签的selected
		function dealMajorData(){
			var tt = $scope.majorTypedata[$scope.majorTypedata.indexNow].ifcTypeSelectorMap;
			angular.forEach(tt, function(ttdata,idx){
				ttdata.selected = true;
				angular.forEach(ttdata.namespaceSelectorMap, function(ttd,i){
					ttd.selected = true;
				});
			});
		}
		
		//后台默认没带，树形前面两个大类标签的selected，删除selected树形，回传
		function delSelectedMap(tt){
			tt = angular.copy(tt);
			delete tt.$$hashKey;
			delete tt.rid;
			angular.forEach(tt, function(ttdata){
				delete ttdata.selected;
				if( ttdata instanceof Object && typeof(ttdata.namespaceSelectorMap) !== 'undefined'){
					angular.forEach(ttdata.namespaceSelectorMap, function(tdata,key){
						delete tdata.selected;
					});
				}
			});
			return tt;
		}
		
		
		
		//不影响数据单纯数据操作		
		window.collosePand = function(obj){
			var $jq_obj = $(obj);
			if($jq_obj.attr('class').indexOf('icon-white-down')>-1){
				$jq_obj.attr('class','icon-white-up tile-fr');
			}else{
				$jq_obj.attr('class','icon-white-down tile-fr');
			}
			var $jq_baba = $jq_obj.parent();
			$jq_baba.next().toggle();
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
	
	function checkTree(){
		var tt = $scope.majorTypedata[$scope.majorTypedata.indexNow].ifcTypeSelectorMap;
		angular.forEach(tt, function(ttdata){
			tt.selected = !!tt.selected;
			var s1 = 0;
			var count1 = 0;
			if( ttdata instanceof Object && typeof(ttdata.namespaceSelectorMap) !== 'undefined'){
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
						angular.forEach(t.oids, function(data,index,array){
							
							if(xeogl.scene.components['ifc'+data]){
								xeogl.scene.components['ifc'+data].visibility.showTypeType = !t.selected;//默认此参数是空，所以某类显示出来的时候
								
								if(t.selected){
									if(!xeogl.scene.components['ifc'+data].visibility.showType && !xeogl.scene.components['ifc'+data].visibility.showTypeType && !xeogl.scene.components['ifc'+data].visibility.showFloor){
										xeogl.scene.components['ifc'+data].visibility.visible = true;
									}
								}else{
									xeogl.scene.components['ifc'+data].visibility.visible = false;
								}
							}
							
						});
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
			}
		});
	}

});