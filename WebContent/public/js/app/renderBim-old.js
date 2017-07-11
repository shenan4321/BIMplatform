SceneJS.setConfigs({
    pluginPath:""
});



function showView(geom) {
        var boundMinX = 0, boundMinY = 0, boundMinZ = 0,
            boundMaxX = 0, boundMaxY = 0, boundMaxZ = 0;
        
            boundMinX = Math.min(0, geom.bound.min.x);
            boundMinY = Math.min(0, geom.bound.min.y);
            boundMinZ = Math.min(0, geom.bound.min.z);
            boundMaxX = Math.max(0, geom.bound.max.x);
            boundMaxY = Math.max(0, geom.bound.max.y);
            boundMaxZ = Math.max(0, geom.bound.max.z);
            var node = {
                    geometry_info: geom,
                    boundMinX : boundMinX,
                    boundMinY : boundMinY,
                    boundMinZ : boundMinZ,
                    boundMaxX : boundMaxX,
                    boundMaxY : boundMaxY,
                    boundMaxZ : boundMaxZ,
                    type:"geometry/ifcmodel"
            };
            window.scene.getNode("my-lights",function(xxx){
    	   		xxx.addNode({type:"material",nodes:[node]});
    	   	});
}
function createScene(sceneNodes){
	 var sceneViewObj = {
		        canvasId:"mySceneCanvas",
		        type: "scene",
		        nodes: [{
		            type:"cameras/orbit",
		            id:'cameras',
		            yaw:0,
		            pitch:86.89999999999996,
		            minPitch:95.89999999999996,
		            maxPitch:160.9999999999996,
		            zoom:-40000,
		            zoomSensitivity:2000,
		            eye:{ x:0, y:0, z:10 },
		            look:{ x:0, y:0, z:0 },
		            showCursor:true,
		            nodes: [{
		                type: 'camera',
		                id: 'main-camera',
		                optics: {
		                    type: 'perspective',
		                    far: 132440.78865666725,
		                    near: 132.44078865666725,
		                    aspect: $(window).width() / $(window).height(),
		                    fovy: 37.8493
		                },
		                nodes: [{
		                    type: 'renderer',
		                    id: 'main-renderer',
		                    clear: {
		                        color: true,
		                        depth: true,
		                        stencil: true
		                    },
		                    nodes:[{
		                        type:"enable",
		                        id:"myEnable",
		                        enabled:true,
		                        nodes: [{
		                            type: 'lights',
		                            id: 'my-lights',
		                            lights: [
		                                {
		                                    mode:"ambient",
		                                    color:{ r:0.9, g:0.9, b:0.9},
		                                    diffuse:false,
		                                    specular:false
		                                },
		                                {
		                                    mode:"dir",
		                                    color:{ r:1.0, g:1.0, b:1.0 },
		                                    diffuse:true,
		                                    specular:true,
		                                    dir:{ x:-0.5, y:-0.5, z:-1.0 },
		                                    space:"view"
		                                },
		                                {
		                                    type:		'light',
		                                    id:			'sun-light',
		                                    mode:		'dir',
		                                    color:		{r: 0.6, g: 0.6, b: 0.6},
		                                    dir:   		{x: -0.5, y: 0.5, z: -1.0},
		                                    diffuse:	true,
		                                    specular:	true
		                                }
		                            ]
		                        }]
		                    }]
		                }]
		            }]
		        }]
		    };
	    window.scene =  SceneJS.createScene(sceneViewObj);
	    
}
function bindEvent(sceneNodes) {
    
    window.hisPick = {}; //记录点过的东西
    /*scene.on("pick",function (hit) {
    		console.log(hit);	
    	          scene.getNode(hit.name + "geometry",
    	                  function (material) {
    	                      if(hisPick.name){
    	                          scene.getNode(hisPick.name + "geometry", function (material) {
    	                              material.setColor(hisPick.color);//之前点过的东西还原
    	                          });
    	                      }
    	                      hisPick = {name:hit.name,color:material.getColor()}
    	                      material.setColor({r: 0, g: 1, b: 0});
    	                      var pTableScope= $('#pTable').scope();
    	                      pTableScope.oid = hit.name ;
    	                      $.ajax({
    	                    	  url:'./model/queryProperty.do',
    	                    	  type:'GET',
    	                    	  data:{oid:hit.name,rid:string}
    	                      }).done(function(data){
    	                    	  pTableScope.list = data.data  
    	                    	  $('#pTable').scope().$apply();
    	                      })
	                      	  
    	                  });

    	          // To illustrate, these are the params to expect on the pick hit:
    	          var name = hit.name; // Eg. "object1"
    	          var path = hit.path; // Eg. "foo.object1"
    	          var nodeId = hit.nodeId;
    	          var canvasX = hit.canvasPos[0];
    	          var canvasY = hit.canvasPos[1];
    });*/
    
  

    var canvas = scene.getCanvas();

    

    function mouseDown(event) {
        lastX = downX = event.clientX;
        lastY = downY = event.clientY;
        dragging = true;
    }

    function touchStart(event) {
        lastX = downX = event.targetTouches[0].clientX;
        lastY = downY = event.targetTouches[0].clientY;
        dragging = true;
    }

    function mouseUp(event) {

        if (dragging && closeEnough(event.clientX, downX) && closeEnough(event.clientY, downY)) {
            pick(event.clientX, event.clientY);
        }
        dragging = false;
    }

    function closeEnough(x, y) {
        return (x > y) ? (x - y < 5) : (y - x < 5);
    }

    function touchEnd(event) {
        if (dragging && event.targetTouches[0].clientX == downX && event.targetTouches[0].clientY == downY) {
            pick(event.targetTouches[0].clientX, event.targetTouches[0].clientY);
        }
        dragging = false;
    }

    function mouseMove(event) {
        var posX = event.clientX;
        var posY = event.clientY;
        actionMove(posX, posY);
    }

    function touchMove(event) {
        var posX = event.targetTouches[0].clientX;
        var posY = event.targetTouches[0].clientY;
        actionMove(posX, posY);
    }

    function actionMove(posX, posY) {
        if (dragging) {
            yaw += (posX - lastX) * 0.1;
            pitch -= (posY - lastY) * 0.1;
            orbiting = true;
        }
        lastX = posX;
        lastY = posY;
    }

    function mouseWheel(event) {
        var delta = 0;
        if (!event) event = window.event;
        if (event.wheelDelta) {
            delta = event.wheelDelta / 120;
            if (window.opera) delta = -delta;
        } else if (event.detail) {
            delta = -event.detail / 3;
        }
        if (delta) {
            if (delta < 0) {
                zoom -= zoomSensitivity;
            } else {
                zoom += zoomSensitivity;
            }
        }
        if (event.preventDefault) {
            event.preventDefault();
        }
        event.returnValue = false;
        orbiting = true;
    }

    canvas.addEventListener('mousedown', mouseDown, true);
    canvas.addEventListener('mouseup', mouseUp, true);
    canvas.addEventListener('touchstart', touchStart, true);
    canvas.addEventListener('touchend', touchEnd, true);
    canvas.addEventListener('mousemove', mouseMove, true);
    canvas.addEventListener('touchmove', touchMove, true);
    canvas.addEventListener('mousewheel', mouseWheel, true);
    canvas.addEventListener('DOMMouseScroll', mouseWheel, true);

    function pick(canvasX, canvasY) {
        scene.pick({ canvasPos :[canvasX, canvasY], rayPick: true });
    }
    scene.on("pick",function (hit) {
    	
    		console.log('hit',hit);
            // Some plugins wrap things in this name to
            // avoid them being picked, such as skyboxes
            if (!hit.worldPos || hit.name == "__SceneJS_dontPickMe") {
                return;
            }
            
            

            startPivot = glmat.vec3.fromValues(currentPivot[0], currentPivot[1], currentPivot[2]);
            endPivot = hit.worldPos;

            if (indicatorVis) {
                indicatorVis.setEnabled(true);
                indicatorPos.setXYZ({x: endPivot[0], y: endPivot[1], z: endPivot[2] });
                label.setShown(true);
            }

            var vec = glmat.vec3.create();
            glmat.vec3.sub(vec, endPivot, startPivot);

            flightDist = glmat.vec3.length(vec);
            flightStartTime = null;
            flightDuration = 1000.0 * ((flightDist / 1000) + 1); // extra seconds to ensure arrival
            flying = true;

            label.setText("[ " + Math.round(endPivot[0]) + ", " + Math.round(endPivot[1]) + ", " + Math.round(endPivot[2]) + " ]");
        });

    scene.on("tick",
        function () {

            if (flying) {

                if (flightStartTime == null) {
                    flightStartTime = (new Date()).getTime();
                }

                var timeNow = (new Date()).getTime();
                var timeElapsed = timeNow - flightStartTime;

                if (timeElapsed >= flightDuration) {
                    flying = false;
                    flying = false;
                    flightStartTime = null;

                    // Hide pick indicator
                    if (indicatorVis) {
//                        indicatorVis.setEnabled(false);
//                        label.setShown(false);
                    }
                } else {

                    var easedTime = easeOut((timeNow - flightStartTime) / flightDuration, 0, 1, 1);

                    // Continue flight
                    // Find new pivot point, interpolated on path towards new point

                    glmat.vec3.lerp(currentPivot, startPivot, endPivot, easedTime);

                    // Need to rotate lookat
                    orbiting = true;
                }
            }


            if (orbiting) {

                // Update location of point-of-interest indicator
                if (indicatorVis) {
                    indicatorVis.setEnabled(true);
                    label.setShown(true);
                }

                var eye = glmat.vec3.fromValues(0, 0, zoom);
                var look = glmat.vec3.fromValues(currentPivot[0], currentPivot[1], currentPivot[2]);
                //    var up = glmat.vec3.fromValues(0, 1, 0);

                var eyeVec = glmat.vec3.create();
                glmat.vec3.sub(eyeVec, eye, look);

                var mat = glmat.mat4.create();

                glmat.mat4.rotateY(mat, mat, -yaw * 0.0174532925);
                glmat.mat4.rotateX(mat, mat, -pitch * 0.0174532925);

                var eye3 = glmat.vec3.create();

                glmat.vec3.transformMat4(eye3, eye, mat);

                // Update view transform
                lookat.setLook({x: look[0], y: look[1], z: look[2] });
                lookat.setEye({x: look[0] - eye3[0], y: look[1] - eye3[1], z: look[2] - eye3[2] });

                var lookatArgs = {
                    eye: {x: look[0], y: look[1], z: look[2] },
                    look: {x: look[0] - eye3[0], y: look[1] - eye3[1], z: look[2] - eye3[2] }
                };

                self.publish("updated", lookatArgs);

                // Rotate complete
                orbiting = false;

                if (indicatorVis) {
                    setTimeout(function () {
                        indicatorVis.setEnabled(false);
                        label.setShown(false);
                    }, 2000);
                }
            }
        });
    
}


