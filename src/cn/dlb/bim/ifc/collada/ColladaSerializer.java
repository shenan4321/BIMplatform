package cn.dlb.bim.ifc.collada;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.ProjectInfo;
import cn.dlb.bim.ifc.engine.RenderEngineException;
import cn.dlb.bim.ifc.engine.cells.Matrix;
import cn.dlb.bim.ifc.engine.cells.Vector3d;
import cn.dlb.bim.ifc.serializers.SerializerException;
import cn.dlb.bim.ifc.shared.ProgressReporter;
import cn.dlb.bim.models.geometry.GeometryData;
import cn.dlb.bim.models.geometry.GeometryInfo;
import cn.dlb.bim.models.store.SIPrefix;
import cn.dlb.bim.utils.UTF8PrintWriter;

public class ColladaSerializer extends AbstractGeometrySerializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ColladaSerializer.class);
	private static final Map<String, MaterialConvertor> convertors = new LinkedHashMap<String, MaterialConvertor>();
	private final Map<String, Set<IdEObject>> converted = new HashMap<String, Set<IdEObject>>();
	private SIPrefix lengthUnitPrefix;

	private static void addConvertor(MaterialConvertor convertor) {
		convertors.put(convertor.getIfcType(), convertor);
	}

	static {
		addConvertor(new MaterialConvertor("IfcRoof", new double[] { 153.0/255.0f, 0.0, 51.0/255.0f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcSlab", new double[] { 217.0/255.0f, 217.0/255.0f, 217.0/255.0f }, 1.0f));
		
		addConvertor(new MaterialConvertor("IfcWindow", new double[] { 0.2f, 0.2f, 0.8f }, 0.2f));
		addConvertor(new MaterialConvertor("IfcSpace", new double[] { 0.5f, 0.4f, 0.1f }, 0.0f));
		addConvertor(new MaterialConvertor("IfcDoor", new double[] { 0.637255f, 0.603922f, 0.670588f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcStair", new double[] { 0.637255f, 0.603922f, 0.670588f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcStairFlight", new double[] { 0.637255f, 0.603922f, 0.670588f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcFlowSegment", new double[] { 0.6f, 0.4f, 0.5f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcFurnishingElement", new double[] { 205.0/255.0f, 104.0/255.0f, 57.0/255.0f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcPlate", new double[] { 0.437255f, 0.603922f, 0.370588f }, 0.4f));
		addConvertor(new MaterialConvertor("IfcMember", new double[] { 0.137255f, 0.203922f, 0.270588f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcWallStandardCase", new double[] { 217.0/255.0f, 217.0/255.0f, 217.0/255.0f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcWall", new double[] { 217.0/255.0f, 217.0/255.0f, 217.0/255.0f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcCurtainWall", new double[] { 0.5f, 0.5f, 0.5f }, 0.5f));
		addConvertor(new MaterialConvertor("IfcRailing", new double[] { 0.137255f, 0.203922f, 0.270588f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcColumn", new double[] { 0.437255f, 0.603922f, 0.370588f, }, 1.0f));
		addConvertor(new MaterialConvertor("IfcBuildingElementProxy", new double[] { 0.0f, 0.5f, 0.0f }, 1.0f));
		
		addConvertor(new MaterialConvertor("IfcBeam", new double[] { 0.137255f, 0.403922f, 0.870588f }, 1.0f));
//		addConvertor(new Convertor<IfcBeamStandardCase>(IfcBeamStandardCase.class, new double[] { 0.137255f, 0.403922f, 0.870588f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcFlowTerminal", new double[] { 0.137255f, 0.403922f, 0.870588f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcProxy", new double[] { 0.137255f, 0.403922f, 0.870588f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcSite", new double[] { 0.0, 102.0f/255.0f, 102.0f/255.0f }, 1.0f));
//		addConvertor(new Convertor<IfcLightFixture>(IfcLightFixture.class, new double[] { 0.137255f, 0.403922f, 0.870588f }, 1.0f));
//		addConvertor(new Convertor<IfcDuctSegment>(IfcDuctSegment.class, new double[] { 0.137255f, 0.403922f, 0.870588f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcDistributionFlowElement", new double[] { 0.8470588235f, 0.427450980392f, 0.0f }, 1.0f));
//		addConvertor(new Convertor<IfcDuctFitting>(IfcDuctFitting.class, new double[] { 0.137255f, 0.403922f, 0.870588f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcPile", new double[] { 0.8470588235f, 0.427450980392f, 0.0f }, 1.0f));
//		addConvertor(new Convertor<IfcAirTerminal>(IfcAirTerminal.class, new double[] { 0.8470588235f, 0.427450980392f, 0.0f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcCovering", new double[] { 0.8470588235f, 0.427450980392f, 0.0f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcTransportElement", new double[] { 0.8470588235f, 0.427450980392f, 0.0f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcFlowController", new double[] { 0.8470588235f, 0.427450980392f, 0.0f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcFlowFitting", new double[] { 0.8470588235f, 0.427450980392f, 0.0f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcRamp", new double[] { 0.8470588235f, 0.427450980392f, 0.0f }, 1.0f));
//		addConvertor(new Convertor<IfcFurniture>(IfcFurniture.class, new double[] { 0.8470588235f, 0.427450980392f, 0.0f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcFooting", new double[] { 0.8470588235f, 0.427450980392f, 0.0f }, 1.0f));
//		addConvertor(new Convertor<IfcSystemFurnitureElement>(IfcSystemFurnitureElement.class, new double[] { 0.8470588235f, 0.427450980392f, 0.0f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcBuildingElementPart", new double[] { 1.0f, 0.5f, 0.5f }, 1.0f));
		addConvertor(new MaterialConvertor("IfcDistributionElement", new double[] { 1.0f, 0.5f, 0.5f }, 1.0f));
		
		addConvertor(new MaterialConvertor("IfcProduct", new double[] { 0.0, 102.0f/255.0f, 102.0f/255.0f }, 1.0f));
	}

	// Prepare a transformer for floating-point numbers into a strings, clipping extraneous zeros.
	private static final DecimalFormat decimalFormat = new DecimalFormat("#.##########");
	// Prepare a transformer for integers into strings.
	private static final DecimalFormat intFormat = new DecimalFormat("#");

	private Vector3d lowestObserved = new Vector3d();
	private Vector3d highestObserved = new Vector3d();

	@Override
	public void init(IfcModelInterface model, ProjectInfo projectInfo, boolean normalizeOids) throws SerializerException {
		super.init(model, projectInfo, normalizeOids);
		this.lengthUnitPrefix = getLengthUnitPrefix(model);
	}

	@Override
	public boolean write(OutputStream out, ProgressReporter progressReporter) throws SerializerException {
		if (getMode() == Mode.BODY) {
			PrintWriter writer = new UTF8PrintWriter(out);
			try {
				writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				writer.println("<COLLADA xmlns=\"http://www.collada.org/2008/03/COLLADASchema\" version=\"1.5.0\">");
				// Data sections.
				writeAssets(writer);
				writeCameras(writer);
				writeLights(writer);
				writeEffects(writer);
				writeMaterials(writer);
				writeGeometries(writer);
				writeVisualScenes(writer);
				writeScene(writer);
				// End of root section.
				writer.print("</COLLADA>");
				writer.flush();
			} catch (Exception e) {
				LOGGER.error("", e);
			}
			writer.flush();
			setMode(Mode.FINISHED);
			return true;
		} else if (getMode() == Mode.FINISHED) {
			return false;
		} else if (getMode() == Mode.HEADER) {
			setMode(Mode.BODY);
			return true;
		}
		return false;
	}

	// Provide a date formatter.
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");

	private void writeAssets(PrintWriter out) {
		// Produce a date based on now.
		String date = dateFormat.format(new Date());
		// Determine what a meter is and what to logically refer to that transformation as.
		Number unitMeter = (lengthUnitPrefix == null) ? 1 : Math.pow(10.0, lengthUnitPrefix.getValue());
		String unitName = (lengthUnitPrefix == null) ? "meter" : lengthUnitPrefix.name().toLowerCase();
		// Write the asset block.
		out.println(" <asset>");
		out.println("  <contributor>");
		out.println("   <author>" + (getProjectInfo() == null ? "" : "linfujun") + "</author>");
		out.println("   <authoring_tool>BIMplatform</authoring_tool>");
		out.println("   <comments>" + (getProjectInfo() == null ? "" : getProjectInfo().getDescription()) + "</comments>");
		out.println("   <copyright>Copyright</copyright>");
		out.println("  </contributor>");
		out.println("  <created>" + date + "</created>");
		out.println("  <modified>" + date + "</modified>");
		out.println("  <unit meter=\"" + unitMeter + "\" name=\"" + unitName + "\"/>");
		out.println("  <up_axis>Z_UP</up_axis>");
		out.println(" </asset>");
	}

	private void writeGeometries(PrintWriter out) throws RenderEngineException, SerializerException {
		// Prepare the section.
		out.println(" <library_geometries>");
		// For each IfcProduct, get the geometry for each object in the product.
		
		EClass productClass = (EClass) model.getPackageMetaData().getEClass("IfcProduct");

		Set<IdEObject> convertedObjects = new HashSet<IdEObject>();

		for (MaterialConvertor materialConvertor : convertors.values()) {
			String ifcType = materialConvertor.getIfcType();
			for (IdEObject ifcProduct : model.getAllWithSubTypes(model.getPackageMetaData().getEClass(ifcType))) {
				if (!convertedObjects.contains(ifcProduct)) {
					convertedObjects.add(ifcProduct);
					setGeometry(out, ifcProduct, materialConvertor.getIfcType());
				}
			}
		}
		
		// Close the section.
		out.println(" </library_geometries>");
	}

	private void setGeometry(PrintWriter out, IdEObject ifcProductObject, String material) throws RenderEngineException, SerializerException {
		// Mostly just skips IfcOpeningElements which one would probably not want to end up in the Collada file.
		if (isInstanceOf(ifcProductObject, "IfcFeatureElementSubtraction") || isInstanceOf(ifcProductObject, "IfcSpace")) {
			return;
		}
		//
		
		GeometryInfo geometryInfo = (GeometryInfo) ifcProductObject.eGet(ifcProductObject.eClass().getEStructuralFeature("geometry"));
		if (geometryInfo != null && geometryInfo.getTransformation() != null) {
			GeometryData geometryData = geometryInfo.getData();
			ByteBuffer indicesBuffer = ByteBuffer.wrap(geometryData.getIndices());
			indicesBuffer.order(ByteOrder.LITTLE_ENDIAN);
			// TODO: In Blender (3d modeling tool) and Three.js, normals are ignored in favor of vertex order. The incoming geometry seems to be in order 0 1 2 when it needs to be in 1 0 2. Need more test cases.
			// Failing order: (0, 1050, 2800), (0, 1050, 3100), (3580, 1050, 3100)
			// Successful order: (0, 1050, 3100), (0, 1050, 2800), (3580, 1050, 3100)
			List<Integer> list = new ArrayList<Integer>();
			while (indicesBuffer.hasRemaining())
				list.add(indicesBuffer.getInt());
			indicesBuffer.rewind();
			for (int i = 0; i < list.size(); i += 3)
			{
				Integer first = list.get(i);
				Integer next = list.get(i + 1);
				list.set(i, next);
				list.set(i + 1, first);
			}
			// Positions the X or the Y or the Z of (X, Y, Z).
			ByteBuffer positionsBuffer = ByteBuffer.wrap(geometryData.getVertices());
			positionsBuffer.order(ByteOrder.LITTLE_ENDIAN);
			// Do pass to find highest Z for considered objects.
			while (positionsBuffer.hasRemaining())
			{
				float x = positionsBuffer.getFloat();
				float y = positionsBuffer.getFloat();
				float z = positionsBuffer.getFloat();
				// X
				if (x > highestObserved.x)
					highestObserved.x = x;
				else if (x < lowestObserved.x)
					lowestObserved.x = x;
				// Y
				if (y > highestObserved.y)
					highestObserved.y = y;
				else if (y < lowestObserved.y)
					lowestObserved.y = y;
				// Z
				if (z > highestObserved.z)
					highestObserved.z = z;
				else if (z < lowestObserved.z)
					lowestObserved.z = z;
			}
			positionsBuffer.rewind();
			//
			ByteBuffer normalsBuffer = ByteBuffer.wrap(geometryData.getNormals());
			normalsBuffer.order(ByteOrder.LITTLE_ENDIAN);
			// Create a geometry identification number in the form of: geom-320450
			long oid = ifcProductObject.getOid();
			String id = String.format("geom-%d", oid);
			// If the material doesn't exist in the converted map, add it.
			if (!converted.containsKey(material))
				converted.put(material, new HashSet<IdEObject>());
			// Add the current IfcProduct to the appropriate entry in the material map.
			converted.get(material).add(ifcProductObject);
			// Name for geometry.
			Object globalIdObj = ifcProductObject.eGet(ifcProductObject.eClass().getEStructuralFeature("GlobalId"));
			String name = (globalIdObj == null) ? "[NO_GUID]" : (String) globalIdObj;
			// Counts.
			int vertexComponentsTotal = positionsBuffer.capacity() / 4, normalComponentsTotal = normalsBuffer.capacity() / 4;
			int verticesCount = positionsBuffer.capacity() / 12, normalsCount = normalsBuffer.capacity() / 12, triangleCount = indicesBuffer.capacity() / 12;
			// Vertex scalars as one long string: 4.05 2 1 55.0 34.01 2
			String stringPositionScalars = byteBufferToFloatingPointSpaceDelimitedString(positionsBuffer);
			// Normal scalars as one long string: 4.05 2 1 55.0 34.01 2
			String stringNormalScalars = byteBufferToFloatingPointSpaceDelimitedString(normalsBuffer); //doubleBufferToFloatingPointSpaceDelimitedString(flippedNormalsBuffer);
			// Vertex indices as one long string: 1 0 2 0 3 2 5 4 6
			String stringIndexScalars = listToSpaceDelimitedString(list, intFormat);
			// Write geometry block for this IfcProduct (i.e. IfcRoof, IfcSlab, etc).
			out.println(" <geometry id=\"" + id + "\" name=\"" + name + "\">");
			out.println("  <mesh>");
			out.println("   <source id=\"positions-" + oid + "\" name=\"positions-" + oid + "\">");
			out.println("    <float_array id=\"positions-array-" + oid + "\" count=\"" + vertexComponentsTotal + "\">" + stringPositionScalars + "</float_array>");
			out.println("    <technique_common>");
			out.println("     <accessor count=\"" + verticesCount + "\" offset=\"0\" source=\"#positions-array-" + oid + "\" stride=\"3\">");
			out.println("      <param name=\"X\" type=\"float\"></param>");
			out.println("      <param name=\"Y\" type=\"float\"></param>");
			out.println("      <param name=\"Z\" type=\"float\"></param>");
			out.println("     </accessor>");
			out.println("    </technique_common>");
			out.println("   </source>");
			out.println("   <source id=\"normals-" + oid + "\" name=\"normals-" + oid + "\">");
			out.println("    <float_array id=\"normals-array-" + oid + "\" count=\"" + normalComponentsTotal + "\">" + stringNormalScalars + "</float_array>");
			out.println("    <technique_common>");
			out.println("     <accessor count=\"" + normalsCount + "\" offset=\"0\" source=\"#normals-array-" + oid + "\" stride=\"3\">");
			out.println("      <param name=\"X\" type=\"float\"></param>");
			out.println("      <param name=\"Y\" type=\"float\"></param>");
			out.println("      <param name=\"Z\" type=\"float\"></param>");
			out.println("     </accessor>");
			out.println("    </technique_common>");
			out.println("   </source>");
			out.println("   <vertices id=\"vertices-" + oid + "\">");
			out.println("    <input semantic=\"POSITION\" source=\"#positions-" + oid + "\"/>");
			out.println("    <input semantic=\"NORMAL\" source=\"#normals-" + oid + "\"/>");
			out.println("   </vertices>");
			out.println("   <triangles count=\"" + triangleCount + "\" material=\"Material-" + oid + "\">");
			out.println("    <input offset=\"0\" semantic=\"VERTEX\" source=\"#vertices-" + oid + "\"/>");
			out.println("    <p>" + stringIndexScalars + "</p>");
			out.println("   </triangles>");
			out.println("  </mesh>");
			out.println(" </geometry>");
		}
	}

	@SuppressWarnings("unused")
	private String doubleBufferToFloatingPointSpaceDelimitedString(DoubleBuffer buffer) {
		// For each scalar in the buffer, turn it into a string, adding it to the overall list.
		List<String> stringScalars = doubleBufferToStringList(buffer, decimalFormat);
		// Send back a space-delimited list of the strings: 1 2.45 0
		return StringUtils.join(stringScalars, " ");
	}

	private String byteBufferToFloatingPointSpaceDelimitedString(ByteBuffer buffer) {
		// For each scalar in the buffer, turn it into a string, adding it to the overall list.
		List<String> stringScalars = floatBufferToStringList(buffer, decimalFormat);
		// Send back a space-delimited list of the strings: 1 2.45 0
		return StringUtils.join(stringScalars, " ");
	}

	@SuppressWarnings("unused")
	private String byteBufferToIntPointSpaceDelimitedString(ByteBuffer buffer) {
		// Prepare to store integers as a list of strings.
		List<String> stringScalars = intBufferToStringList(buffer, intFormat);
		// Send back a space-delimited list of the strings: 1 2 0
		return StringUtils.join(stringScalars, " ");
	}

	private List<String> doubleBufferToStringList(DoubleBuffer buffer, Format formatter) {
		// Transform the array into a list.
		List<Float> list = new ArrayList<Float>();
		while (buffer.hasRemaining())
			list.add(new Float(buffer.get()));
		// Get the data as a list of String objects.
		return listToStringList(list, formatter);
	}

	private List<String> floatBufferToStringList(ByteBuffer buffer, Format formatter) {
		// Transform the array into a list.
		List<Float> list = new ArrayList<Float>();
		while (buffer.hasRemaining())
			list.add(new Float(buffer.getFloat()));
		// Get the data as a list of String objects.
		return listToStringList(list, formatter);
	}

	private List<String> intBufferToStringList(ByteBuffer buffer, Format formatter) {
		List<Integer> list = new ArrayList<Integer>();
		while (buffer.hasRemaining())
			list.add(new Integer(buffer.getInt()));
		// Get the data as a list of String objects.
		return listToStringList(list, formatter);
	}

	private String floatArrayToSpaceDelimitedString(float[] matrix) {
		List<Float> floatMatrix = floatArrayToFloatList(matrix);
		List<String> list = listToStringList(floatMatrix, decimalFormat);
		// Get data as space-delimited string: 1.004 5.0 24.00145
		return StringUtils.join(list, " ");
	}

	private String doubleArrayToSpaceDelimitedString(double[] matrix) {
		List<Double> floatMatrix = doubleArrayToFloatList(matrix);
		List<String> list = listToStringList(floatMatrix, decimalFormat);
		// Get data as space-delimited string: 1.004 5.0 24.00145
		return StringUtils.join(list, " ");
	}

	private List<Float> floatArrayToFloatList(float[] array) {
		List<Float> list = new ArrayList<Float>();
		for (float f : array)
			list.add(new Float(f));
		return list;
	}

	private List<Double> doubleArrayToFloatList(double[] array) {
		List<Double> list = new ArrayList<Double>();
		for (double f : array)
			list.add(new Double(f));
		return list;
	}
	
	private String listToSpaceDelimitedString(List<?> list, Format formatter) {
		List<String> stringScalars = listToStringList(list, formatter);
		return StringUtils.join(stringScalars, " ");
	}

	private List<String> listToStringList(List<?> list, Format formatter) {
		// Prepare to store floating-points as a list of strings.
		List<String> stringScalars = new ArrayList<String>();
		// For each scalar in the buffer, turn it into a string, adding it to the overall list.
		for (Object scalar : list) {
			String scalarAsString = formatter.format(scalar);
			stringScalars.add(scalarAsString);
		}
		return stringScalars;
	}

	private void writeScene(PrintWriter out) {
		out.println(" <scene>");
		out.println("  <instance_visual_scene url=\"#VisualSceneNode\"/>");
		out.println(" </scene>");
	}
	
	
	//String leftLightLocationString = String.format("%f %f %f", leftLightLocation.x(), leftLightLocation.y(), leftLightLocation.z());
	@SuppressWarnings("unused")
	private void writeVisualScenes(PrintWriter out) {
		// Open the section.
		out.println(" <library_visual_scenes>");
		out.println("  <visual_scene id=\"VisualSceneNode\" name=\"VisualSceneNode\">");
		// Write each IFC object as a node entry (maps to a displayed object).
		for (String material : converted.keySet()) {
			Set<IdEObject> ids = converted.get(material);
			for (IdEObject product : ids) {
				GeometryInfo geometryInfo = (GeometryInfo) product.eGet(product.eClass().getEStructuralFeature("geometry"));
				if (geometryInfo != null && geometryInfo.getTransformation() != null) {
					out.println("   <node id=\"node-" + product.getOid() + "\" name=\"node-" + product.getOid() + "\">");
					printMatrix(out, geometryInfo);
					out.println("    <instance_geometry url=\"#geom-" + product.getOid() + "\">");
					out.println("     <bind_material>");
					out.println("      <technique_common>");
					out.println("       <instance_material symbol=\"Material-" + product.getOid() + "\" target=\"#" + material + "Material\"/>");
					out.println("      </technique_common>");
					out.println("     </bind_material>");
					out.println("    </instance_geometry>");
					out.println("   </node>");
				}
			}
		}
		// Create convenience variables to simplify the perceived complexity of the equations.
		float lx = (float) lowestObserved.x, ly = (float) lowestObserved.y, lz = (float) lowestObserved.z;
		float hx = (float) highestObserved.x, hy = (float) highestObserved.y, hz = (float) highestObserved.z;
		// Derive useful information from the observed boundary of the IFC objects.
		Vector3d delta = new Vector3d(hx, hy, hz);
		delta.sub(lowestObserved);
		// Move the light left (-x) and back (-y) and up (+z) at 20% of the size of the observed objects. 
		Vector3d leftLightLocation = new Vector3d(lx - (0.2 * delta.x), ly - (0.2 * delta.y), hz + (0.2 * delta.z));
		float x = (float) leftLightLocation.x, y = (float) leftLightLocation.y, z = (float) leftLightLocation.z;
		// Move left (-x) and back (-y) at 500% and up (+z) at 200% of the light (so that the objects are in sensing range of the camera). 
		Vector3d leftCameraLocation = new Vector3d(5 * x, 5 * y, 2 * z);
		float cx = (float) leftCameraLocation.x, cy = (float) leftCameraLocation.y, cz = (float) leftCameraLocation.z;
		// TODO: Three.js doesn't seem to care about the camera and the light.
		// Include the camera.
		out.println("   <node id=\"Camera\" name=\"Camera\">");
		out.println("    <translate>"+ cx + " " + cy + " " + cz + "</translate>");
		out.println("    <rotate>"+ 0f + " " + 0f + " " + 1f + " " + -45f + "</rotate>");
		out.println("    <rotate>"+ 1f + " " + 0f + " " + 0f + " " + 45f + "</rotate>");
		out.println("    <instance_camera url=\"#PerspCamera\"/>");
		out.println("   </node>");
		// Include the light.
		out.println("   <node id=\"Light\" name=\"Light\">");
		out.println("    <translate>"+ x + " " + y + " " + z + "</translate>");
		out.println("    <rotate>"+ 0f + " " + 0f + " " + 1f + " " + 225f + "</rotate>");
		out.println("    <rotate>"+ 0f + " " + 1f + " " + 0f + " " + 45f + "</rotate>");
		out.println("    <instance_light url=\"#light-lib\"/>");
		out.println("   </node>");
		// Close the section.
		out.println("  </visual_scene>");
		out.println(" </library_visual_scenes>");
	}

	private void printMatrix(PrintWriter out, GeometryInfo geometryInfo) {
		ByteBuffer transformation = ByteBuffer.wrap(geometryInfo.getTransformation());
		transformation.order(ByteOrder.LITTLE_ENDIAN);
		DoubleBuffer doubleBuffer = transformation.asDoubleBuffer();
		// Prepare to create the transform matrix.
		double[] matrix = new double[16];
		// Add the first 16 values of the buffer.
		for (int i = 0; i < matrix.length; i++)
			matrix[i] = doubleBuffer.get();
		// Switch from column-major (x.x ... x.y ... x.z ... 0 ...) to row-major orientation (x.x x.y x.z 0 ...)?
		matrix = Matrix.changeOrientation(matrix);
		// List all 16 elements of the matrix as a single space-delimited String object.
		out.println("    <matrix>" + doubleArrayToSpaceDelimitedString(matrix) + "</matrix>");
	}

	private void writeEffects(PrintWriter out) {
		out.println(" <library_effects>");
		for (MaterialConvertor convertor : convertors.values())
			writeEffect(out, convertor.getIfcType(), convertor.getColors(), convertor.getOpacity());
		out.println(" </library_effects>");
	}

	private void writeEffect(PrintWriter out, String name, double[] colors, double transparency) {
		out.println("  <effect id=\"" + name + "-fx\">");
		out.println("   <profile_COMMON>");
		out.println("    <technique sid=\"common\">");
		out.println("     <phong>");
		out.println("      <emission>");
		out.println("       <color>0 0 0 1</color>");
		out.println("      </emission>");
		out.println("      <ambient>");
		out.println("       <color>0 0 0 1</color>");
		out.println("      </ambient>");
		out.println("      <diffuse>");
		out.println("       <color>" + colors[0] + " " + colors[1] + " " + colors[2] + " " + transparency + "</color>");
		out.println("      </diffuse>");
		out.println("      <specular>");
		out.println("       <color>0.5 0.5 0.5 1</color>");
		out.println("      </specular>");
		
		if (transparency < 1.0) {
			out.println("      <transparent opaque=\"RGB_ZERO\">");
			out.println("       <color>0.4980392 0.4980392 0.4980392 0.4980392</color>");
			out.println("      </transparent>");
			out.println("      <transparency>");
			out.println("       <float>1</float>");
			out.println("      </transparency>");
			out.println("      <shininess>");
			out.println("       <float>64</float>");//max 128
			out.println("      </shininess>");
		} else {
			out.println("      <shininess>");
			out.println("       <float>16</float>");
			out.println("      </shininess>");
		}
		
		out.println("      <reflective>");
		out.println("       <color>0 0 0 1</color>");
		out.println("      </reflective>");
		out.println("      <reflectivity>");
		out.println("       <float>0.5</float>");
		out.println("      </reflectivity>");
		out.println("      <index_of_refraction>");
		out.println("       <float>0</float>");
		out.println("      </index_of_refraction>");
		out.println("     </phong>");
		out.println("    </technique>");
		out.println("   </profile_COMMON>");
		out.println("  </effect>");
	}

	private void writeLights(PrintWriter out) {
		// TODO: Lights defined in library_lights of COLLADA file must be used in some visual_scene to prevent crashes with collada2gltf (stand-alone OpenGL Transmission Format exporter).
		out.println(" <library_lights>");
		out.println("  <light id=\"light-lib\" name=\"light\">");
		out.println("   <technique_common>");
		out.println("    <directional>");
		out.println("     <color>1 1 1</color>");
		out.println("    </directional>");
		out.println("   </technique_common>");
		out.println("   <technique profile=\"MAX3D\">");
		out.println("    <intensity>1.000000</intensity>");
		out.println("   </technique>");
		out.println("  </light>");
		out.println(" </library_lights>");
	}

	private void writeCameras(PrintWriter out) {
		out.println(" <library_cameras>");
		out.println("  <camera id=\"PerspCamera\" name=\"PerspCamera\">");
		out.println("   <optics>");
		out.println("    <technique_common>");
		out.println("     <perspective>");
		out.println("      <yfov>37.8493</yfov>");
		out.println("      <aspect_ratio>1</aspect_ratio>");
		out.println("      <znear>10</znear>");
		out.println("      <zfar>1000</zfar>");
		out.println("     </perspective>");
		out.println("    </technique_common>");
		out.println("   </optics>");
		out.println("  </camera>");
		out.println("  <camera id=\"testCameraShape\" name=\"testCameraShape\">");
		out.println("   <optics>");
		out.println("    <technique_common>");
		out.println("     <perspective>");
		out.println("      <yfov>37.8501</yfov>");
		out.println("      <aspect_ratio>1</aspect_ratio>");
		out.println("      <znear>0.01</znear>");
		out.println("      <zfar>1000</zfar>");
		out.println("     </perspective>");
		out.println("    </technique_common>");
		out.println("   </optics>");
		out.println("  </camera>");
		out.println(" </library_cameras>");
	}

	private void writeMaterials(PrintWriter out) {
		out.println(" <library_materials>");
		for (MaterialConvertor convertor : convertors.values())
			writeMaterial(out, convertor.getIfcType());
		out.println(" </library_materials>");
	}

	private void writeMaterial(PrintWriter out, String materialName) {
		out.println("  <material id=\"" + materialName + "Material\" name=\"" + materialName + "Material\">");
		out.println("   <instance_effect url=\"#" + materialName + "-fx\"/>");
		out.println("  </material>");
	}

	private SIPrefix getLengthUnitPrefix(IfcModelInterface model) {
		SIPrefix lengthUnitPrefix = null;
		boolean prefixFound = false;
		Map<Long, IdEObject> objects = model.getObjects();
		for (IdEObject object : objects.values()) {
			if (isInstanceOf(object, "IfcProject")) {
				IdEObject unitsInContext = (IdEObject) object.eGet(object.eClass().getEStructuralFeature("UnitsInContext"));
//				IfcUnitAssignment unitsInContext = ((IfcProject) object).getUnitsInContext();
				if (unitsInContext != null) {
					List units = (List) unitsInContext.eGet(unitsInContext.eClass().getEStructuralFeature("Units"));
//					EList<IfcUnit> units = unitsInContext.getUnits();
					for (Object unit : units) {
						IdEObject ifcSIUnit = (IdEObject) unit;
						if (isInstanceOf(ifcSIUnit, "IfcSIUnit")) {
							EStructuralFeature feature = ifcSIUnit.eClass().getEStructuralFeature("UnitType");
							Enumerator unitType = (Enumerator) ifcSIUnit.eGet(feature);
							if (unitType.getLiteral() == "LENGTHUNIT") {
								prefixFound = true;
								Enumerator prefix = (Enumerator) ifcSIUnit.eGet(ifcSIUnit.eClass().getEStructuralFeature("Prefix"));
								switch (prefix.getLiteral()) {
								case "EXA":
									lengthUnitPrefix = SIPrefix.EXAMETER;
									break;
								case "PETA":
									lengthUnitPrefix = SIPrefix.PETAMETER;
									break;
								case "TERA":
									lengthUnitPrefix = SIPrefix.TERAMETER;
									break;
								case "GIGA":
									lengthUnitPrefix = SIPrefix.GIGAMETER;
									break;
								case "MEGA":
									lengthUnitPrefix = SIPrefix.MEGAMETER;
									break;
								case "KILO":
									lengthUnitPrefix = SIPrefix.KILOMETER;
									break;
								case "HECTO":
									lengthUnitPrefix = SIPrefix.HECTOMETER;
									break;
								case "DECA":
									lengthUnitPrefix = SIPrefix.DECAMETER;
									break;
								case "DECI":
									lengthUnitPrefix = SIPrefix.DECIMETER;
									break;
								case "CENTI":
									lengthUnitPrefix = SIPrefix.CENTIMETER;
									break;
								case "MILLI":
									lengthUnitPrefix = SIPrefix.MILLIMETER;
									break;
								case "MICRO":
									lengthUnitPrefix = SIPrefix.MICROMETER;
									break;
								case "NANO":
									lengthUnitPrefix = SIPrefix.NANOMETER;
									break;
								case "PICO":
									lengthUnitPrefix = SIPrefix.PICOMETER;
									break;
								case "FEMTO":
									lengthUnitPrefix = SIPrefix.FEMTOMETER;
									break;
								case "ATTO":
									lengthUnitPrefix = SIPrefix.ATTOMETER;
									break;
								case "NULL":
									lengthUnitPrefix = SIPrefix.METER;
									break;
								}
								break;
							}
						}
					}
				}
			}
			if (prefixFound)
				break;
		}
		return lengthUnitPrefix;
	}
	
	private Boolean isInstanceOf(IdEObject originObject, String type) {
		PackageMetaData packageMetaData = model.getPackageMetaData();
		EClass eClass = packageMetaData.getEClass(type);
		return eClass.isSuperTypeOf(originObject.eClass());
	}
}
