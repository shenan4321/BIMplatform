package cn.dlb.bim.ifc.engine.pool;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

import cn.dlb.bim.ifc.engine.IRenderEngine;
import cn.dlb.bim.ifc.engine.IRenderEngineFactory;
import cn.dlb.bim.ifc.engine.RenderEngineException;

public class CommonsRenderEnginePool implements RenderEnginePool {
	private GenericObjectPool<IRenderEngine> genericObjectPool;
	
	public CommonsRenderEnginePool(int poolSize, IRenderEngineFactory renderEngineFactory, String schema) throws RenderEngineException {
		PooledObjectFactory<IRenderEngine> pooledObjectFactory = new PooledObjectFactory<IRenderEngine>() {
			@Override
			public void activateObject(PooledObject<IRenderEngine> arg0) throws Exception {
				arg0.getObject().init();
			}

			@Override
			public void destroyObject(PooledObject<IRenderEngine> arg0) throws Exception {
				arg0.getObject().close();
			}

			@Override
			public PooledObject<IRenderEngine> makeObject() throws Exception {
				return new DefaultPooledObject<IRenderEngine>(renderEngineFactory.createRenderEngine(schema));
			}

			@Override
			public void passivateObject(PooledObject<IRenderEngine> arg0) throws Exception {
			}

			@Override
			public boolean validateObject(PooledObject<IRenderEngine> arg0) {
				return false;
			}
		};
		
		genericObjectPool = new GenericObjectPool<IRenderEngine>(pooledObjectFactory);
		
		genericObjectPool.setMaxWaitMillis(1000 * 60 * 1);
		genericObjectPool.setMaxTotal(8);
	}

	@Override
	public IRenderEngine borrowObject() throws RenderEngineException {
		try {
			return genericObjectPool.borrowObject();
		} catch (Exception e) {
			throw new RenderEngineException(e);
		}
	}

	@Override
	public void returnObject(IRenderEngine renderEngine) throws RenderEngineException {
		genericObjectPool.returnObject(renderEngine);
	}
}