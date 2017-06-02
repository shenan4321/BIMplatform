package cn.dlb.bim.ifc.engine.pool;

import cn.dlb.bim.ifc.engine.IRenderEngine;
import cn.dlb.bim.ifc.engine.RenderEngineException;

public interface RenderEnginePool  {

	IRenderEngine borrowObject() throws RenderEngineException;

	void returnObject(IRenderEngine renderEngine) throws RenderEngineException;
}