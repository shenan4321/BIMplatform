package cn.dlb.bim.engine.impl;

import cn.dlb.bim.engine.RenderEngineException;
import cn.dlb.bim.engine.RenderEngineGeometry;
import cn.dlb.bim.engine.IRenderEngineInstance;
import cn.dlb.bim.engine.RenderEngineSurfaceProperties;

public class JvmIfcEngineInstance implements IRenderEngineInstance {
	private final JvmIfcEngine failSafeIfcEngine;
	private final int instanceId;
	private final int modelId;

	public JvmIfcEngineInstance(JvmIfcEngine failSafeIfcEngine, int modelId, int instanceId) {
		this.failSafeIfcEngine = failSafeIfcEngine;
		this.modelId = modelId;
		this.instanceId = instanceId;
	}

	@Override
	public double[] getTransformationMatrix() throws RenderEngineException {
		synchronized (failSafeIfcEngine) {
			failSafeIfcEngine.writeCommand(Command.GET_TRANSFORMATION_MATRIX);
			failSafeIfcEngine.writeInt(modelId);
			failSafeIfcEngine.writeInt(instanceId);
			failSafeIfcEngine.flush();
			double[] result = new double[16];
			for (int i=0; i<16; i++) {
				result[i] = failSafeIfcEngine.readDouble();
			}
			return result;
		}
	}

	@Override
	public double getArea() throws RenderEngineException {
		synchronized (failSafeIfcEngine) {
			failSafeIfcEngine.writeCommand(Command.GET_AREA);
			failSafeIfcEngine.writeInt(modelId);
			failSafeIfcEngine.writeInt(instanceId);
			failSafeIfcEngine.flush();
			return failSafeIfcEngine.readDouble();
		}
	}

	@Override
	public double getVolume() throws RenderEngineException {
		synchronized (failSafeIfcEngine) {
			failSafeIfcEngine.writeCommand(Command.GET_VOLUME);
			failSafeIfcEngine.writeInt(modelId);
			failSafeIfcEngine.writeInt(instanceId);
			failSafeIfcEngine.flush();
			return failSafeIfcEngine.readDouble();
		}
	}
	
	private RenderEngineSurfaceProperties initialize() throws RenderEngineException {
		synchronized (failSafeIfcEngine) {
			failSafeIfcEngine.writeCommand(Command.INITIALIZE_MODELLING_INSTANCE);
			failSafeIfcEngine.writeInt(modelId);
			failSafeIfcEngine.writeInt(instanceId);
			failSafeIfcEngine.flush();
			int noIndices = failSafeIfcEngine.readInt();
			int noVertices = failSafeIfcEngine.readInt();
			return new RenderEngineSurfaceProperties(modelId, noVertices, noIndices, 0.0);
		}		
	}
	
	@Override
	public RenderEngineGeometry generateGeometry() throws RenderEngineException {
		RenderEngineSurfaceProperties initialize = initialize();
		return finalize(initialize);
	}

	private RenderEngineGeometry finalize(RenderEngineSurfaceProperties initialize) throws RenderEngineException {
		synchronized (failSafeIfcEngine) {
			failSafeIfcEngine.writeCommand(Command.FINALIZE_MODELLING);
			failSafeIfcEngine.writeInt(modelId);
			failSafeIfcEngine.writeInt(initialize.getIndicesCount());
			failSafeIfcEngine.writeInt(initialize.getVerticesCount());
			failSafeIfcEngine.flush();
			
			int[] indices = new int[initialize.getIndicesCount()];
			float[] vertices = new float[initialize.getVerticesCount() * 3];
			float[] normals = new float[initialize.getVerticesCount() * 3];
			for (int i = 0; i < indices.length; i++) {
				indices[i] = failSafeIfcEngine.readInt();
			}
			for (int i = 0; i < vertices.length; i++) {
				vertices[i] = failSafeIfcEngine.readFloat();
			}
			for (int i = 0; i < normals.length; i++) {
				normals[i] = failSafeIfcEngine.readFloat();
			}
			return new RenderEngineGeometry(indices, vertices, normals, null, null);
		}
	}
}