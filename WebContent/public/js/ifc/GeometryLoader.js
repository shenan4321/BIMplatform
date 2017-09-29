function GeometryLoader() {
	var o = this;
	o.state = {
		nrObjectsRead: 0,
		nrObjects: 0
	};
	
	o.gId = {};
	o.gList = {};
	o.geoList = {};
	
	this.createGeometry = function(geometryId,vertices, normals, colors, indices){
		var geometry = new THREE.BufferGeometry();
		geometry.addAttribute( 'position', new THREE.BufferAttribute( vertices, 3 ) );
		geometry.addAttribute( 'normal', new THREE.BufferAttribute( normals, 3 ) );
		geometry.addAttribute( 'color', new THREE.BufferAttribute( colors, 3 ) );
		geometry.setIndex( new THREE.BufferAttribute( new Uint16Array(indices), 1 ) );
		geometry.computeBoundingBox();
		geometry.computeBoundingSphere();
		o.gId["Mesh." + geometryId] = geometry;
	}

	
	function arrayMax( array ) {
		if ( array.length === 0 ) return - Infinity;
		var max = array[ 0 ];
		for ( var i = 1, l = array.length; i < l; ++ i ) {
			if ( array[ i ] > max ) max = array[ i ];
		}
		return max;
	}
	
	
	this.createGeometry1 = function(geometryId,vertices, normals, colors, indices){
		var geometry = new THREE.BufferGeometry();
		geometry.addAttribute( 'position', new THREE.BufferAttribute( new Float32Array(vertices), 3 ) );
		geometry.addAttribute( 'normal', new THREE.BufferAttribute( new Float32Array(normals), 3 ) );
		geometry.addAttribute( 'color', new THREE.BufferAttribute( new Float32Array(colors), 3 ) );
		if(arrayMax(indices)>65535){
			geometry.setIndex( new THREE.BufferAttribute( new Uint32Array(indices), 1 ) );
		}else{
			geometry.setIndex( new THREE.BufferAttribute( new Uint16Array(indices), 1 ) );
		}
		geometry.computeBoundingBox();
		o.gId["Mesh." + geometryId] = geometry;
	}
	
	
	this.createGeometryLine = function(geometryId,vertices,indices){
		var geometry = new THREE.BufferGeometry();
		geometry.addAttribute( 'position', new THREE.BufferAttribute( new Float32Array(vertices) , 3 ) );
		if(arrayMax(indices)>65535){
			geometry.setIndex( new THREE.BufferAttribute( new Uint32Array(indices), 1 ) );
		}else{
			geometry.setIndex( new THREE.BufferAttribute( new Uint16Array(indices), 1 ) );
		}
		o.gId["Line." + geometryId] = geometry;
	}
	
	this.createBimObject = function(ifcProductOid , geometryDataOid , material, transformationMatrix,boundingBox){

		
		var geometry = o.gId["Mesh." + geometryDataOid].clone();
		
		
		var object = new THREE.Mesh(geometry,material);
		


		object.name = 'Mesh'+ ifcProductOid;
		object.applyMatrix(new THREE.Matrix4().set(
						transformationMatrix[0],
						transformationMatrix[4],
						transformationMatrix[8],
						transformationMatrix[12],
						transformationMatrix[1],
						transformationMatrix[5],
						transformationMatrix[9],
						transformationMatrix[13],
						transformationMatrix[2],
						transformationMatrix[6],
						transformationMatrix[10],
						transformationMatrix[14],
						transformationMatrix[3],
						transformationMatrix[7],
						transformationMatrix[11],
						transformationMatrix[15]
		));
		
		scene.add(object);
	}

	
	this.createBimLine = function(ifcProductOid , geometryDataOid ,transformationMatrix){
		if(o.gId["Line." + geometryDataOid]){
			var geometry = o.gId["Line." + geometryDataOid].clone();
			var material =  new THREE.LineBasicMaterial({ color: 0x404040, linewidth: 1 ,opacity:.3,transparent:true});
			var object = new THREE.LineSegments(geometry,material);
			object.name = 'Line'+ ifcProductOid;
			object.applyMatrix(new THREE.Matrix4().set(
							transformationMatrix[0],
							transformationMatrix[4],
							transformationMatrix[8],
							transformationMatrix[12],
							transformationMatrix[1],
							transformationMatrix[5],
							transformationMatrix[9],
							transformationMatrix[13],
							transformationMatrix[2],
							transformationMatrix[6],
							transformationMatrix[10],
							transformationMatrix[14],
							transformationMatrix[3],
							transformationMatrix[7],
							transformationMatrix[11],
							transformationMatrix[15]
			));
			
			scene.add( object );
			
		}
		
	}
	
	
	this.index = 0;
	this.readObject = function(data, geometryType) {	        
			if (geometryType == 2) {
				data.align8();
				//var rid = data.readLong();
				var oid = data.readLong();
				/*var modelBounds = data.readDoubleArray(6);
				o.modelBounds = {
					min: {x: modelBounds[0], y: modelBounds[1], z: modelBounds[2]},
					max: {x: modelBounds[3], y: modelBounds[4], z: modelBounds[5]}
				};*/
				data.align8();
				o.geoList[oid] = {};
				o.geoList[oid].transformationMatrix =  data.readDoubleArray(16);
				o.geoList[oid].geometryDataOid = data.readLong();
				
				/*o.geoList[oid].modelBounds = {
					min: {x: modelBounds[0], y: modelBounds[1], z: modelBounds[2]},
					max: {x: modelBounds[3], y: modelBounds[4], z: modelBounds[5]}
				};*/
			}else if(geometryType == 4){
				
				data.align8();
				
				var geometryDataOid = data.readLong();
				
				var nrParts = data.readInt();
				
				
				var geometryIds = [];
				var geometryindices = [];
				var geometryvertices = [];
				var geometrynormals = [];
				var geometrycolors = [];
				var indicesBump;
				
				for (var i=0; i<nrParts; i++) {
					var coreId = data.readLong();
					indicesBump = geometryvertices.length/3;
					var indices = data.readShortArray((data.readInt()));
					data.align4();
					
					var vertices = data.readFloatArray((data.readInt()));
					
					var normals = data.readFloatArray((data.readInt()));
					
					var colors = data.readFloatArray((data.readInt()));
					
					geometryvertices.push.apply(geometryvertices,vertices);
					geometrynormals.push.apply(geometrynormals,normals);
					geometrycolors.push.apply(geometrycolors,colors);
					
					for (var j = 0, len = indices.length; j < len; j++) {
						geometryindices.push(indices[j] + indicesBump);
	                }
					
				}
				
				o.createGeometry1(geometryDataOid, geometryvertices, geometrynormals, geometrycolors, geometryindices);
				
			} else if(geometryType == 3){
				data.align8();
				var geometryDataOid = data.readLong();
				
				
				var indices = data.readShortArray((data.readInt()));
				
				data.align4();
				
				var IndicesForLinesWireFrame = data.readShortArray((data.readInt()));
				data.align4();
				
				
				var vertices = data.readFloatArray((data.readInt()));
				var normals = data.readFloatArray((data.readInt()));
				var colors = data.readFloatArray((data.readInt()));
				//var colors = data.readInt();
				o.createGeometry(geometryDataOid, vertices, normals, colors, indices);
				o.createGeometryLine(geometryDataOid, vertices, IndicesForLinesWireFrame);
				
			}else if(geometryType == 1){
			
				var ifcname = data.readUTF8();
				var material  =  Ifc.Constants.materials[ifcname] || Ifc.Constants.materials['DEFAULT'];
				material = new THREE.MeshPhongMaterial({color: new THREE.Color(material.r,
            			material.g,
            			material.b),
            			specular:new THREE.Color(material.r,
                    			material.g,
                    			material.b),
           			opacity:material.a,
           			transparent:true});
				var oid = data.readLong();//不同的
				var gid = data.readLong();
				o.gList[oid] = {};
				o.gList[oid].gInfoId = gid;
				o.gList[oid].gMaterial = material;
			}
			o.state.nrObjectsRead++;
			o.updateProgress();

	};


	this.updateProgress = function() {
		progress.update({title:'transferring',progress:o.state.nrObjectsRead,max:o.state.nrObjects});
	};

	this.readStart = function(data){
		//var MessageTypeINIT = data.readByte();
		var start = data.readUTF8();
		if (start != "BGS") {
			console.log("Stream does not start with BGS (" + start + ")");
			return false;
		}
		var version = data.readByte();
		/*if (version != 6) {
			console.log("Unimplemented version");
			return false;
		} else {
			o.state.version = version;
		}*/
		data.align4();
		var modelBounds = data.readDoubleArray(6);
		o.modelBounds = {
			min: {x: modelBounds[0], y: modelBounds[1], z: modelBounds[2]},
			max: {x: modelBounds[3], y: modelBounds[4], z: modelBounds[5]}
		};
		o.center = [
			(o.modelBounds.max.x + o.modelBounds.min.x) / 2,
			(o.modelBounds.max.y + o.modelBounds.min.y) / 2,
			(o.modelBounds.max.z + o.modelBounds.min.z) / 2,
		];
		var zoom = Math.abs(o.modelBounds.max.x - o.modelBounds.min.x);
		o.diagonal = Math.sqrt(Math.pow(o.modelBounds.max.x - o.modelBounds.min.x, 2) + Math.pow(o.modelBounds.max.y - o.modelBounds.min.y, 2) + Math.pow(o.modelBounds.max.z - o.modelBounds.min.z, 2));
		o.far = o.diagonal * 18; // 5 being a guessed constant that should somehow coincide with the max zoom-out-factor
	    camera = new THREE.PerspectiveCamera( 37.8493, jQuery(window).width() / jQuery(window).height(), o.far / 1000, o.far );
		
	    camera.position.x = o.center[0];
		camera.position.y = o.center[1] - o.diagonal;
		camera.position.z = o.center[2];
		camera.lookAt(new THREE.Vector3(o.center[0],o.center[1],o.center[2]));
		window.center = new THREE.Vector3(o.center[0],o.center[1],o.center[2]);
		camera.up.x = 0;
		camera.up.y = 0;
		camera.up.z = 1;
		controls = new THREE.OrbitControls( camera, renderer.domElement );
		controls.target.set(o.center[0],o.center[1],o.center[2]);
		controls.update();
	};
	
	this.process = function(res){
		inputStream = new DataInputStream(res);
		var messageType = inputStream.readByte();
		if (messageType == 0) {
			o.readStart(inputStream);
		}else{
			o.readObject(inputStream, messageType);
		}
		
	};

}