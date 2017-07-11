function GeometryLoader() {
	var o = this;
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
	
	o.ifcIds = [];
	
	this.createGeometry = function(geometryId,vertices, normals, colors, indices){
		var geometry = new xeogl.Geometry(xeogl.scene, { // http://xeoengine.org/docs/classes/Geometry.html
            id: "geometry." + geometryId,
            primitive: "triangles",
            positions: vertices,
            normals: normals,
            colors: colors,
            indices: indices
        });
	}
	
	this.createBimObject = function(geometryType , ifcProductOid , geometryDataOid , material, transformationMatrix){
		o.ifcIds.push(ifcProductOid);
		new xeogl.BIMObject(xeogl.scene, {
	        id: ifcProductOid,
	        geometryIds: geometryDataOid,
	        material:material,
	        matrix: transformationMatrix
	    });
		
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
			o.createGeometry(geometryDataOid, vertices, normals, colors, indices);
			o.createBimObject(geometryType , ifcProductOid ,[geometryDataOid], material, transformationMatrix)
		} else if(geometryType == 2){
			var nrParts = data.readInt();
			data.align8();
			var geometryIds = [];
			for (var i=0; i<nrParts; i++) {
				var coreId = data.readLong();
				var indices = data.readShortArray((data.readInt()));
				data.align4();
				var vertices = data.readFloatArray((data.readInt()));
				var normals = data.readFloatArray((data.readInt()));
				var colors = data.readFloatArray((data.readInt()));
				geometryIds.push(coreId);
				o.createGeometry(coreId, vertices, normals, colors, indices);
			}
			o.createBimObject(geometryType , ifcProductOid ,geometryIds, material, transformationMatrix);
		}else if(geometryType == 3){
            var geometryDataOid = data.readLong();
            o.createBimObject(geometryType , ifcProductOid ,[geometryDataOid], material, transformationMatrix);
		}else{
			var arraySize = data.readInt();
            var coreIds = [];
            for (var i=0;i<arraySize;i++) {
                coreIds.push(data.readLong())
            }
            o.createBimObject(geometryType , ifcProductOid ,coreIds, material, transformationMatrix);
		}

		o.state.nrObjectsRead++;
		o.updateProgress();
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
		o.center = [
			(o.modelBounds.max.x + o.modelBounds.min.x) / 2,
			(o.modelBounds.max.y + o.modelBounds.min.y) / 2,
			(o.modelBounds.max.z + o.modelBounds.min.z) / 2,
		];
		var zoom = Math.abs(o.modelBounds.max.x - o.modelBounds.min.x);
		var diagonal = Math.sqrt(Math.pow(o.modelBounds.max.x - o.modelBounds.min.x, 2) + Math.pow(o.modelBounds.max.y - o.modelBounds.min.y, 2) + Math.pow(o.modelBounds.max.z - o.modelBounds.min.z, 2));
		var far = diagonal * 18; // 5 being a guessed constant that should somehow coincide with the max zoom-out-factor
		var lookat = new xeogl.Lookat({
	        eye: [o.center[0], o.center[1] - diagonal, o.center[2]],
	        look: o.center,
	        up: [0,0,1],
	        gimbalLockY: false // Default is true
	    });
	    window.camera = new xeogl.Camera({
	    	view: lookat,
	        project: new xeogl.Perspective({
				far: far,
				near: far / 1000,
				aspect: jQuery(window).width() / jQuery(window).height(),
				fovy: 37.8493
		    })
	    });
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