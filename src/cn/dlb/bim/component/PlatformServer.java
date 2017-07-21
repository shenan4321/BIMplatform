package cn.dlb.bim.component;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.dlb.bim.cache.ModelCacheManager;
import cn.dlb.bim.cache.NewDiskCacheManager;
import cn.dlb.bim.dao.IfcModelDao;
import cn.dlb.bim.ifc.SerializationManager;
import cn.dlb.bim.ifc.collada.ColladaCacheManager;
import cn.dlb.bim.ifc.collada.ColladaProcessFactory;
import cn.dlb.bim.ifc.emf.MetaDataManager;
import cn.dlb.bim.ifc.engine.jvm.JvmRenderEngineFactory;
import cn.dlb.bim.ifc.engine.pool.CommonsPoolingRenderEnginePoolFactory;
import cn.dlb.bim.ifc.engine.pool.RenderEnginePoolFactory;
import cn.dlb.bim.ifc.engine.pool.RenderEnginePools;
import cn.dlb.bim.service.PlatformService;
import cn.dlb.bim.service.impl.PlatformServiceImpl;

/**
 * @author shenan4321
 *
 */
@Component("PlatformServer")
public class PlatformServer implements InitializingBean {

	private MetaDataManager metaDataManager;
	private SerializationManager serializationManager;
	private ColladaCacheManager colladaCacheManager;
	private ColladaProcessFactory colladaProcessFactory;
	private ModelCacheManager modelCacheManager;
	private NewDiskCacheManager diskCacheManager;
	private RenderEnginePools renderEnginePools;
	
	@Autowired
	private MongoGridFs mongoGridFs;
	@Autowired
	private LongActionManager longActionManager;
	@Autowired
	private PlatformServerConfig platformServerConfig;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		metaDataManager = new MetaDataManager(platformServerConfig.getTempDir());
		serializationManager = new SerializationManager(this);
		colladaCacheManager = new ColladaCacheManager(this);
		colladaProcessFactory = new ColladaProcessFactory(this);
		modelCacheManager = new ModelCacheManager();
		diskCacheManager = new NewDiskCacheManager(platformServerConfig.getDiskCachePath());
		renderEnginePools = new RenderEnginePools(this, new CommonsPoolingRenderEnginePoolFactory(10), new JvmRenderEngineFactory(this));//先写10
		
		initialize();
	}
	
	public void initialize() {
		metaDataManager.initialize();
		colladaProcessFactory.initialize();
	}
	
	public MetaDataManager getMetaDataManager() {
		return metaDataManager;
	}
	
	public SerializationManager getSerializationManager() {
		return serializationManager;
	}

	public MongoGridFs getMongoGridFs() {
		return mongoGridFs;
	}

	public LongActionManager getLongActionManager() {
		return longActionManager;
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

	public RenderEnginePools getRenderEnginePools() {
		return renderEnginePools;
	}

	public PlatformServerConfig getPlatformServerConfig() {
		return platformServerConfig;
	}
	
}
