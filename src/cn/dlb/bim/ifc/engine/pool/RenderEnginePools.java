package cn.dlb.bim.ifc.engine.pool;

import java.util.HashMap;
import java.util.Map;
import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.ifc.emf.Schema;
import cn.dlb.bim.ifc.engine.IRenderEngineFactory;
import cn.dlb.bim.ifc.engine.RenderEngineException;

public class RenderEnginePools {

	private final Map<Schema, RenderEnginePool> pools = new HashMap<>();
	private final RenderEnginePoolFactory renderEnginePoolFactory;
	private final IRenderEngineFactory renderEngineFactory;

	public RenderEnginePools(PlatformServer server, RenderEnginePoolFactory renderEnginePoolFactory, IRenderEngineFactory renderEngineFactory) {
		this.renderEnginePoolFactory = renderEnginePoolFactory;
		this.renderEngineFactory = renderEngineFactory;
	}

	public RenderEnginePool getRenderEnginePool(Schema schema) throws RenderEngineException {
		if (pools.containsKey(schema)) {
			return pools.get(schema);
		} else {
			RenderEnginePool renderEnginePool = renderEnginePoolFactory.newRenderEnginePool(renderEngineFactory, schema.name());
			pools.put(schema, renderEnginePool);
			return renderEnginePool;
		}
	}
}