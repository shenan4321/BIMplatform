package cn.dlb.bim.ifc.engine.pool;

import cn.dlb.bim.ifc.engine.IRenderEngineFactory;
import cn.dlb.bim.ifc.engine.RenderEngineException;

public class CommonsPoolingRenderEnginePoolFactory implements RenderEnginePoolFactory {

	private int nrRenderEngineProcesses;

	public CommonsPoolingRenderEnginePoolFactory(int nrRenderEngineProcesses) {
		this.nrRenderEngineProcesses = nrRenderEngineProcesses;
	}
	
	@Override
	public RenderEnginePool newRenderEnginePool(IRenderEngineFactory renderEngineFactory, String schema) throws RenderEngineException {
		return new CommonsRenderEnginePool(nrRenderEngineProcesses, renderEngineFactory, schema);
	}
}
