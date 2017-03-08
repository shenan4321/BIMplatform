package cn.dlb.bim.adaptors;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import cn.dlb.bim.models.geometry.GeometryInfo;
import cn.dlb.bim.models.geometry.Vector3f;

public class GeometryInfoAdaptor implements IAdaptor<GeometryInfo> {

//	String base64Transformation;
//	String base64Indices;
//	String base64Vertices;
//	String base64Normals;
	
//	String transformation;
	private int[] indices;
	private float[] vertices;
	private float[] normals;
	private Bound bound;
	
	
//	@Override
//	public void adapt(GeometryInfo geometryInfo) {
//		byte[] transformation = geometryInfo.getTransformation();
//		byte[] indices = geometryInfo.getData().getIndices();
//		byte[] vertices = geometryInfo.getData().getVertices();
//		byte[] normals = geometryInfo.getData().getNormals();
//		base64Transformation = Base64.getEncoder().encodeToString(transformation);
//		base64Indices = Base64.getEncoder().encodeToString(indices);
//		base64Vertices = Base64.getEncoder().encodeToString(vertices);
//		base64Normals = Base64.getEncoder().encodeToString(normals);
//	}
	
	public GeometryInfoAdaptor() {
		init();
	}
	
	
	public class Bound {
		public Vector3f max;
		public Vector3f min;
	}
	
	public void init() {
		bound = new Bound();
	}
	
	@Override
	public void adapt(GeometryInfo geometryInfo) {
		indices = byteArrayToIntArray(geometryInfo.getData().getIndices());
		vertices = byteArrayToFloatArray(geometryInfo.getData().getVertices());
		normals = byteArrayToFloatArray(geometryInfo.getData().getNormals());
		bound.max = geometryInfo.getMaxBounds();
		bound.min = geometryInfo.getMinBounds();
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
	
}
