xeogl.BIMObject = xeogl.Component.extend({
    /**
     JavaScript class name for this xeogl.BIMObject.

     @property type
     @type String
     @final
     */
    type: "xeogl.BIMObject",

    // Constructor

    _init: function (cfg) {

        // Model this object belongs to, will be null when no model
        this.model = cfg.model; // xeogl.BIMModel

        // Modelling transform component
        this.transform = this.create({
            type: "xeogl.Transform",// http://xeoengine.org/docs/classes/Transform.html
            matrix: cfg.matrix
        });

        // Visibility control component.
        this.visibility = this.create({
            type: "xeogl.Visibility", // http://xeoengine.org/docs/classes/Visibility.html
            visible: true
        });
        
        // Material component
        this.material = this.create({
            /*type: "xeogl.PhongMaterial", // http://xeoengine.org/docs/classes/Material.html
            diffuse: [cfg.material.r,cfg.material.g,cfg.material.b], // Random color until we set for type
*/            type: "xeogl.MetallicMaterial",
            baseColor: [cfg.material.r,cfg.material.g,cfg.material.b],
//baseColor: [1,0,0],
            emissive:[cfg.material.r,cfg.material.g,cfg.material.b],
            metallic: 1.0,
            opacity: cfg.material.a,
        });

        // Rendering modes component
        this.modes = this.create({
            type: "xeogl.Modes", // http://xeoengine.org/docs/classes/Modes.html
            transparent:true,
            backfaces:true
        });

        // When highlighting, causes this object to render after non-highlighted objects
        this.stage = this.create({
            type: "xeogl.Stage",
            priority: 0
        });

        // When highlighting, we use this component to disable depth-testing so that this object
        // appears to "float" over non-highlighted objects
        this.depthBuf = this.create({
            type: "xeogl.DepthBuf",
            active: true
        });
        
        this.id = cfg.id;

        // Create a xeogl.Entity for each xeogl.Geometry
        // Each xeogl.Entity shares the components defined above

        // TODO: If all geometries are of same primitive, then we can combine them

        //this.entities = [];
        var entity;

        
        if(cfg.geometryIds.length==1){
        	
            entity = this.create({ // http://xeoengine.org/docs/classes/Entity.html
                type: "xeogl.Entity",
                id:'ifc'+this.id,
                meta: {
                    objectId: cfg.geometryIds[0]
                },
                modes:this.modes,
                lights:lights,
                camera: camera,
                geometry: "geometry." + cfg.geometryIds[0],
                transform: this.transform,
                visibility: this.visibility,
                material: this.material,
                stage: this.stage,
                depthBuf: this.depthBuf
            });

        }else{
        	var geometryBuilder = new xeogl.GeometryBuilder();
        	
        	for (var i = 0, len = cfg.geometryIds.length; i < len; i++) {
            	geometryBuilder.setShape(xeogl.scene.components["geometry." + cfg.geometryIds[i]])
            	geometryBuilder.addShape();
            }
        	
        	var geometry = new xeogl.Geometry();
            geometryBuilder.build(geometry);
            
        	entity = this.create({ // http://xeoengine.org/docs/classes/Entity.html
                type: "xeogl.Entity",
                id:'ifc'+this.id,
                modes:this.modes,
                lights:lights,
                camera: camera,
                geometry: geometry,
                transform: this.transform,
                visibility: this.visibility,
                material: this.material,
                stage: this.stage,
                depthBuf: this.depthBuf
            });

        	
        }

    },

    add: function(geometryId){
    	console.log(111);
        var entity = this.create({ // http://xeoengine.org/docs/classes/Entity.html
            type: "xeogl.Entity",
            meta: {
                objectId: this.id
            },
            lights:lights,
            camera: camera,
            geometry: "geometry." + geometryId,
            transform: this.transform,
            visibility: this.visibility,
            material: this.material,
            modes: this.modes,
            stage: this.stage,
            depthBuf: this.depthBuf
        });

        this.entities.push(entity);
    },

    // Define read-only properties of xeogl.BIMObject

    _props: {

        // World-space bounding volume
        worldBoundary: {
            get: function () {
                return this.entities[0].worldBoundary
            }
        },

        // View-space bounding volume
        viewBoundary: {
            get: function () {
                return this.entities[0].viewBoundary
            }
        },

        // Canvas-space bounding volume
        canvasBoundary: {
            get: function () {
                return this.entities[0].viewBoundary
            }
        },

        // Whether or not this object is highlighted
        highlighted: {
            set: function (highlight) {
                this.depthBuf.active = !highlight;
                this.stage.priority = highlight ? 2 : 0;
                this.material.emissive = highlight ? [0.5, 0.5, 0.5] : [0, 0, 0];
            }
        }
    }
});
