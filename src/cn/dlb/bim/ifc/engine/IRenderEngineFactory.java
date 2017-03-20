package cn.dlb.bim.ifc.engine;

public interface IRenderEngineFactory {
	public IRenderEngine createRenderEngine(String schema) throws RenderEngineException;
	public void initialize();
}
