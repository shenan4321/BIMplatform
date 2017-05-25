package cn.dlb.bim.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.dlb.bim.PlatformContext;
import cn.dlb.bim.cache.ModelCacheManager;
import cn.dlb.bim.cache.NewDiskCacheManager;
import cn.dlb.bim.dao.IfcModelDao;
import cn.dlb.bim.ifc.SerializationManager;
import cn.dlb.bim.ifc.collada.ColladaCacheManager;
import cn.dlb.bim.ifc.collada.ColladaProcessFactory;
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
	private final ColladaCacheManager colladaCacheManager;
	private final ColladaProcessFactory colladaProcessFactory;
	private final ModelCacheManager modelCacheManager;
	private final NewDiskCacheManager diskCacheManager;
	
	@Autowired
	private MongoGridFs mongoGridFs;
	@Autowired
	private PlatformInitDatas platformInitDatas;
	@Autowired
	private LongActionManager longActionManager;
	@Autowired
	private IfcModelDao ifcModelDao;
	
	public PlatformServer() {
		metaDataManager = new MetaDataManager(PlatformContext.getTempPath());
		serializationManager = new SerializationManager(this);
		renderEngineFactory = new JvmRenderEngineFactory(this);
		colladaCacheManager = new ColladaCacheManager(this);
		colladaProcessFactory = new ColladaProcessFactory();
		modelCacheManager = new ModelCacheManager();
		diskCacheManager = new NewDiskCacheManager(PlatformContext.getDiskCachepath());
		
		initialize();
	}
	
	public void initialize() {
		metaDataManager.initialize();
		renderEngineFactory.initialize();
		colladaProcessFactory.initialize();
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

	public IfcModelDao getIfcModelDao() {
		return ifcModelDao;
	}

	public ColladaCacheManager getColladaCacheManager() {
		return colladaCacheManager;
	}

	public ColladaProcessFactory getColladaProcessFactory() {
		return colladaProcessFactory;
	}

	public ModelCacheManager getModelCacheManager() {
		return modelCacheManager;
	}

	public NewDiskCacheManager getDiskCacheManager() {
		return diskCacheManager;
	}
	
}
