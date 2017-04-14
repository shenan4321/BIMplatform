package cn.dlb.bim.ifc.engine;

public class RenderEngineGeometry {
	private final int[] indices;
	private final float[] vertices;
	private final float[] normals;
	private float[] materials;
	private int[] materialIndices;

	public RenderEngineGeometry(int[] indices, float[] vertices, float[] normals, float[] materials, int[] materialIndices) {
		this.indices = indices;
		this.vertices = vertices;
		this.normals = normals;
		this.materials = materials;
		this.materialIndices = materialIndices;
	}
	
	public int getIndex(int index) {
		return indices[index];
	}
	
	public float getVertex(int index) {
		return vertices[index];
	}
	
	public float getNormal(int index) {
		return normals[index];
	}

	public float[] getMaterials() {
		return materials;
	}
	
	public int[] getMaterialIndices() {
		return materialIndices;
	}
	
	public int getNrVertices() {
		return vertices.length;
	}

	public int getNrNormals() {
		return normals.length;
	}

	public int getNrIndices() {
		return indices.length;
	}

	public float[] getVertices() {
		return vertices;
	}
	
	public float[] getNormals() {
		return normals;
	}
	
	public int[] getIndices() {
		return indices;
	}

	public int getMaterialIndex(int index) {
		return materialIndices[index];
	}

	public float getMaterial(int i) {
		return materials[i];
	}
}