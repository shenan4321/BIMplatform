if(typeof Ifc != 'object') {
	Ifc = {};
}
if(typeof Ifc.Constants != 'object') {
	Ifc.Constants = {};
}


/**
 * Time in milliseconds before a connect or login action will timeout
 */
Ifc.Constants.timeoutTime = 10000; // ms

/**
 * The default IFC Types to load
 */
Ifc.Constants.defaultTypes = [
	"IfcColumn",
	"IfcStair",
	"IfcSlab",
	"IfcWindow",
//	"IfcOpeningElement",
	"IfcDoor",
	"IfcBuildingElementProxy",
	"IfcWallStandardCase",
	"IfcWall",
	"IfcBeam",
	"IfcRailing",
	"IfcProxy",
	"IfcRoof"
];


Ifc.Constants.materials1 = {
	IfcSpace: {r: 0.5, g: 0.4, b: 0.1, a: 0.6},
	IfcRoof: {r: 70/255, g: 30/255, b: 30/255, a: 0.7},
	IfcSlab: {r: 160.0/255, g: 150/255, b:104.0/255, a: 0.7},
	IfcWall: {r: 150/255, g: 150/255, b: 104.0/255, a:0.6},
	IfcWallStandardCase: {r: 146.0/255, g: 72.0/255, b: 38.0/255, a:0.6},
	IfcDoor: {r: 0.637255, g:  0.603922, b: 0.670588, a: 0.6},
	IfcWindow: {r: 0, g: 0, b: 0.8, a: 0.5},
	IfcOpeningElement: {r: 0.137255, g: 0.403922, b: 0.870588, a: 0.5},
	IfcRailing: {r: 0.137255, g: 0.203922, b:0.270588, a: 0.6},
	IfcColumn: {r:  0, g:0, b: 0, a: 0.6},
	IfcBeam:{r: 0.137255, g: 0.403922, b:0.870588, a: 0.6},
	IfcBeamStandardCase: {r: 0.137255, g: 0.403922, b: 0.870588, a: 0.6},
	IfcFurnishingElement: {r: 205.0/255, g: 104.0/255, b: 57.0/255, a: 0.6},
	IfcCurtainWall: {r:0.5, g: 0.5, b: 0.5, a: 0.6},
	IfcStair: {r: 0, g: 0, b: 0, a: 0.6},
	IfcStairFlight: {r:  0, g:  0, b:0, a: 0.6},
	IfcBuildingElementProxy: {r: 0.0, g: 0.8, b: 0.5, a: 0.6},
	IfcFlowSegment: {r: 0.6, g: 0.4, b:0.5, a: 0.6},
	IfcFlowitting: {r: 0.137255, g: 0.403922, b: 0.870588, a: 0.6},
	IfcFlowTerminal: {r: 0.137255, g:  0.403922, b: 0.870588, a: 0.6},
	IfcProxy: {r: 0.0, g: 0.8, b: 0.5, a: 0.9},
	IfcSite: {r: 0.0, g: 102/255, b: 180/255, a: 0.8},
	IfcLightFixture: {r:  0.137255, g: 0.403922, b:  0.870588, a: 0.6},
	IfcDuctSegment: {r: 0.137255, g: 0.403922, b:0.870588, a: 0.6},
	IfcDistributionFlowElement: {r: 0.8470588235, g: 0.427450980392, b: 0, a: 0.6},
	IfcDuctFitting: {r: 0.137255, g: 0.403922, b:0.870588, a: 0.6},
	IfcPlate: {r: 0, g: 0, b:0, a: 0.5},
	IfcPile: {r:0.8470588235, g:  0.427450980392, b: 0, a: 0.6},
	IfcAirTerminal: {r: 0.8470588235, g: 0.427450980392, b: 0, a: 0.6},
	IfcMember: {r: 0.137255, g: 0.203922, b:0.270588, a: 0.6},
	IfcCovering: {r: 0.8470588235, g: 0.427450980392, b: 0, a: 0.6},
	IfcTransportElement: {r: 0.8470588235, g: 0.427450980392, b: 0, a: 0.6},
	IfcFlowController: {r: 0.8470588235, g: 0.427450980392, b: 0, a: 0.6},
	IfcFlowFitting: {r: 0.8470588235, g: 0.427450980392, b: 0, a: 0.6},
	IfcRamp: {r: 0.8470588235, g: 0.427450980392, b: 0, a: 0.6},
	IfcFurniture: {r: 0.8470588235, g: 0.427450980392, b: 0, a: 0.6},
	IfcFooting: {r: 0.8470588235, g: 0.427450980392, b: 0, a: 0.6},
	IfcSystemFurnitureElement: {r: 0.8470588235, g: 0.427450980392, b: 0, a: 0.6},
	IfcSpace: {r: 0.137255, g: 0.303922, b: 0.570588, a: 0.6},
	IfcBuildingElementPart: {r: 1, g: 0.5, b: 0.5, a: 0.6},
	IfcDistributionElement: {r: 1, g: 0.5, b: 0.5, a: 0.6},
	IfcProduct: {r:0.0, g: 102/255, b:102/255, a: 0.6},
	DEFAULT: {r: 0.8470588235, g: 0.427450980392, b: 0, a: 0.6}
}


Ifc.Constants.materials2 = {
		IfcSpace: {r: 0.5, g: 0.4, b: 0.1, a: 0.6},
		IfcRoof: {r: 142/255, g: 142/255, b: 130/255, a: 1},
		IfcSlab: {r: 204/255, g: 204/255, b:204/255, a: 1},
		IfcWall: {r: 205/255, g: 205/255, b: 205/255, a:1},
		IfcWallStandardCase: {r: 205/255, g: 205/255, b: 205/255, a:1},
		IfcDoor: {r: 0.55, g:0.55, b:0.5, a: 1},
		IfcWindow: {r: 0, g: 0.5, b: 1, a: 0.2},
		IfcOpeningElement: {r:0, g: 0, b: 0, a: 1},
		IfcRailing: {r: 153/255, g: 153/255, b:153/255, a: 1},
		IfcColumn: {r:102/255, g:102/255, b: 102/255, a: 1},
		IfcBeam:{r: 0, g: 0, b:0, a:0.6},
		IfcBeamStandardCase: {r: 0, g: 0, b: 0, a: 0.6},
		IfcFurnishingElement: {r: 142/255, g: 130/255, b: 102/255, a: 1},
		IfcCurtainWall: {r:0, g: 0, b: 0, a: 0.6},
		IfcStair: {r: 0, g: 0, b: 0, a: 0.6},
		IfcStairFlight: {r:  204/255, g:204/255, b:204/255, a:1},
		IfcBuildingElementProxy: {r: 51/255, g: 102/255, b: 0/255, a: 1},
		IfcFlowSegment: {r: 0, g: 0, b:0, a: 0.6},
		IfcFlowitting: {r: 0, g: 0, b: 0, a: 0.6},
		IfcFlowTerminal: {r: 0, g:  0, b: 0, a: 0.6},
		IfcProxy: {r: 0, g: 0, b: 0, a:0},
		IfcSite: {r:51/255, g: 102/255, b: 0/255, a: 1},
		IfcLightFixture: {r:  0, g: 0, b:  0, a: 0.6},
		IfcDuctSegment: {r: 0, g: 0, b:0, a: 0.6},
		IfcDistributionFlowElement: {r: 0, g: 0, b: 0, a: 0},
		IfcDuctFitting: {r: 0, g: 0, b:0, a: 0.6},
		IfcPlate: {r: 0.4, g: 0.6, b:0.8, a: 0.4},
		IfcPile: {r:0, g:  0, b: 0, a: 0.6},
		IfcAirTerminal: {r: 0, g: 0, b: 0, a: 0.6},
		IfcMember: {r: 0, g: 0, b:0, a: 0.6},
		IfcCovering: {r: 0.8, g: 0.8, b: 0.8, a: 1},
		IfcTransportElement: {r: 0, g: 0, b: 0, a: 0.6},
		IfcFlowController: {r: 0, g: 0, b: 0, a: 0.6},
		IfcFlowFitting: {r: 0, g: 0, b: 0, a: 0.6},
		IfcRamp: {r: 0, g: 0, b: 0, a: 0.6},
		IfcFurniture: {r: 0, g:0,b: 0, a: 0.6},
		IfcFooting: {r: 0.4,g: 0.4, b: 0.4, a: 1},
		IfcSystemFurnitureElement: {r: 0, g: 0, b: 0, a: 0.6},
		IfcSpace: {r: 0, g: 0, b: 0, a: 0.6},
		IfcBuildingElementPart: {r: 0, g: 0, b: 0, a: 0.6},
		IfcDistributionElement: {r: 0, g: 0, b: 0, a: 0.6},
		IfcProduct: {r:0, g: 0, b:0, a: 0.6},
		DEFAULT: {r: 0.8470588235, g: 0.427450980392, b: 0, a: 0.6}
}

Ifc.Constants.materials3 = {
		IfcSpace: {r: 0.5, g: 0.4, b: 0.1, a: 0.6},
		IfcRoof: {r: 70/255, g: 30/255, b: 30/255, a: 0.7},
		IfcSlab: {r: 153/255, g: 153/255, b:153/255, a: 1},
		IfcWall: {r: 153/255, g: 166/255, b: 153/255, a:0.6},
		IfcWallStandardCase: {r: 146.0/255, g: 145/255, b: 130/255, a:1},
		IfcDoor: {r: 0.8, g:  0.8, b:0.8, a: 1},
		IfcWindow: {r: 0, g: 0, b: 0, a: 0.5},
		IfcOpeningElement: {r:0.5, g: 0.5, b: 0.5, a: 1},
		IfcRailing: {r: 0.6, g: 0.6, b:0.6, a: 1},
		IfcColumn: {r:0.6, g:0.65, b: 0.6, a: 1},
		IfcBeam:{r: 0, g: 0, b:0, a:0.6},
		IfcBeamStandardCase: {r: 0, g: 0, b: 0, a: 0},
		IfcFurnishingElement: {r: 146/255, g: 145/255, b: 146/255, a: 1},
		IfcCurtainWall: {r:0, g: 0, b: 0, a: 0},
		IfcStair: {r: 0, g: 0, b: 0, a: 0.6},
		IfcStairFlight: {r:  0.4, g:0.4, b:0.4, a:1},
		IfcBuildingElementProxy: {r: 0.4, g: 0.6, b: 0.2, a: 1},
		IfcFlowSegment: {r: 0, g: 0, b:0, a: 0},
		IfcFlowitting: {r: 0, g: 0, b: 0, a: 0},
		IfcFlowTerminal: {r: 0, g:  0, b: 0, a: 0.6},
		IfcProxy: {r: 0, g: 0, b: 0, a:0},
		IfcSite: {r:153/255, g: 205/255, b: 153/255, a: 1},
		IfcLightFixture: {r:  0, g: 0, b:  0, a: 0},
		IfcDuctSegment: {r: 0, g: 0, b:0, a: 0},
		IfcDistributionFlowElement: {r: 0, g: 0, b: 0, a: 0},
		IfcDuctFitting: {r: 0, g: 0, b:0, a: 0},
		IfcPlate: {r: 0, g: 0, b:0, a: 0.5},
		IfcPile: {r:0, g:  0, b: 0, a: 0},
		IfcAirTerminal: {r: 0, g: 0, b: 0, a: 0},
		IfcMember: {r: 0, g: 0, b:0, a: 0.6},
		IfcCovering: {r: 0.6, g: 0.6, b: 0.6, a: 0.6},
		IfcTransportElement: {r: 0, g: 0, b: 0, a: 0},
		IfcFlowController: {r: 0, g: 0, b: 0, a: 0},
		IfcFlowFitting: {r: 0, g: 0, b: 0, a: 0},
		IfcRamp: {r: 0, g: 0, b: 0, a: 0},
		IfcFurniture: {r: 0, g:0,b: 0, a: 0},
		IfcFooting: {r: 0.6,g: 0.6, b: 0.6, a: 1},
		IfcSystemFurnitureElement: {r: 0, g: 0, b: 0, a: 0},
		IfcSpace: {r: 0, g: 0, b: 0, a: 0},
		IfcBuildingElementPart: {r: 0, g: 0, b: 0, a: 0},
		IfcDistributionElement: {r: 0, g: 0, b: 0, a: 0},
		IfcProduct: {r:0, g: 0, b:0, a: 0},
		DEFAULT: {r: 0.8470588235, g: 0.427450980392, b: 0, a: 0.6}
	}

var IfcMType = localStorage.IfcMType;
if(IfcMType){
	Ifc.Constants.materials = Ifc.Constants['materials'+ IfcMType];
}else{
	localStorage.setItem("IfcMType", 1);
	Ifc.Constants.materials = Ifc.Constants.materials1;
}
 







/*
 * Default camera settings
 */
Ifc.Constants.camera = {
	maxOrbitSpeed : Math.PI * 0.1,
	orbitSpeedFactor : 0.05,
	zoomSpeedFactor : 0.1,
	panSpeedFactor : 0.6
};

/*
 * Default markup for highlighted objects
 */
Ifc.Constants.highlightSelectedObject = {
	type : 'material',
	wire: true,
	id : 'highlight',
	emit : 0.0,
	baseColor : {r: 0.0, g: 1, b: 0}
}

/*
 * Default markup for highlighted special objects
 */
Ifc.Constants.highlightSelectedSpecialObject = {
	type : 'material',
	id : 'specialselectedhighlight',
	emit : 1,
	baseColor : {r: 0.16,g: 0.70,b: 0.88},
	shine : 10.0
};

/*
 * Enumeration for progressbar types
 */
Ifc.Constants.ProgressBarStyle = {
	Continuous: 1,
	Marquee: 2
}



/**
 * Returns a number whose value is limited to the given range.
 *
 * Example: limit the output of this computation to between 0 and 255
 * (x * 255).clamp(0, 255)
 *
 * @param {Number} s The number to clamp
 * @param {Number} min The lower boundary of the output range
 * @param {Number} max The upper boundary of the output range
 * @returns A number in the range [min, max]
 * @type Number
 */
Ifc.Constants.clamp = function(s, min, max) {
	return Math.min(Math.max(s, min), max);
};