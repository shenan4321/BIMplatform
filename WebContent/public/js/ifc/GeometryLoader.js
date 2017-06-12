function GeometryLoader(bimServerApi, models, viewer, type) {
	var o = this;
	o.models = models;
	o.bimServerApi = bimServerApi;
	o.viewer = viewer;
	o.state = {
		nrObjectsRead: 0,
		nrObjects: 0
	};
	o.progressListeners = [];
	o.objectAddedListeners = [];
	o.prepareReceived = false;
	o.todo = [];
	o.type = type;

	if (o.type == null) {
		o.type = "triangles";
	}

	o.stats = {
		nrPrimitives: 0,
		nrVertices: 0,
		nrNormals: 0,
		nrColors: 0
	};

	// GeometryInfo.oid -> GeometryData.oid
//	o.infoToData = {};

	// GeometryData.oid -> [GeometryInfo.oid]
	o.dataToInfo = {};

	// Loaded geometry, GeometryData.oid -> Boolean
	o.loadedGeometry = {};

	// GeometryInfo.oid -> IfcProduct.oid
	o.infoToOid = {};

	this.addProgressListener = function(progressListener) {
		o.progressListeners.push(progressListener);
	};

	this.readObject = function(data, geometryType) {
		var pos1 = data.getPos();
		var ifcname = data.readUTF8();
		var pos2 = data.getPos();
		var rid = data.readInt();
		var geometryDataOid = data.readLong();
		data.align8();
		if (geometryType == 2) {
			var coreIds = [];
			var nrParts = data.readInt();
			data.align8();
			var objectBounds = data.readDoubleArray(6);
			var nodes = [];
			var nodesLines = [];
			for (var i=0; i<nrParts; i++) {
				var coreId = data.readLong();
				coreIds.push(coreId);
				var nrIndices = data.readInt();
				o.stats.nrPrimitives += nrIndices / 3;
				var indices = data.readShortArray(nrIndices);
				data.align4();
				var nrVertices = data.readInt();
				o.stats.nrVertices += nrVertices;
				var vertices = data.readFloatArray(nrVertices);
				var nrNormals = data.readInt();
				o.stats.nrNormals += nrNormals;
				var normals = data.readFloatArray(nrNormals);
				var nrColors = data.readInt();
				o.stats.nrColors += nrColors;
				var colors = data.readFloatArray(nrColors);
				nodes.push({type : "geometry",primitive : "triangles",positions: vertices,indices: indices,normals: normals});
				//nodesLines.push({type : "geometry",primitive : "l",positions: vertices,indices: indices,normals: normals});
			}

			var material  =  Ifc.Constants.materials[ifcname] || Ifc.Constants.materials['DEFAULT'];

			window.scene.getNode("my-lights",function(xxx){
				xxx.addNode({
					type : "flags",
					flags : {
						transparent : true,
						backfaces:true,
						enable:true
					},
					id : "flags"+geometryDataOid,
					nodes : [{
						type : "name",
						name : geometryDataOid,
						nodes : [{
							type : "material",
							baseColor: material,
							color: material,
							alpha: material.a,
							id:geometryDataOid+"geometry",
							nodes: nodes
						}]
					}]
				});
			});


		} else if (geometryType == 1) {
			if (ifcname == "IfcSite") {
				console.log("IfcSite");
			}
			var modelBounds = data.readDoubleArray(6);
			var oid = data.readLong()
			var nrIndices = data.readInt();
			var indices = data.readShortArray(nrIndices);

			o.stats.nrPrimitives += nrIndices / 3;

			data.align4();

			var nrIndicesForLinesWireFrame = data.readInt();
			var IndicesForLinesWireFrame = data.readShortArray(nrIndicesForLinesWireFrame);

			data.align4();
			var nrVertices = data.readInt();
			var vertices = data.readFloatArray(nrVertices);
			o.stats.nrVertices += nrVertices;
			var nrNormals = data.readInt();
			o.stats.nrNormals += nrNormals;
			var normals = data.readFloatArray(nrNormals);
			var nrColors = data.readInt();
			o.stats.nrColors += nrColors;
			var colors = data.readFloatArray(nrColors);
			var material  =  Ifc.Constants.materials[ifcname] || Ifc.Constants.materials['DEFAULT'];
			window.scene.getNode("my-lights",function(xxx){
				xxx.addNode({
					type : "flags",
					flags : {
						transparent : true,
						backfaces:true,
						enable:true
					},
					id : "flags"+geometryDataOid,
					nodes : [{
						type : "name",
						name : geometryDataOid,
						nodes : [{
							type : "material",
							baseColor: material,
							color: material,
							alpha: material.a,
							id:geometryDataOid+"geometry",
							nodes: [{
								type : "geometry",
								primitive : "triangles",
								positions: vertices,
								indices: indices,
								normals: normals
							}]
						},
							{
								type : "material",
								baseColor: material,
								color: {r:0,g:0,b:0},
								alpha: 0.5,
								id:"geometryLines"+geometryDataOid,
								nodes: [{
									type : "geometry",
									primitive : "lines",
									positions: vertices,
									indices: IndicesForLinesWireFrame,
									normals: normals
								}]
							}]
					}]
				});
			});

		}

		o.state.nrObjectsRead++;
		o.updateProgress();
	};

	this.convertToLines = function(indices) {
		var lineIndices = [];
		for (var i=0; i<indices.length; i+=3) {
			var i1 = indices[i];
			var i2 = indices[i+1];
			var i3 = indices[i+2];

			lineIndices.push(i1, i2);
			lineIndices.push(i2, i3);
			lineIndices.push(i3, i1);
		}
		return lineIndices;
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
		if(!window.scene){
			createScene(o.center,zoom);
		}
		if (window.scene) {
			window.scene.getNode("lookAt",function(lookat){
				var eye = { x: (o.modelBounds.max.x - o.modelBounds.min.x) * 0.5, y: (o.modelBounds.max.y - o.modelBounds.min.y) * -1, z: (o.modelBounds.max.z - o.modelBounds.min.z) * 0.5 };
				lookat.setEye(eye);
				lookat.setLook(o.center);
			});
			window.scene.getNode("main-camera",function(maincamera){
				maincamera.setOptics({
					type: 'perspective',
					far: 900000000,
					near: 132,
					aspect: jQuery(window).width() / jQuery(window).height(),
					fovy: 37.8493
				});
			});
		}
		o.state.nrObjects = data.readInt();
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