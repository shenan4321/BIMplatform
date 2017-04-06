package cn.dlb.bim.component;

import org.springframework.beans.factory.annotation.Autowired;
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

	private final MetaDataManager metaDataManager;
	private final SerializationManager serializationManager;
	private final IRenderEngineFactory renderEngineFactory;
	
	@Autowired
	private MongoGridFs mongoGridFs;
	@Autowired
	private PlatformInitDatas platformInitDatas;
	@Autowired
	private LongActionManager longActionManager;
	
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

	public MongoGridFs getMongoGridFs() {
		return mongoGridFs;
	}

	public PlatformInitDatas getPlatformInitDatas() {
		return platformInitDatas;
	}

	public LongActionManager getLongActionManager() {
		return longActionManager;
	}
	
}
