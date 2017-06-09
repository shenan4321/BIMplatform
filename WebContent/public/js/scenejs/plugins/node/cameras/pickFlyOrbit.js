/**
 * Camera that flies to ray-picked point on scene object
 *
 * @author xeolabs / http://xeolabs.com
 *
 * <p>Usage example</p>
 *
 * <pre>someNode.addNode({
 *      type: "cameras/pickFlyOrbit",
 *      eye:{ x: y:0 },
 *      look:{ y:0 },
 *      yaw: 340,,
 *      pitch: -20,
 *      zoom: 350,
 *      zoomSensitivity:10.0,
 *      showCursor: true
 * });
 * </pre>
 *
 * <p>The camera is initially positioned at the given 'eye' and 'look', then the distance of 'eye' is zoomed out
 * away from 'look' by the amount given in 'zoom', and then 'eye' is rotated by 'yaw' and 'pitch'.</p>
 *
 */
require([
    SceneJS.getConfigs("pluginPath") + "/lib/gl-matrix-min.js",
    SceneJS.getConfigs("pluginPath") + "/lib/quaternion.js"
],
    function (glmat,Quaternion) {

        // Create target indicator div

        var label = (function () {
            var text;
            var body = document.getElementsByTagName("body")[0];
            var div = document.createElement('div');

            var style = div.style;
            style.display = "none";
            style.position = "absolute";
            style["font-family"] = "Helvetica";
            style["font-size"] = "14px";
            style.padding = "5px";
            style.margin = "4px";
            style["padding-left"] = "12px";
            style["border"] = "1px solid #000055";
            style.color = "black";
            style.background = "#AAFFAA";
            style.opacity = "0.8";
            style["border-radius"] = "3px";
            style["z-index"] = 100000;
            style["-moz-border-radius"] = "3px";
            style["box-shadow"] = "3px 3px 3px #444444";
            style.left = "0";
            style.top = "0";
            style.height = "auto";
            style.width = "auto";
            div.innerHTML += 'Foo';
            body.appendChild(div);
            return {

                // Shows label, but only if text has been set
                setShown: function (shown) {
                    style.display = shown && text ? "" : "none";
                },

                // Sets canvas position of label
                setPos: function (pos) {
                    style.left = "" + pos.x + "px";
                    style.top = "" + pos.y + "px";
                },

                // Sets text in label
                setText: function (t) {
                    text = t;
                    div.innerHTML = "<span>" + text + "</span>";
                }
            }
        })();

        var vecCrossProduct = function(a, b) { var r = SceneJS_math_cross3Vec3([a.x, a.y, a.z], [b.x, b.y, b.z]); return {x:r[0], y:r[1], z:r[2]}; };
        var vecMultiplyScalar = function(a, m) { return {x:a.x*m, y:a.y*m, z:a.z*m}; };
        var vecSubtract = function (a, b) { return { x: a.x - b.x, y: a.y - b.y, z: a.z - b.z }; };
        var vecMagnitude = function(v) { var x = v.x, y = v.y, z = v.z; return Math.sqrt(x*x + y*y + z*z); };
        var vecNormalize = function(v) { return vecMultiplyScalar(v, 1/vecMagnitude(v)); };
        var vecNegate = function(v) { return {x:-v.x, y:-v.y, z:-v.z}; };
        var vecAdd = function (a, b) { return { x: a.x + b.x, y: a.y + b.y, z: a.z + b.z }; };
        var sphericalCoords = function(eye) {var r= vecMagnitude(eye)||1;var phi   = Math.acos(eye.z / r);var theta = Math.atan2(eye.y, eye.x);return {phi: phi, theta: theta};};
        window.currentPiont = [0,0,0];
        
        SceneJS.Types.addType("cameras/pickFlyOrbit", {

            construct: function (params) {

                var self = this;

                var lookat = this.addNode({
                    type: "lookAt",
                	id:'lookAt',
                    nodes: [
                        {
                            type: "name",
                            name: "noname",

                            // A plugin node type is responsible for attaching specified
                            // child nodes within itself
                            nodes: params.nodes
                        }
                    ]
                });

                var indicatorPos;
                var indicatorVis;
                var indicatorSize;

                if (params.showCursor) {

                    // Red sphere to indicate current point-of-interest. We'll update the
                    // position of this whenever a new 3D position has been ray-picked.
                    // This will be the position about which we'll orbit.

                    // Sphere position, with a ID so we can update this node:
                    indicatorPos = lookat.addNode({
                        type: "translate",
                        id: "__spherePOI"
                    });

                    indicatorVis = indicatorPos.addNode({
                        type: "flags",
                        flags: {
                            enabled: false,
                            transparent: true,
                            specular: true,
                            diffuse: false
                        }
                    });

                    var cursorSize = params.cursorSize || 1;

                    indicatorSize = indicatorVis.addNode({
                        type: "scale",
                        id: "__sphereSize",
                        x: cursorSize,
                        y: cursorSize,
                        z: cursorSize,
                        nodes: [
                            {
                                type: "material",
                                color: { r: 0.4, g: 1.0, b: 0.4 },
                                specularColor: { r: 1.0, g: 1.0, b: 1.0 },
                                emit: 0.2,
                                nodes: [
                                    {
                                        type: "style",
                                        lineWidth: 2,
                                        nodes: [

                                            // Sphere primitive implemented by plugin at
                                            // http://scenejs.org/api/latest/plugins/node/geometry/sphere.js
                                            {
                                                type: "geometry/sphere"
                                            }
                                        ]
                                    }
                                ]
                            }
                        ]
                    });

                    this.getScene().getNode("__sphereSize", function (n) {
                        n.on("rendered",
                            function (event) {
                                label.setPos(event.getCanvasPos());
                            });
                    });
                }

                var eye = params.eye || { x: 0, y: 0, z: 0 };
                var look = params.look || { x: 0, y: 0, z: 0};
                var zoom = params.zoom || 100;
                var zoomSensitivity = params.zoomSensitivity || 1.0;

                var lookatArgs = {
                    eye: { x: eye.x, y: eye.y, z: eye.z},
                    look: { x: look.x, y: look.y, z: look.z },
                    up: { x: 0, y: 1, z: -zoom }
                };
                lookat.setEye(lookatArgs.eye);
                lookat.setLook(lookatArgs.look);
                lookat.setUp(lookatArgs.up);
                this.publish("updated", lookatArgs);

                var canvas = this.getScene().getCanvas();

                var downX;
                var downY;
                var lastX;
                var lastY;
                var dragging;

                var yaw = params.yaw || 0;
                var pitch = params.pitch || 0;

                var direction = 1;
                var startPivot;
                var currentPivot = glmat.vec3.fromValues(look.x || 0, look.y || 0, look.z || 0);
                var endPivot = glmat.vec3.fromValues(look.x || 0, look.y || 0, look.z || 0);

                var flightStartTime = null;
                var flightDist;
                var flightDuration;
                var flying = false;
                var orbiting = true;
                var mouseWheeling = true;
                

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
                        yaw += (posX - lastX)* 0.001;
                        pitch += (posY - lastY) * 0.001;
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
                    mouseWheeling = false;
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
                canvas.addEventListener('keydown', doKeyDown, true);
                window.addEventListener('keydown', doKeyDown, true); 

                function doKeyDown(event){
                	var keyID = event.keyCode ? event.keyCode :event.which; 
                	if(keyID === 38 ) { // up arrow and W 
                		scene.getNode('lookAt',function(look){
                			var xyz = look.getEye();
            				var myVec3 = [xyz.x-window.currentPiont[0],xyz.y-window.currentPiont[1],xyz.z-window.currentPiont[2]];
                            var caluVec = [myVec3[0]*0.875 ,  myVec3[1]*0.875 , myVec3[2]*0.875];
                            var newVec3 = [caluVec[0]+ window.currentPiont[0],caluVec[1]+window.currentPiont[1],caluVec[2]+window.currentPiont[2]];
                            look.setEye({x: newVec3[0], y: newVec3[1], z:  newVec3[2] });
                            lookat.setEye({x: x, y:y, z: xyz.z });
                            event.preventDefault();
                		})
                	} 
            		if(keyID === 40) { // down arrow and S 
            			scene.getNode('lookAt',function(look){
                			var xyz = look.getEye();
                            var myVec3 = [xyz.x-window.currentPiont[0],xyz.y-window.currentPiont[1],xyz.z-window.currentPiont[2]];
                            /*var r = Math.sqrt(Math.pow(xyz.x-window.currentPiont[0],2)+Math.pow(xyz.y-window.currentPiont[1],2)+Math.pow(xyz.z-window.currentPiont[2],2));
                            if(r>far){
                            	return false;
                            }*/
                            var caluVec = [myVec3[0]*8/7 ,  myVec3[1]*8/7 , myVec3[2]*8/7];
                            var newVec3 = [caluVec[0]+window.currentPiont[0],caluVec[1]+window.currentPiont[1],caluVec[2]+window.currentPiont[2]];
                            look.setEye({x: newVec3[0], y: newVec3[1], z:  newVec3[2] });
                            lookat.setEye({x: x, y:y, z: xyz.z });
                            event.preventDefault();
                		})
                	} 
                	if(keyID === 39) { // right arrow and D 
                		scene.getNode('lookAt',function(look){
                			var xyz = look.getEye();
                            var x=0 ,y=0, z=0;
                            var r = Math.sqrt(Math.pow(xyz.x-window.currentPiont[0],2)+Math.pow(xyz.y-window.currentPiont[1],2)+Math.pow(xyz.z-window.currentPiont[2],2));
                            var thi = Math.atan2(xyz.y-window.currentPiont[1],xyz.x-window.currentPiont[0])*180/Math.PI;
                            thi = thi+1;
                            if(thi> 360){
                            	thi = (thi-360)+1
                            }
                            x=window.currentPiont[0]+r*Math.cos(2*Math.PI/360*(thi));
                            y=window.currentPiont[1]+r*Math.sin(2*Math.PI/360*(thi));
                            look.setEye({x: x, y:y, z: xyz.z });
                            lookat.setEye({x: x, y:y, z: xyz.z });
                            
                            event.preventDefault();
                		})
                	} 
                	
                	if(keyID === 37) { // left arrow and A 
                		scene.getNode('lookAt',function(look){
                			var xyz = look.getEye();
                            var x=0 ,y=0, z=0;
                            var r = Math.sqrt(Math.pow(xyz.x-window.currentPiont[0],2)+Math.pow(xyz.y-window.currentPiont[1],2)+Math.pow(xyz.z-window.currentPiont[2],2));
                            var thi = Math.atan2(xyz.y-window.currentPiont[1],xyz.x-window.currentPiont[0])*180/Math.PI;
                            thi = thi-1;
                            if(thi> 360){
                            	thi = (thi-360)-1
                            }
                            x=window.currentPiont[0]+r*Math.cos(2*Math.PI/360*(thi));
                            y=window.currentPiont[1]+r*Math.sin(2*Math.PI/360*(thi));
                            look.setEye({x: x, y:y, z: xyz.z });
                            lookat.setEye({x: x, y:y, z: xyz.z });
                            event.preventDefault();
                		})
                	} 
                }


                function pick(canvasX, canvasY) {
                    scene.pick({ canvasPos :[canvasX, canvasY], rayPick: true });
                }

                var scene = this.getScene();

                scene.on("pick",
                    function (hit) {

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
                        flightDuration = 1000.0 * ((flightDist / 15000) + 1); // extra seconds to ensure arrival
                        flying = true;

                        label.setText("[ " + Math.round(endPivot[0]) + ", " + Math.round(endPivot[1]) + ", " + Math.round(endPivot[2]) + " ]");
                        window.currentPiont = endPivot;
                        scene.getNode(hit.name + "geometry",function (material) {
                            if(hisPick.name){
                                scene.getNode(hisPick.name + "geometry", function (material) {
                                    material.setColor(hisPick.color);//之前点过的东西还原
                                });
                            }
                            hisPick = {name:hit.name,color:material.getColor()}
                            material.setColor({r: 0.03137255, g: 0.30980392, b: 0.62745098});
                            if(window.tt){
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
                            }
                        });
                        
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
//                                    indicatorVis.setEnabled(false);
//                                    label.setShown(false);
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

                            var radius = vecMagnitude(lookat.getEye());
                			var phiTheta = sphericalCoords(lookat.getEye());
                			var startPhi = phiTheta.phi;
                			var startTheta = phiTheta.theta;
                			var PI_2 = 2*Math.PI;
                			var phi = pitch * 0.5 + startPhi;
                			while(phi > PI_2) phi -= PI_2;
                			while(phi < 0   ) phi += PI_2;
                			if(phi > Math.PI) {
                				if (direction != -1) {
                					direction = -1;
                					//lookat.setUp({x: 0, y:1, z: -zoom});
                				}
                			} else {
                				if (direction != 1) {
                					direction = 1;
                				}
                			}
                			
                			
                			lookat.setUp({x: 0, y:1, z: Math.abs(zoom)>Math.abs(maxZoom) ? Math.abs(maxZoom) : Math.abs(zoom) });

                			
                			//原点的眼睛
                			var eye = glmat.vec3.fromValues(0, 0, zoom);
                			
                			
                            
                            //lookat的那个轴
                            var look = glmat.vec3.fromValues(currentPivot[0], currentPivot[1], currentPivot[2]);
                            //var up = glmat.vec3.fromValues(0, 1, 0);

                            
                            var eyeVec = glmat.vec3.create();
                            
                            //计算出他们之间 眼睛 那个轴向量
                            glmat.vec3.sub(eyeVec, eye, look);

                            var mat = glmat.mat4.create();
                            	
                            
                            var q = Quaternion.fromEuler(0, -pitch * Math.PI/2 ,-yaw * Math.PI/2);
                            
                            mat =  q.conjugate().toMatrix4();

                            var eye3 = glmat.vec3.create();

                            glmat.vec3.transformMat4(eye3, eye, mat);

                            
                            // Update view transform
                            lookat.setLook({x: look[0], y: look[1], z: look[2] });
                            lookat.setEye({x: look[0] - eye3[0], y: look[1] - eye3[1], z: look[2] - eye3[2] });                            
                            
                            //lookat.setEye({x: eyeVec[0], y: eyeVec[1], z: eyeVec[2] });

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
            },

            destruct: function () {
                // TODO: remove mouse handlers
            }
        });


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

    });