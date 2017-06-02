package cn.dlb.bim.ifc.engine.pool;

import cn.dlb.bim.ifc.engine.IRenderEngineFactory;
import cn.dlb.bim.ifc.engine.RenderEngineException;

public interface RenderEnginePoolFactory {

	RenderEnginePool newRenderEnginePool(IRenderEngineFactory renderEngineFactory, String schema) throws RenderEngineException;

}
