package cn.dlb.bim.common;

import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import cn.dlb.bim.deserializers.DeserializeException;
import cn.dlb.bim.deserializers.IfcStepDeserializer;
import cn.dlb.bim.emf.IfcModelInterface;
import cn.dlb.bim.emf.IfcModelInterfaceException;
import cn.dlb.bim.emf.PackageMetaData;
import cn.dlb.bim.emf.Schema;
import cn.dlb.bim.engine.EntityNotFoundException;
import cn.dlb.bim.engine.IRenderEngine;
import cn.dlb.bim.engine.IRenderEngineInstance;
import cn.dlb.bim.engine.IRenderEngineModel;
import cn.dlb.bim.engine.IndexFormat;
import cn.dlb.bim.engine.Precision;
import cn.dlb.bim.engine.RenderEngineException;
import cn.dlb.bim.engine.RenderEngineFilter;
import cn.dlb.bim.engine.RenderEngineGeometry;
import cn.dlb.bim.engine.RenderEngineSettings;
import cn.dlb.bim.engine.geometry.GenerateGeometryResult;
import cn.dlb.bim.engine.geometry.Matrix;
import cn.dlb.bim.ifc.BasicIfcModel;
import cn.dlb.bim.models.geometry.GeometryData;
import cn.dlb.bim.models.geometry.GeometryFactory;
import cn.dlb.bim.models.geometry.GeometryInfo;
import cn.dlb.bim.models.geometry.Vector3f;
import cn.dlb.bim.models.ifc2x3tc1.IfcProduct;
import cn.dlb.bim.serializers.IfcStepSerializer;
import cn.dlb.bim.serializers.ProgressReporter;
import cn.dlb.bim.serializers.Serializer;
import cn.dlb.bim.serializers.SerializerException;
import cn.dlb.bim.serializers.SerializerInputstream;

public class GeometryGenerator {
	private static final Logger LOGGER = LoggerFactory.getLogger(GeometryGenerator.class);
	private IfcModelInterface model;
	private Serializer serializer;
	private IRenderEngine renderEngine;
	private IRenderEngineModel renderEngineModel;
	private final Map<Integer, GeometryData> hashes = new ConcurrentHashMap<Integer, GeometryData>();

	public GeometryGenerator(IfcModelInterface model, Serializer serializer, IRenderEngine renderEngine) {
		this.model = model;
		this.serializer = serializer;
		this.renderEngine = renderEngine;
	}

	public void generateForAllElements() {
		try {
			serializer.init(model, null, true);
			InputStream in = new SerializerInputstream(serializer);
			renderEngineModel = renderEngine.openModel(in);
			final RenderEngineSettings settings = new RenderEngineSettings();
			settings.setPrecision(Precision.SINGLE);
			settings.setIndexFormat(IndexFormat.AUTO_DETECT);
			settings.setGenerateNormals(true);
			settings.setGenerateTriangles(true);
			settings.setGenerateWireFrame(false);
			
			final RenderEngineFilter renderEngineFilter = new RenderEngineFilter();
			
			renderEngineModel.setSettings(settings);
			renderEngineModel.setFilter(renderEngineFilter);
			
			renderEngineModel.generateGeneralGeometry();
			
			for (IfcProduct ifcProduct : model.getAllWithSubTypes(IfcProduct.class)) {
				generateGeometry(ifcProduct);
			}
		} catch (SerializerException e) {
			e.printStackTrace();
		} catch (RenderEngineException e) {
			e.printStackTrace();
		}
	}

	private GenerateGeometryResult generateGeometry(IfcProduct ifcProduct) {
		GenerateGeometryResult generateGeometryResult = new GenerateGeometryResult();
		if (ifcProduct.getRepresentation() != null && ifcProduct.getRepresentation().getRepresentations().size() != 0) {
			try {
				IRenderEngineInstance renderEngineInstance = renderEngineModel.getInstanceFromExpressId(ifcProduct.getExpressId());
				RenderEngineGeometry geometry = renderEngineInstance.generateGeometry();
				boolean translate = true;
				if (geometry != null && geometry.getNrIndices() > 0) {
					GeometryInfo geometryInfo = null;
					geometryInfo = GeometryFactory.eINSTANCE.createGeometryInfo();

					geometryInfo.setMinBounds(createVector3f(model.getPackageMetaData(), model, Double.POSITIVE_INFINITY));
					geometryInfo.setMaxBounds(createVector3f(model.getPackageMetaData(), model, -Double.POSITIVE_INFINITY));

					try {
						double area = renderEngineInstance.getArea();
						geometryInfo.setArea(area);
						double volume = renderEngineInstance.getVolume();
						if (volume < 0d) {
							volume = -volume;
						}
						geometryInfo.setVolume(volume);
						
//						EStructuralFeature guidFeature = ifcProduct.eClass().getEStructuralFeature("GlobalId");
//						String guid = (String) ifcProduct.eGet(guidFeature);
//						System.out.println(guid + ": " + "Area: " + area + ", Volume: " + volume);
					} catch (UnsupportedOperationException e) {
					}
					
					GeometryData geometryData = null;
					geometryData = GeometryFactory.eINSTANCE.createGeometryData();

					geometryData.setIndices(intArrayToByteArray(geometry.getIndices()));
					geometryData.setVertices(floatArrayToByteArray(geometry.getVertices()));
					geometryData.setMaterialIndices(intArrayToByteArray(geometry.getMaterialIndices()));
					geometryData.setNormals(floatArrayToByteArray(geometry.getNormals()));
					
					geometryInfo.setPrimitiveCount(geometry.getIndices().length / 3);

					if (geometry.getMaterialIndices() != null && geometry.getMaterialIndices().length > 0) {
						boolean hasMaterial = false;
						float[] vertex_colors = new float[geometry.getVertices().length / 3 * 4];
						for (int i = 0; i < geometry.getMaterialIndices().length; ++i) {
							int c = geometry.getMaterialIndices()[i];
							for (int j = 0; j < 3; ++j) {
								int k = geometry.getIndices()[i * 3 + j];
								if (c > -1) {
									hasMaterial = true;
									for (int l = 0; l < 4; ++l) {
										vertex_colors[4 * k + l] = geometry.getMaterials()[4 * c + l];
									}
								}
							}
						}
						if (hasMaterial) {
							geometryData.setMaterials(floatArrayToByteArray(vertex_colors));
						}
					}

					double[] tranformationMatrix = new double[16];
					Matrix.setIdentityM(tranformationMatrix, 0);
					if (translate && renderEngineInstance.getTransformationMatrix() != null) {
						tranformationMatrix = renderEngineInstance.getTransformationMatrix();
					}

					for (int i = 0; i < geometry.getIndices().length; i++) {
						processExtends(geometryInfo, tranformationMatrix, geometry.getVertices(), geometry.getIndices()[i] * 3, generateGeometryResult);
					}

					geometryInfo.setData(geometryData);

//					long length = (geometryData.getIndices() != null ? geometryData.getIndices().length : 0) + 
//								  (geometryData.getVertices() != null ? geometryData.getVertices().length : 0) + 
//								  (geometryData.getNormals() != null ? geometryData.getNormals().length : 0) + 
//								  (geometryData.getMaterials() != null ? geometryData.getMaterials().length : 0) +
//								  (geometryData.getMaterialIndices() != null ? geometryData.getMaterialIndices().length : 0);

					setTransformationMatrix(geometryInfo, tranformationMatrix);
					int hash = hash(geometryData);
					if (hashes.containsKey(hash)) {
						geometryInfo.setData(hashes.get(hash));
					} else {
						hashes.put(hash, geometryData);
					}

					ifcProduct.setGeometry(geometryInfo);
				}
			} catch (EntityNotFoundException e) {
				// As soon as we find a representation that is not Curve2D, then we should show a "INFO" message in the log to indicate there could be something wrong
				boolean ignoreNotFound = true;
//				for (Object rep : representations) {
//					if (rep instanceof IfcShapeRepresentation) {
//						IfcShapeRepresentation ifcShapeRepresentation = (IfcShapeRepresentation)rep;
//						if (!"Curve2D".equals(ifcShapeRepresentation.getRepresentationType())) {
//							ignoreNotFound = false;
//						}
//					}
//				}
				if (!ignoreNotFound) {
					LOGGER.info("Entity not found " + ifcProduct.eClass().getName() + " " + ifcProduct.getExpressId() + "/" + ifcProduct.getOid());
				}
			} catch (RenderEngineException e) {
				LOGGER.error("", e);
			} catch (IfcModelInterfaceException e) {
				LOGGER.error("", e);
			}
		}
		return generateGeometryResult;
	}
	
	private byte[] floatArrayToByteArray(float[] vertices) {
		if (vertices == null) {
			return null;
		}
		ByteBuffer buffer = ByteBuffer.wrap(new byte[vertices.length * 4]);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		FloatBuffer asFloatBuffer = buffer.asFloatBuffer();
		for (float f : vertices) {
			asFloatBuffer.put(f);
		}
		return buffer.array();
	}

	private byte[] intArrayToByteArray(int[] indices) {
		if (indices == null) {
			return null;
		}
		ByteBuffer buffer = ByteBuffer.wrap(new byte[indices.length * 4]);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		IntBuffer asIntBuffer = buffer.asIntBuffer();
		for (int i : indices) {
			asIntBuffer.put(i);
		}
		return buffer.array();
	}
	
	private void setTransformationMatrix(GeometryInfo geometryInfo, double[] transformationMatrix) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(16 * 8);
		byteBuffer.order(ByteOrder.nativeOrder());
		DoubleBuffer asDoubleBuffer = byteBuffer.asDoubleBuffer();
		for (double f : transformationMatrix) {
			asDoubleBuffer.put(f);
		}
		geometryInfo.setTransformation(byteBuffer.array());
	}
	
	private Vector3f createVector3f(PackageMetaData packageMetaData, IfcModelInterface model, double defaultValue) throws IfcModelInterfaceException {
		Vector3f vector3f = null;
		vector3f = GeometryFactory.eINSTANCE.createVector3f();
		vector3f.setX(defaultValue);
		vector3f.setY(defaultValue);
		vector3f.setZ(defaultValue);
		return vector3f;
	}
	
	private int hash(GeometryData geometryData) {
		int hashCode = 0;
		if (geometryData.getIndices() != null) {
			hashCode += Arrays.hashCode(geometryData.getIndices());
		}
		if (geometryData.getVertices() != null) {
			hashCode += Arrays.hashCode(geometryData.getVertices());
		}
		if (geometryData.getNormals() != null) {
			hashCode += Arrays.hashCode(geometryData.getNormals());
		}
		if (geometryData.getMaterialIndices() != null) {
			hashCode += Arrays.hashCode(geometryData.getMaterialIndices());
		}
		if (geometryData.getMaterials() != null) {
			hashCode += Arrays.hashCode(geometryData.getMaterials());
		}
		return hashCode;
	}
	
	private void processExtends(GeometryInfo geometryInfo, double[] transformationMatrix, float[] vertices, int index, GenerateGeometryResult generateGeometryResult) {
		double x = vertices[index];
		double y = vertices[index + 1];
		double z = vertices[index + 2];
		double[] result = new double[4];
		Matrix.multiplyMV(result, 0, transformationMatrix, 0, new double[] { x, y, z, 1 }, 0);
		x = result[0];
		y = result[1];
		z = result[2];
		geometryInfo.getMinBounds().setX(Math.min(x, geometryInfo.getMinBounds().getX()));
		geometryInfo.getMinBounds().setY(Math.min(y, geometryInfo.getMinBounds().getY()));
		geometryInfo.getMinBounds().setZ(Math.min(z, geometryInfo.getMinBounds().getZ()));
		geometryInfo.getMaxBounds().setX(Math.max(x, geometryInfo.getMaxBounds().getX()));
		geometryInfo.getMaxBounds().setY(Math.max(y, geometryInfo.getMaxBounds().getY()));
		geometryInfo.getMaxBounds().setZ(Math.max(z, geometryInfo.getMaxBounds().getZ()));

		generateGeometryResult.getMinBoundsAsVector3f().setX(Math.min(x, generateGeometryResult.getMinBoundsAsVector3f().getX()));
		generateGeometryResult.getMinBoundsAsVector3f().setY(Math.min(y, generateGeometryResult.getMinBoundsAsVector3f().getY()));
		generateGeometryResult.getMinBoundsAsVector3f().setZ(Math.min(z, generateGeometryResult.getMinBoundsAsVector3f().getZ()));
		generateGeometryResult.getMaxBoundsAsVector3f().setX(Math.max(x, generateGeometryResult.getMaxBoundsAsVector3f().getX()));
		generateGeometryResult.getMaxBoundsAsVector3f().setY(Math.max(y, generateGeometryResult.getMaxBoundsAsVector3f().getY()));
		generateGeometryResult.getMaxBoundsAsVector3f().setZ(Math.max(z, generateGeometryResult.getMaxBoundsAsVector3f().getZ()));
	}
	
//	public static void main(String[] args) {
//		Schema schema = Schema.IFC2X3TC1;
//		
//		CommonContext context = new CommonContext();
//		
//		IfcStepDeserializer deserializer = new IfcStepDeserializer(schema) {
//		};
//		PackageMetaData packageMetaData = context.getMetaDataManager().getPackageMetaData(schema.getEPackageName());
//		
//		deserializer.init(packageMetaData);
//		
//		try {
//			deserializer.read(new File("resources/file/huahuatest.ifc"));
//		} catch (DeserializeException e) {
//			e.printStackTrace();
//		}
//		
//		IfcModelInterface model = deserializer.getModel();
//		
//		IfcStepSerializer serializer = BimFactory.getInstance().createIfcStepSerializer(schema);
//		ProgressReporter progressReporter = new ProgressReporter() {
//			
//			@Override
//			public void update(long progress, long max) {
//				
//			}
//			
//			@Override
//			public void setTitle(String stage) {
//			}
//		};
//		
//		JvmRenderEngineFactory factory = JvmRenderEngineFactory.getInstance();
//		factory.init(context);
//		IRenderEngine renderEngine;
//		try {
//			renderEngine = factory.createRenderEngine(schema.getEPackageName());
//			GeometryGenerator generator = new GeometryGenerator(model, serializer, renderEngine);
//			generator.generateForAllElements();
//			
//		} catch (RenderEngineException e) {
//			e.printStackTrace();
//		}
//		
//		BasicIfcModel basic = (BasicIfcModel) model;
//		Gson gson = new Gson();
//		System.out.println(gson.toJson(basic));
//	}
}