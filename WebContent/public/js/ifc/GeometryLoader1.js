function GeometryLoader() {
	var o = this;
	this.models = {};
	o.state = {
		nrObjectsRead: 0,
		nrObjects: 0
	};
	o.stats = {
		nrPrimitives: 0,
		nrVertices: 0,
		nrNormals: 0,
		nrColors: 0
	};
	o.nodes = [];
	o.step = 1;
	var flagsObj = {
		transparent : true,
		backfaces:true,
		enable:true
	};
	this.setModels = function(data){
		o.models = data;
	}
	this.getModels = function(){
		return o.models;
	}
	this.readObject = function(data, geometryType) {
		var ifcname = data.readUTF8();
		var material  =  Ifc.Constants.materials[ifcname] || Ifc.Constants.materials['DEFAULT'];
		var ifcProductOid = data.readLong();//不同的
		data.align8();
        var transformationMatrix = data.readDoubleArray(16);
		if (geometryType == 1) {
			var geometryDataOid = data.readLong(); 
			var indices = data.readShortArray((data.readInt()));
			data.align4();
			var IndicesForLinesWireFrame = data.readShortArray((data.readInt()));
			data.align4();
			var vertices = data.readFloatArray((data.readInt()));
			var normals = data.readFloatArray((data.readInt()));
			var colors = data.readFloatArray((data.readInt()));
			o.library.addNodes([{
				coreId:'geo'+geometryDataOid,
			    type : "geometry",
			    primitive : "triangles",
			    positions: vertices,
			    indices: indices,
			    normals: normals
			},{
				coreId:'geoLine'+geometryDataOid,
				type : "geometry",
				primitive : "lines",
				positions: vertices,
				indices: IndicesForLinesWireFrame,
				normals: normals
			}]);
			this.processGeometry(1,ifcProductOid, [geometryDataOid], material, transformationMatrix)
		} else if(geometryType == 2){
			var nrParts = data.readInt();
			data.align8();
			var nodes = [];
			for (var i=0; i<nrParts; i++) {
				var coreId = data.readLong();
				var indices = data.readShortArray((data.readInt()));
				data.align4();
				var vertices = data.readFloatArray((data.readInt()));
				var normals = data.readFloatArray((data.readInt()));
				var colors = data.readFloatArray((data.readInt()));
				o.library.addNode({
					type:"geometry",
					primitive:"triangles",
					positions: vertices,
					indices: indices,
					coreId:'geo'+coreId,
					normals: normals
				});
			}
		}else if(geometryType == 3){
            var geometryDataOid = data.readLong();
            this.processGeometry(3,ifcProductOid, [geometryDataOid], material, transformationMatrix)
		}else{
			var arraySize = data.readInt();
            var coreIds = [];
            for (var i=0;i<arraySize;i++) {
            	var coreId = data.readLong();
                coreIds.push(coreId)
            }
            this.processGeometry(4,ifcProductOid, coreIds, material, transformationMatrix);
		}

		o.state.nrObjectsRead++;
		o.updateProgress();
		
	};

	this.processGeometry = function(type,ifcProductOid, coreIds, material, transformationMatrix){
		
		var coreNodes = [];
		var coreNodesLine = [];
		
		coreIds.forEach(function(coreId){
			coreNodes.push({
				type: "geometry",
				coreId: 'geo'+coreId
			});
			if(type==1||type==3){
				coreNodesLine.push({
					type: "geometry",
					coreId: 'geoLine'+coreId
				});
			}
		});
		var flags = {
			type : "flags",
			flags : flagsObj,
			id : "flags"+ifcProductOid,
			nodes : [{
				type : "name",
				name : ifcProductOid,
				nodes:[{
					type: "matrix",
					elements: transformationMatrix,
					nodes : [{
						type : "material",
						baseColor:{r:material.r,g:material.g,b:material.b},
                        color:{r:material.r,g:material.g,b:material.b},
						alpha: material.a,
						id:ifcProductOid+"geometry",
						nodes: coreNodes
					}]
				}]
			}]
		};
		
		if(type==1||type==3){
			flags.nodes[0].nodes[0].nodes[1] = {
				type : "material",
	            color:{r:0,g:0,b:0},
				alpha:0.5,
				nodes: coreNodesLine
			}
		}
		
		o.models.addNode(flags);
		flags = null;
	}

	this.updateProgress = function() {
		progress.update({title:'transferring',progress:o.state.nrObjectsRead,max:o.state.nrObjects});
	};

	this.readStart = function(data){
		var start = data.readUTF8();
		if (start != "BGS") {
			console.log("Stream does not start with BGS (" + start + ")");
			return false;
		}
		var version = data.readByte();
		if (version != 6) {
			console.log("Unimplemented version");
			return false;
		} else {
			o.state.version = version;
		}
		data.align8();
		var modelBounds = data.readDoubleArray(6);
		o.modelBounds = {
			min: {x: modelBounds[0], y: modelBounds[1], z: modelBounds[2]},
			max: {x: modelBounds[3], y: modelBounds[4], z: modelBounds[5]}
		};
		o.center = {
			x: (o.modelBounds.max.x + o.modelBounds.min.x) / 2,
			y: (o.modelBounds.max.y + o.modelBounds.min.y) / 2,
			z: (o.modelBounds.max.z + o.modelBounds.min.z) / 2,
		};
		var zoom = Math.abs(o.modelBounds.max.x - o.modelBounds.min.x);
		if (window.scene) {
			window.scene.getNode("lookAt",function(lookat){
				var eye = { x: (o.modelBounds.max.x - o.modelBounds.min.x) * 0.5, y: (o.modelBounds.max.y - o.modelBounds.min.y) * -1, z: (o.modelBounds.max.z - o.modelBounds.min.z) * 0.5 };
				lookat.setEye(eye);
				lookat.setLook(o.center);
			});
			window.scene.getNode("main-camera",function(maincamera){
				var diagonal = Math.sqrt(Math.pow(o.modelBounds.max.x - o.modelBounds.min.x, 2) + Math.pow(o.modelBounds.max.y - o.modelBounds.min.y, 2) + Math.pow(o.modelBounds.max.z - o.modelBounds.min.z, 2));
				var far = diagonal * 18; // 5 being a guessed constant that should somehow coincide with the max zoom-out-factor
				maincamera.setOptics({
					type: 'perspective', 
					far: far,
					near: far / 1000,
					aspect: jQuery(window).width() / jQuery(window).height(),
					fovy: 37.8493
				});
			}); 
			window.scene.getNode("cameras",function(cameras){
				console.log(cameras);
				cameras.zoom = -(zoom*10);
			});
		}
		o.library = scene.findNode("library");
		if (o.library == null) {
			o.library = scene.addNode({
				id: "library",
				type: "library"
			});
		}
		o.state.nrObjects = data.readInt();
		//o.step = o.state.nrObjects<100 ? 1 :~~(o.state.nrObjects/100);
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