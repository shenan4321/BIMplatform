var scene = {};

SceneJS.setConfigs({
    pluginPath:"public/js/scenejs/plugins"
});

function showView(data) {
        var boundMinX = 0, boundMinY = 0, boundMinZ = 0,
            boundMaxX = 0, boundMaxY = 0, boundMaxZ = 0,
            len = data.length,
            nodes = new Array(len);

        for(var i = 0; i <len; i++){
            var geom = data[i];
            boundMinX = Math.min(0, geom.bound.min.x);
            boundMinY = Math.min(0, geom.bound.min.y);
            boundMinZ = Math.min(0, geom.bound.min.z);
            boundMaxX = Math.max(0, geom.bound.max.x);
            boundMaxY = Math.max(0, geom.bound.max.y);
            boundMaxZ = Math.max(0, geom.bound.max.z);
            var node = {
                nodes:[{
                    geometry_info: geom,
                    boundMinX : boundMinX,
                    boundMinY : boundMinY,
                    boundMinZ : boundMinZ,
                    boundMaxX : boundMaxX,
                    boundMaxY : boundMaxY,
                    boundMaxZ : boundMaxZ,
                    type:"geometry/ifcmodel"
                }]
            };
            nodes[i] = node;
        }
        SceneJS.getScene().getNode("my-lights",function(xxx){
	   		xxx.addNode({type:"material",nodes: nodes})
	   	});

}

function sceneJsShow(sceneNodes) {
    var sceneViewObj = {
        canvasId:"mySceneCanvas",
        type: "scene",
        nodes: [{
            type: 'lookAt',
            id: 'main-lookAt',
            eye: { x: 1, y: 1, z: 1 },
            look:{ x: 0.0, y: 0.0, z: 0.0 },
            up:{ x: 0.0, y: 0.0, z: 1.0 },
            nodes: [{
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
                        type:"cameras/orbit",
                        id:'cameras',
                        yaw:0,
                        pitch:115.89999999999996,
                        zoom:-( Math.abs( sceneNodes[sceneNodes.length-1].bound.max.x - sceneNodes[sceneNodes.length-1].bound.min.x ))-10000,
                        zoomSensitivity:2000,
                        eye:{ x:0, y:0, z:10 },
                        look:{ x:0, y:0, z:0 },
                        nodes: [{
                            type: 'renderer',
                            id: 'main-renderer',
                            clear: {
                                color: true,
                                depth: true,
                                stencil: true
                            },
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
                            }
                            ]
                        }]

                    }]
                }]
            }]
        }]
    };
    
    scene =  SceneJS.createScene(sceneViewObj);
    showView(sceneNodes);

    
    var info = document.getElementById("infoDark");
    var hisPick = {}; //记录点过的东西
    scene.on("pick",function (hit) {
    		console.log(hit);	
    	          scene.getNode(hit.name + "geometry",
    	                  function (material) {
    	        	  console.log(material);
    	                      if(hisPick.name){
    	                          scene.getNode(hisPick.name + "geometry", function (material) {
    	                              material.setColor(hisPick.color);//之前点过的东西还原
    	                          });
    	                      }
    	                      hisPick = {name:hit.name,color:material.getColor()}
    	                      material.setColor({r: 0, g: 1, b: 0});
    	                  });
    	          info.innerHTML = "Pick hit: " + JSON.stringify(hit);

    	          // To illustrate, these are the params to expect on the pick hit:
    	          var name = hit.name; // Eg. "object1"
    	          var path = hit.path; // Eg. "foo.object1"
    	          var nodeId = hit.nodeId;
    	          var canvasX = hit.canvasPos[0];
    	          var canvasY = hit.canvasPos[1];

    	      });
    
    
  
    var canvas = scene.getCanvas();

    var downX;
    var downY;
    var lastX;
    var lastY;
    var dragging;
    var yaw = 0;
    var pitch =115.89999999999996;


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

    canvas.addEventListener('mousedown', mouseDown, true);
    canvas.addEventListener('mouseup', mouseUp, true);
    canvas.addEventListener('touchstart', touchStart, true);
    canvas.addEventListener('touchend', touchEnd, true);
    canvas.addEventListener('mousemove', mouseMove, true);
    canvas.addEventListener('touchmove', touchMove, true);

    function pick(canvasX, canvasY) {
        scene.pick(canvasX, canvasY, { rayPick: true });
    }
}


