function GeometryLoader(bimServerApi, models, viewer, type) {
	var o = this;
	o.models = models;
	o.bimServerApi = bimServerApi;
	o.viewer = viewer;
	o.state = {};
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
		console.log(ifcname);
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
		o.progressListeners.forEach(function(progressListener){
			progressListener("Loading", -1);
		});

//		if (o.state.nrObjectsRead < o.state.nrObjects) {
//			var progress = Math.ceil(100 * o.state.nrObjectsRead / o.state.nrObjects);
//			if (progress != o.state.lastProgress) {
//				o.progressListeners.forEach(function(progressListener){
//					progressListener(progress, o.state.nrObjectsRead, o.state.nrObjects);
//				});
//				o.viewer.SYSTEM.events.trigger('progressChanged', [progress]);
//				o.state.lastProgress = progress;
//			}
//		} else {
//			o.viewer.SYSTEM.events.trigger('progressDone');
//			o.progressListeners.forEach(function(progressListener){
//				progressListener("done", o.state.nrObjectsRead, o.state.nrObjects);
//			});
//			o.viewer.events.trigger('sceneLoaded', [scene]);
//			o.bimServerApi.call("ServiceInterface", "cleanupLongAction", {topicId: o.topicId}, function(){
//			});
//		}
	};
	
	this.downloadInitiated = function(){
		o.state = {
			mode: 0,
			nrObjectsRead: 0,
			nrObjects: 0
		};
//		o.viewer.SYSTEM.events.trigger('progressStarted', ['Loading Geometry']);
//		o.viewer.SYSTEM.events.trigger('progressBarStyleChanged', BIMSURFER.Constants.ProgressBarStyle.Continuous);
		
//		o.viewer.refreshMask();

		o.library = scene.findNode("library-" + o.groupId);
		if (o.library == null) {
			o.library = scene.addNode({
				id: "library-" + o.groupId,
				type: "library"
			});
		}
		
		var msg = {
			topicId: o.topicId
		};
		
		o.bimServerApi.setBinaryDataListener(o.topicId, o.binaryDataListener);
		o.bimServerApi.downloadViaWebsocket(msg);
	};
	
	this.binaryDataListener = function(data){
		o.todo.push(data);
	};
	
	this.readEnd = function(data){
		if (Object.keys(o.dataToInfo).length > 0) {
			console.error("Unsolved links");
			for (var key in o.dataToInfo) {
				console.log(key, o.dataToInfo[key]);
			}
		}
		
//		o.boundsTranslate = scene.findNode("bounds_translate");
//
//		var center = {
//			x: (o.modelBounds.max.x + o.modelBounds.min.x) / 2,
//			y: (o.modelBounds.max.y + o.modelBounds.min.y) / 2,
//			z: (o.modelBounds.max.z + o.modelBounds.min.z) / 2,
//		};
//		
//		o.boundsTranslate.x = -o.center.x;
//		o.boundsTranslate.y = -o.center.y;
//		o.boundsTranslate.z = -o.center.z;
//
//		var lookat = scene.findNode("main-lookAt");
//		var eye = { x: (o.modelBounds.max.x - o.modelBounds.min.x) * 0.5, y: (o.modelBounds.max.y - o.modelBounds.min.y) * -1, z: (o.modelBounds.max.z - o.modelBounds.min.z) * 0.5 };
//		lookat.set("eye", eye);
//		
//		var maincamera = scene.findNode("main-camera");
//		
//		var diagonal = Math.sqrt(Math.pow(o.modelBounds.max.x - o.modelBounds.min.x, 2) + Math.pow(o.modelBounds.max.y - o.modelBounds.min.y, 2) + Math.pow(o.modelBounds.max.z - o.modelBounds.min.z, 2));
//		
//		var far = diagonal * 5; // 5 being a guessed constant that should somehow coincide with the max zoom-out-factor
//		
//		maincamera.setOptics({
//			type: 'perspective',
//			far: far,
//			near: far / 1000,
//			aspect: jQuery(o.viewer.canvas).width() / jQuery(o.viewer.canvas).height(),
//			fovy: 37.8493
//		});
		
		console.log(o.stats);
		
		o.viewer.SYSTEM.events.trigger('progressDone');
		o.progressListeners.forEach(function(progressListener){
			progressListener("done", o.state.nrObjectsRead, o.state.nrObjectsRead);
		});
		o.bimServerApi.call("ServiceInterface", "cleanupLongAction", {topicId: o.topicId}, function(){
		});
	}
	
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
		o.state.mode = 1;
	};
	
	this.process = function(res){
			inputStream = new DataInputStream(res);
			var messageType = inputStream.readByte();
			if (messageType == 0) {
				o.readStart(inputStream);
			} else if (messageType == 6) {
				o.readEnd(inputStream);
			} else{
				o.readObject(inputStream, messageType);
			}
			//data = o.todo.shift();
		//}
	};
	
	this.progressHandler = function(topicId, state){
		if (topicId == o.topicId) {
			if (state.title == "Done preparing") {
				if (!o.prepareReceived) {
					o.prepareReceived = true;
					o.downloadInitiated();
				}
			}
			if (state.state == "FINISHED") {
				o.bimServerApi.unregisterProgressHandler(o.topicId, o.progressHandler);
			}
			o.progressListeners.forEach(function(progressListener){
				progressListener("Loading" + (o.options.title == null ? "" : " " + o.options.title) + "...", state.progress);
			});
		}
	};
	
	this.setTitle = function(title) {
		o.options.title = title;
	}

	this.setLoadOids = function(roids, oids) {
		o.options = {type: "oids", roids: roids, oids: oids};
	}

	this.start = function(){
		if (o.options != null) {
			o.groupId = o.options.roids[0];
			
			o.infoToOid = {};
			
			var oids = [];
			o.options.oids.forEach(function(object){
				if (object.gid != null) {
					o.infoToOid[object.gid] = object.oid;
					oids.push(object.gid);
				}
			});

			if (oids.length > 0) {
				var query = {
					type: "GeometryInfo",
					oids: oids,
					include: {
						type: "GeometryInfo",
						field: "data"
					}
				};
				o.bimServerApi.getSerializerByPluginClassName("org.bimserver.serializers.binarygeometry.BinaryGeometryMessagingStreamingSerializerPlugin3", function(serializer){
					o.bimServerApi.call("ServiceInterface", "download", {
						roids: o.options.roids,
						serializerOid : serializer.oid,
						sync : false,
						query: JSON.stringify(query)
					}, function(topicId){
						o.topicId = topicId;
						o.bimServerApi.registerProgressHandler(o.topicId, o.progressHandler);
					});
				});
			}
		}
	};
}