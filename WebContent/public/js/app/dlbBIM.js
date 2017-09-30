//three.js版本的
var dlbBIM = {};
/**

 * 通过mesh的名字获取对象
 *
 * @param {name} three.js的MESH对象的属性值

 * @returns {obj} three.js的MESH对象
 
 */
dlbBIM.getName = function(name){
	return scene.getObjectByName(name);
}

/**

 * 通过mesh的名字执行fly to操作

 * @param {name} three.js的MESH对象的属性值

 * @returns {void}
 
 */
dlbBIM.flyToByName = function(name){
	 var t = this.getName(hisPick.name);
	 if(t){
		 this.flyTo(t);
	 }
}
/**

 * 通过mesh执行fly to操作

 * @param {obj} three.js的MESH对象

 * @returns {void}
 
 */
dlbBIM.flyTo = function(obj){
	obj.material = new THREE.MeshLambertMaterial( { color: new THREE.Color(0,1,1),opacity:0.5,transparent:true,side:THREE.DoubleSide} );
	var vec = new THREE.Vector3(camera.position.x, camera.position.y, camera.position.z).sub(center).normalize();
    var diag = this.getAABB3Diag(obj.geometry.boundingBox);
    var sca = Math.abs((diag) /  Math.tan(45*0.0174532925) );
    var pos = new THREE.Vector3( obj.position.x,obj.position.y,obj.position.z);
    var t1 = pos.add(obj.geometry.boundingSphere.center);
    var newCaram =  new THREE.Vector3();
    newCaram.x = t1.x + (vec.x * sca);
    newCaram.y = t1.y + (vec.y * sca);
    newCaram.z = t1.z + (vec.z * sca);
	dlbBIM.moveAndLookAt(camera, newCaram, t1, {duration: 1000}); 
}
/**

 * Gets the value of an attribute in the table.

 * @param {obj} three.js的MESH对象

 * @returns {void}
 
 */
dlbBIM.moveAndLookAt = function (camera, dstpos, dstlookat, options) {
	  options || (options = {duration: 300});
	  var origpos = new THREE.Vector3().copy(camera.position); // original position
	  var origrot = new THREE.Euler().copy(camera.rotation); // original rotation
	 
	  camera.position.set(dstpos.x, dstpos.y, dstpos.z);
	  camera.lookAt(dstlookat);
	  center = dstlookat;
	  
	  var dstrot = new THREE.Euler().copy(camera.rotation);
	  var qa = new THREE.Quaternion().copy(camera.quaternion); // src quaternion
	  var qb = new THREE.Quaternion().setFromEuler(dstrot); // dst quaternion
	  var qm = new THREE.Quaternion();
	  var t = {t: 0};

	  // reset original position and rotation
	  camera.position.set(origpos.x, origpos.y, origpos.z);
	  //camera.rotation.set(origrot.x, origrot.y, origrot.z);
	  // position
	 new TWEEN.Tween(camera.position).to({
	    x: dstpos.x,
	    y: dstpos.y,
	    z: dstpos.z
	  }, options.duration).onUpdate(function () {
	     THREE.Quaternion.slerp(qa, qb, qm,t);
	     camera.quaternion.set(qm.x, qm.y, qm.z, qm.w);
	 }).start().onComplete(function(){
		  controls.target.set(dstlookat.x, dstlookat.y, dstlookat.z);
		  controls.update();
	});
}

/**

 * 通过mesh的名字变色

 * @param {name} three.js的MESH对象的名字

 * @param {param} color{r:1,g:1,b:1,a:0} or{r:1,g:1,b:1,o:0}

 * @returns {void}

 */
dlbBIM.chageColor = function(name,param){
	 var t = this.getName(name);
	 if(t){
	 	 t.material.color.set(new THREE.Color(param.r,param.g,param.b));
		 t.material.opacity = param.a || param.o;
	 }
}
/**

 * 通过mesh变色

 * @param {obj} three.js的MESH对象

 * @param {param} color{r:1,g:1,b:1,a:0} or{r:1,g:1,b:1,o:0}

 * @returns {void}
 */
dlbBIM.chageObjColor = function(obj,param){
	 obj.material.color.set(new THREE.Color(param.r,param.g,param.b));
	 obj.material.opacity = param.a || param.o;
}

/**

 * 事件注册类

 */
dlbBIM.EventRegistry = function(){
	var o = this;
	o.registry = [];
};

dlbBIM.EventRegistry.prototype.register = function(fn) {
	var skip = false;
	this.registry.forEach(function(existing){
		if (existing == fn) {
			skip = true;
		}
	});
	if (!skip) {
		this.registry.push(fn);
	}
};

dlbBIM.EventRegistry.prototype.unregister = function(fn) { 
	var len = this.registry.length;
	while (len--) {
		if (this.registry[len] == fn) {
			this.registry.splice(len, 1);
		}
	}
};

dlbBIM.EventRegistry.prototype.size = function(){
	return this.registry.length;
};

dlbBIM.EventRegistry.prototype.trigger = function(callback){
	this.registry.forEach(callback);
};

dlbBIM.EventRegistry.prototype.clear = function(){
	this.registry = [];
};


/**

 * Gets the value of an attribute in the table.

 * @param {aabb} aabb

 * @returns {Number} 几何模型对角线长度
 */
dlbBIM.getAABB3Diag =  function (aabb) {
    return Math.abs(aabb.min.sub(aabb.max).length());
};

/**

 * 有错的方法

 * @param {aabb} aabb

 * @returns {THREE.Vector3}
 
 */
dlbBIM.getAABBCenter =  function (aabb) {
    return new THREE.Vector3( (aabb.min.x+ aabb.max.x)/2 , (aabb.min.y+ aabb.max.y)/2 ,(aabb.min.z+ aabb.max.z)/2);
};

/**

 * 鼠标点击事件.

 * @param {ev} 鼠标自带event事件

 * @returns {void}
 
 */
dlbBIM.onMouseUp = function(ev){
	event.preventDefault();
	mouse.x = ( event.clientX / window.innerWidth ) * 2 - 1;
	mouse.y = - ( event.clientY / window.innerHeight ) * 2 + 1;
	var vector = new THREE.Vector3( mouse.x, mouse.y, 0.5).unproject( camera );
	raycaster = new THREE.Raycaster(camera.position,vector.sub(new THREE.Vector3( camera.position.x, camera.position.y, camera.position.z)).normalize() );
	var intersections = raycaster.intersectObjects( scene.children );
	if ( intersections.length > 0 ) {
		if ( intersected != intersections[ 0 ].object ) {
			dlbBIM.chageColor(hisPick.name,hisPick.color);
			intersected = intersections[ 0 ].object;
			window.hisPick={
				name:intersected.name,	
				color:{
					r:intersected.material.color.r,
					g:intersected.material.color.g,
					b:intersected.material.color.b,
					o:intersected.material.opacity	
				}
			};
			dlbBIM.flyTo(intersected);
			if(typeof(pickEvent)){
				pickEvent.trigger(function(modelLoadedListener){
	        		modelLoadedListener(ev);
	        	});
			}
		}else {  
            if (intersected) intersectedmaterial = new THREE.MeshLambertMaterial({color: new THREE.Color(hisPick.color.r,hisPick.color.g,hisPick.color.b),opacity:window.hisPick.color.o,transparent:true})
            intersected = null;  
        }  
		document.body.style.cursor = 'pointer';
	}
    return false;
};

/**

 * Three.JS 从世界坐标系转换到屏幕坐标系

 * @param {pos} new THREE.Vector3(0,0,1)

 * @returns {x:100,y:200} 屏幕坐标
 
 */
dlbBIM.changeWorldToScreenPos = function(pos){

	var projector = new THREE.Projector();  
	
	var vector =new THREE.Vector3(pos.x,pos.y,pos.z).project(camera);  
	var halfWidth = window.innerWidth / 2;  
	var halfHeight = window.innerHeight / 2;  
	  
	return {  
	    x: Math.round(vector.x * halfWidth + halfWidth),  
	    y: Math.round(-vector.y * halfHeight + halfHeight)  
	};
}


dlbBIM.textlabels = [];

/**

 * Three.JS 创建HTML标签
 
 */
dlbBIM.createLable = function(){
    var div = document.createElement('div');
    div.className = 'text-label';
    div.style.position = 'absolute';
    div.innerHTML = "hi there!";
    return {
      element: div,
      parent: false,
      position: new THREE.Vector3(0,0,0),
      setClassName: function(classname) {
          this.element.className = classname;
      },
      setHTML: function(html) {
        this.element.innerHTML = html;
      },
      setParent: function(obj) {
        this.parent = obj;
      },
      updatePosition: function() {
        if(parent) {
          this.position.copy(this.parent.position);
        }
        var pos = new THREE.Vector3( this.position.x,this.position.y,this.position.z);
        var t1 = pos.add(this.parent.geometry.boundingSphere.center);
        var coords2d = dlbBIM.changeWorldToScreenPos(t1);
        this.element.style.left = coords2d.x + 'px';
        this.element.style.top = coords2d.y + 'px';
      }
    };
}

dlbBIM.createLableByMeshName = function(name,html){
	var text = dlbBIM.createLable();
	classname && (text.setClassName(classname));
	text.setHTML(html);
	text.setParent(dlbBIM.getName(name));
	dlbBIM.textlabels.push(text);
	container.appendChild(text.element);
}

