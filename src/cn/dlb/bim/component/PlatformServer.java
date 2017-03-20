package cn.dlb.bim.component;

import org.springframework.stereotype.Component;

import cn.dlb.bim.PlatformContext;
import cn.dlb.bim.ifc.SerializationManager;
import cn.dlb.bim.ifc.emf.MetaDataManager;
import cn.dlb.bim.ifc.engine.IRenderEngineFactory;
import cn.dlb.bim.ifc.engine.jvm.JvmRenderEngineFactory;

/**
 * @author shenan4321
 *
 */
@Component("PlatformServer")
public class PlatformServer {

	private MetaDataManager metaDataManager;
	private SerializationManager serializationManager;
	private IRenderEngineFactory renderEngineFactory;
	
	public PlatformServer() {
		metaDataManager = new MetaDataManager(PlatformContext.getTempPath());
		serializationManager = new SerializationManager(this);
		renderEngineFactory = new JvmRenderEngineFactory(this);
		
		initialize();
	}
	
	public void initialize() {
		metaDataManager.initialize();
		renderEngineFactory.initialize();
	}
	
	public MetaDataManager getMetaDataManager() {
		return metaDataManager;
	}
	
	public SerializationManager getSerializationManager() {
		return serializationManager;
	}

	public IRenderEngineFactory getRenderEngineFactory() {
		return renderEngineFactory;
	}

}
