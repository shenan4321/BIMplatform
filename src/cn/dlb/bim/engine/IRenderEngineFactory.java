package cn.dlb.bim.engine;

import cn.dlb.bim.common.CommonContext;

public interface IRenderEngineFactory {
	public IRenderEngine createRenderEngine(String schema) throws RenderEngineException;
	public void init(CommonContext commonContext);
}
