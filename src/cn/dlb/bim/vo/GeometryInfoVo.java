package cn.dlb.bim.vo;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import cn.dlb.bim.models.geometry.GeometryInfo;
import cn.dlb.bim.models.ifc2x3tc1.IfcProduct;
import cn.dlb.bim.models.ifc2x3tc1.IfcSpace;

public class GeometryInfoVo implements ITransformer<GeometryInfo> {

	private int[] indices;
	private float[] vertices;
	private float[] normals;
	private Bound bound;
	private String typeName;
	
	public GeometryInfoVo() {
		init();
	}
	
	public class Bound {
		public Vector3f max;
		public Vector3f min;
		public Bound() {
			max = new Vector3f(0, 0, 0);
			min = new Vector3f(0, 0, 0);
		}
	}
	
	public void init() {
		bound = new Bound();
	}
	
	@Override
	public void transform(GeometryInfo geometryInfo) {
		indices = byteArrayToIntArray(geometryInfo.getData().getIndices());
		vertices = byteArrayToFloatArray(geometryInfo.getData().getVertices());
		normals = byteArrayToFloatArray(geometryInfo.getData().getNormals());
		double maxX = geometryInfo.getMaxBounds().getX();
		double maxY = geometryInfo.getMaxBounds().getY();
		double maxZ = geometryInfo.getMaxBounds().getZ();
		double minX = geometryInfo.getMinBounds().getX();
		double minY = geometryInfo.getMinBounds().getY();
		double minZ = geometryInfo.getMinBounds().getZ();
		bound.max.set(maxX, maxY, maxZ);
		bound.min.set(minX, minY, minZ);
	}
	
	public boolean adapt(IfcProduct ifcProduct) {//IfcProduct
		GeometryInfo geometryInfo = ifcProduct.getGeometry();
		if (geometryInfo == null || ifcProduct instanceof IfcSpace) {
			return false;
		}
		typeName = ifcProduct.getClass().getSimpleName();
		if (typeName.endsWith("Impl")) {
			typeName = typeName.substring(0, typeName.indexOf("Impl"));
		}
		indices = byteArrayToIntArray(geometryInfo.getData().getIndices());
		vertices = byteArrayToFloatArray(geometryInfo.getData().getVertices());
		normals = byteArrayToFloatArray(geometryInfo.getData().getNormals());
		double maxX = geometryInfo.getMaxBounds().getX();
		double maxY = geometryInfo.getMaxBounds().getY();
		double maxZ = geometryInfo.getMaxBounds().getZ();
		double minX = geometryInfo.getMinBounds().getX();
		double minY = geometryInfo.getMinBounds().getY();
		double minZ = geometryInfo.getMinBounds().getZ();
		bound.max.set(maxX, maxY, maxZ);
		bound.min.set(minX, minY, minZ);
		return true;
	}
	
	private int[] byteArrayToIntArray(byte[] byteArray) {
		 IntBuffer intBuf =
				   ByteBuffer.wrap(byteArray)
				     .order(ByteOrder.LITTLE_ENDIAN)
				     .asIntBuffer();
				 int[] array = new int[intBuf.remaining()];
				 intBuf.get(array);
		return array;
	}
	
	private float[] byteArrayToFloatArray(byte[] byteArray) {
		 FloatBuffer floatBuf =
				   ByteBuffer.wrap(byteArray)
				     .order(ByteOrder.LITTLE_ENDIAN)
				     .asFloatBuffer();
				 float[] array = new float[floatBuf.remaining()];
				 floatBuf.get(array);
		return array;
	}

	public int[] getIndices() {
		return indices;
	}

	public void setIndices(int[] indices) {
		this.indices = indices;
	}

	public float[] getVertices() {
		return vertices;
	}

	public void setVertices(float[] vertices) {
		this.vertices = vertices;
	}

	public float[] getNormals() {
		return normals;
	}

	public void setNormals(float[] normals) {
		this.normals = normals;
	}

	public Bound getBound() {
		return bound;
	}

	public void setBound(Bound bound) {
		this.bound = bound;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
}
