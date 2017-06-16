function GeometryLoader() {
	var o = this;
	this.models = {};
	o.state = {
		nrObjectsRead: 0,
		nrObjects: 0
	};
	o.models1 = {}; //临时存储geometryType == 1的二进制变量,以便于3调用
	o.models2 = {}; //临时存储geometryType == 2的二进制变量,以便于4调用
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
			o.models1['geo'+geometryDataOid] = {
					geometry:[{
                        type : "geometry",
                        primitive : "triangles",
                        positions: vertices,
                        indices: indices,
                        normals: normals
                    }],
					line:{
						type : "material",
						baseColor: material,
						color: {r:0,g:0,b:0},
						alpha: 0.5,
						nodes: [{
							type : "geometry",
							primitive : "lines",
							positions: vertices,
							indices: IndicesForLinesWireFrame,
							normals: normals
						}]
					}
			};
			o.nodes.push({
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
                            baseColor: material,
                            color: material,
                            alpha: material.a,
                            id:ifcProductOid+"geometry",
                            nodes: o.models1['geo'+geometryDataOid].geometry
                        },o.models1['geo'+geometryDataOid].line]
					}]
				}]
			});
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
					o.models2['geo'+coreId] = {type:"geometry",primitive:"triangles",positions: vertices,indices: indices,normals: normals};
					nodes.push(o.models2['geo'+coreId]);
				}
				o.nodes.push({
					type : "flags",
					flags :flagsObj,
					id : "flags"+ifcProductOid,
					nodes : [{
						type : "name",
						name : ifcProductOid,
						nodes:[{
							type: "matrix",
							elements: transformationMatrix,
							nodes : [{
								type : "material",
								baseColor: material,
								color: material,
								alpha: material.a,
								id:ifcProductOid+"geometry",
								nodes: nodes
							}]
						}]
					}]
				});
		}else if(geometryType == 3){
            var geometryDataOid = data.readLong();
            var geo = o.models1['geo'+geometryDataOid];
            o.nodes.push({
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
                            baseColor: material,
                            color: material,
                            alpha: material.a,
                            id:ifcProductOid+"geometry",
                            nodes: geo.geometry
                        },geo.line]
					}]
				}]
			});
		}else{
			var arraySize = data.readInt();
            var nodes = [];
            for (var i=0;i<arraySize;i++) {
            	var coreId = data.readLong();
                nodes.push(o.models2['geo'+coreId]);
            }
            o.nodes.push( {
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
							baseColor: material,
							color: material,
							alpha: material.a,
							id:ifcProductOid+"geometry",
							nodes: nodes
						}]
					}]
				}]
			});
		}

		o.state.nrObjectsRead++;
		if(o.state.nrObjectsRead%o.step==0||o.state.nrObjectsRead==o.state.nrObjects){
			o.updateProgress();
			o.models.addNode({type:'flags',nodes:o.nodes});
			o.nodes = [];
			//o.models.addNode(o.nodes);
			//o.nodes= {};
		}
		
	};


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
		o.step = o.state.nrObjects<100 ? 1 :~~(o.state.nrObjects/100);
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