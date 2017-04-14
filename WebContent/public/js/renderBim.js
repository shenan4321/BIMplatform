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
                        zoom:- (Math.abs( Math.max(0, sceneNodes[sceneNodes.length-1].bound.max.x) - Math.min(0, sceneNodes[sceneNodes.length-1].bound.min.x) )),
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
    
    SceneJS.createScene(sceneViewObj);
    showView(sceneNodes);

}


