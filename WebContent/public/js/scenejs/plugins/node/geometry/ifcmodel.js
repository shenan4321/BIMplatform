
(function() {

	SceneJS.Types.addType("geometry/ifcmodel", {

		construct : function(params) {
			this.addNode(build.call(this, params));
		}
	});

	function build(params) {
		
		var geometryInfo = params.geometry_info;
		
		var coreId = "geometry/ifcmodel_" + (params.wire ? "wire" : "_solid") + geometryInfo.oid;

		// If a node core already exists for a prim with the given properties,
		// then for efficiency we'll share that core rather than create another
		// geometry
		if (this.getScene().hasCore("geometry", coreId)) {
			return {
				type : "geometry",
				coreId : geometryInfo.oid
			};
		}

		var positions = geometryInfo.vertices;
		var indices = geometryInfo.indices;
		var indicesForLinesWireFrame=geometryInfo.indicesForLinesWireFrame;
		var normals = geometryInfo.normals;
		var typeName = geometryInfo.typeName;
		
		var material = Ifc.Constants.materials[typeName] || Ifc.Constants.materials['DEFAULT'];
//		var transformation = geometryInfo.base64Transformation;

//		var positionsArray = convertBase64ToFloat32Array(basePositions);
//		var indicesArray = convertBase64ToUint16Array(indices);
//		var normalsArray = convertBase64ToFloat32Array(normals);
//		var transformationByte = convertBase64ToBinary(transformation);
		
		var lookAt = this.getScene().getNode("main-lookAt");

        var boundMinX = params.boundMinX;
        var boundMinY = params.boundMinY;
        var boundMinZ = params.boundMinZ;
		var boundMaxX = params.boundMaxX;
        var boundMaxY = params.boundMaxY;
        var boundMaxZ = params.boundMaxZ;

        var eye = { x: (boundMaxX - boundMinX) * 0.5, y: (boundMaxY - boundMinY) * -1, z: (boundMaxZ - boundMinZ) * 0.5 };
        
        //lookAt.set("eye", eye);

        var maincamera = this.getScene().getNode("main-camera");

        var diagonal = Math.sqrt(Math.pow(boundMaxX - boundMinX, 2) + Math.pow(boundMaxY - boundMinY, 2) + Math.pow(boundMaxZ - boundMinZ, 2));

        var far = diagonal * 10; // 5 being a guessed constant that should somehow coincide with the max zoom-out-factor

        maincamera.setOptics({
            type: 'perspective',
            far: far,
            near: far / 1000,
            aspect: jQuery(this.getScene().getCanvas()).width() / jQuery(this.getScene().getCanvas()).height(),
            fovy: 37.8493
        });
        
        window.allPoint.push(geometryInfo.oid);
        
        var positionFloatArray = new Float32Array(positions);
        var normalsFloatArray = new Float32Array(normals);
        
        return {
			type : "flags",
			flags : {
				transparent : true,
				backfaces:true,
				enable:true
			},
			id : "flags"+geometryInfo.oid,
			nodes : [{
				type : "name",
				name : geometryInfo.oid,
				nodes : [{
					type : "material",
					baseColor: material,
					color: material,
					alpha: material.a,
        			id:geometryInfo.oid+"geometry",
					nodes: [{
	        			type : "geometry",
	        			primitive : params.wire ? "lines" : "triangles",
	                    positions: positionFloatArray,
	                    indices: new Uint16Array(indices),
	                    normals: normalsFloatArray
	        		},
	        		{
						type : "material",
						baseColor: material,
						color: {r:0,g:0,b:0},
						alpha: 0.5,
	        			id:"geometryLines"+geometryInfo.oid,
						nodes: [{
		        			type : "geometry",
		        			primitive : "lines",
		                    positions: positionFloatArray,
		                    indices: new Uint16Array(indicesForLinesWireFrame),
		                    normals: normalsFloatArray
		        		}]
					}]
				}]
			}]
		};
	}
	
	function convertBase64ToArrayBuffer(base64) {
		var raw = window.atob(base64);
		var rawLength = raw.length;
		var arraybuffer = new ArrayBuffer(rawLength);
		var array = new Uint8Array(arraybuffer);
		
		for(i = 0; i < rawLength; i++) {
		  array[i] = raw.charCodeAt(i);
		}
		return arraybuffer;
	}
	
	function convertBase64ToUint16Array(base64) {
		var arraybuffer = THREE.Base64.toArrayBuffer(base64);
		var array = new Uint16Array(arraybuffer);
		return array;
	}
	
	function convertBase64ToFloat32Array(base64) {
		var arraybuffer = THREE.Base64.toArrayBuffer(base64);
		var array = new Float32Array(arraybuffer);
		return array;
	}
	
	function binaryToString(binary) {
	    var error;

	    try {
	        return decodeURIComponent(escape(binary));
	    } catch (_error) {
	        error = _error;
	        if (error instanceof URIError) {
	            return binary;
	        } else {
	            throw error;
	        }
	    }
	}

	function base64ToBin(str) {
		var code = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".split("");
		var bitString = "";
		var tail = 0;
		for (var i = 0; i < str.length; i++) {
			if (str[i] != "=") {
				var decode = code.indexOf(str[i]).toString(2);
				bitString += (new Array(7 - decode.length)).join("0") + decode;
			} else {
				tail++;
			}
		}
		return bitString.substr(0, bitString.length - tail * 2);
	}

})();