SceneJS.setConfigs({
    pluginPath:"public/js/scenejs/plugins"
});



function showView(geom) {
        var boundMinX = 0, boundMinY = 0, boundMinZ = 0,
            boundMaxX = 0, boundMaxY = 0, boundMaxZ = 0;
        	if(geom.bound){
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
}



function createScene(sceneNodes){
	var yaw = 0;
	var  pitch=35.89999999999978,
	minPitch= -80,
	maxPitch=-10,
	zoom= - (~~(maxZoom/3)),
	zoomSensitivity= -(~~(maxZoom/15)),
	//eye={x:middle.x,y: - middle.y* 10,z: maxZoom/5*4},
	eye = middle,
	look=middle;

	console.log(zoom);
	console.log('zoomSensitivity',zoomSensitivity);
	
	 var sceneViewObj = {
		        canvasId:"mySceneCanvas",
		        type: "scene",
		        nodes: [
					{
					    type:"skybox/clouds",
					    size:5000 // Box half-size on each axis - default is 5000
					},
					{
		        	type: "cameras/pickFlyOrbit",
		        	showCursor: true,
		            id:'cameras',
		            yaw:-20,
		            pitch:-90.72999999999763,
		            minPitch:minPitch,
		            maxPitch:maxPitch,
		            zoom:zoom,
		            zoomSensitivity:zoomSensitivity,
		            eye:eye,
		            look:look,
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
/*    window.hisPick = {}; //记录点过的东西
    scene.on("pick",function (hit) {
          

          // To illustrate, these are the params to expect on the pick hit:
          var name = hit.name; // Eg. "object1"
          var path = hit.path; // Eg. "foo.object1"
          var nodeId = hit.nodeId;
          var canvasX = hit.canvasPos[0];
          var canvasY = hit.canvasPos[1];
    });

    */
}


function easeOut(t, b, c, d) {
    var ts = (t /= d) * t;
    var tc = ts * t;
    return b + c * (-1 * ts * ts + 4 * tc + -6 * ts + 4 * t);
}

function easeIn(t, b, c, d) {
    var ts = (t /= d) * t;
    var tc = ts * t;
    return b + c * (tc * ts);
}

