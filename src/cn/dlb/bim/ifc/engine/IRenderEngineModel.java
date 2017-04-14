package cn.dlb.bim.ifc.engine;

import java.util.Collection;

public interface IRenderEngineModel extends AutoCloseable {
	public static final int PRECISION = Precision.BIT;
	public static final int INDEX_BITS = IndexFormat.BIT;
	public static final int NORMALS = 32;
	public static final int TRANSFORM_GEOMETRY = 128;
	public static final int TRIANGLES = 256;
	public static final int WIREFRAME = 4096;

	void setFormat(int format, int mask) throws RenderEngineException;
	void setSettings(RenderEngineSettings settings) throws RenderEngineException;
	IRenderEngineInstance getInstanceFromExpressId(int oid) throws RenderEngineException;
	Collection<IRenderEngineInstance> listInstances() throws RenderEngineException;
	void generateGeneralGeometry() throws RenderEngineException;
	void close() throws RenderEngineException;
	void setFilter(RenderEngineFilter renderEngineFilter) throws RenderEngineException;
}