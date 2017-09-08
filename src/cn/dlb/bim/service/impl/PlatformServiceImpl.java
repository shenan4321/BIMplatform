package cn.dlb.bim.service.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Component;

import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.component.PlatformServerConfig;
import cn.dlb.bim.dao.BaseMongoDao;
import cn.dlb.bim.dao.IfcModelDao;
import cn.dlb.bim.dao.entity.CatalogIfc;
import cn.dlb.bim.dao.entity.PlatformVersions;
import cn.dlb.bim.database.BatchThreadLocal;
import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.service.PlatformService;

/**
 * @author shenan4321
 *
 */
@Component("PlatformServiceImpl")
public class PlatformServiceImpl implements InitializingBean, PlatformService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PlatformServiceImpl.class);
	
	private static final int AUTO_COMMIT_SIZE = 1000;
	private static final String SAVE_BATCH_KEY = "save";
	private static final String UPDATE_BATCH_KEY = "update";
	
	private final EClass[] cidToEclass;
	private final Map<EClass, Short> eClassToCid;
	private final Map<EClass, AtomicLong> oidCounters;
	
	private final BatchThreadLocal<Map<String, List<VirtualObject>>> localBatch;
	
	@Autowired
	@Qualifier("PlatformVersionsDaoImpl")
	private BaseMongoDao<PlatformVersions> platformVersionsDao;
	
	@Autowired
	@Qualifier("CatalogIfcDaoImpl")
	private BaseMongoDao<CatalogIfc> catalogIfcDao;
	
	@Autowired
	@Qualifier("PlatformServer")
	private PlatformServer server;
	
	@Autowired
	@Qualifier("IfcModelDaoImpl")
	private IfcModelDao ifcModelDao;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		initialize();
	}
	
	public PlatformServiceImpl() throws NoSuchMethodException, SecurityException {
		this.cidToEclass = new EClass[Short.MAX_VALUE]; 
		this.eClassToCid = new HashMap<>();
		this.oidCounters = new HashMap<>();
		this.localBatch = new BatchThreadLocal<>(HashMap.class);
	}
	
	private Long getInitCounter(EClass eClass) {
		ByteBuffer cidBuffer = ByteBuffer.wrap(new byte[8]);
		cidBuffer.putShort(6, getCidOfEClass(eClass));
		long startOid = cidBuffer.getLong(0);
		return startOid;
	}
	
	private void initialize() {
		initPlatformVersions();
		initCatalogIfcTable();
	}
	
	private void initPlatformVersions() {
		PlatformVersions platformVersions = platformVersionsDao.findById(PlatformServerConfig.PLATFORM_VERSION);
		if (platformVersions == null) {
			createPlatformVersions();
		} 
	}
	
	private void initCatalogIfcTable() {
		List<CatalogIfc> catalogIfcList = catalogIfcDao.find(new Query());
		if (catalogIfcList.size() > 0) {
			for (CatalogIfc catalogIfc : catalogIfcList) {
				String packageClassName = catalogIfc.getPackageClassName();
				String packageName = packageClassName.substring(0, packageClassName.indexOf("_"));
				String className = packageClassName.substring(packageClassName.indexOf("_") + 1);
				EClass eClass = (EClass) getEClassifier(packageName, className);
				cidToEclass[catalogIfc.getCid()] = eClass;
				eClassToCid.put(eClass, catalogIfc.getCid());
				oidCounters.put(eClass, new AtomicLong(getInitCounter(eClass)));
			}
		} else {
			createClassLookupTable();
		}
	}
	
	public EClassifier getEClassifier(String packageName, String classifierName) {
		if (packageName == null) {
			return null;
		}
		if (packageName.contains(".")) {
			packageName = packageName.substring(packageName.lastIndexOf(".") + 1);
		}
		EPackage ePackage = server.getMetaDataManager().getPackageMetaData(packageName).getEPackage();
		if (ePackage == null) {
			return null;
		}
		if (ePackage.getEClassifier(classifierName) != null) {
			return ePackage.getEClassifier(classifierName);
		}
		throw null;
	}
	
	public Short getCidOfEClass(EClass eClass) {
		return eClassToCid.get(eClass);
	}
	
	public EClass getEClassForCid(short cid) {
		return cidToEclass[cid];
	}
	
	public EClass getEClassForOid(long oid) throws DatabaseException {
		short cid = (short)oid;
		EClass eClass = getEClassForCid(cid);
		if (eClass == null) {
			throw new DatabaseException("No class for cid " + cid + " (cid came from oid: " + oid  + ")");
		}
		return eClass;
	}

	@Override
	public long newOid(EClass eClass) {
		return oidCounters.get(eClass).addAndGet(65536);
//		Short cid = getCidOfEClass(eClass);
//		IfcClassLookupEntity ifcClassLookupEntity = platformInitDatasDao.findAndIncreateOid(cid, 65536);
//		return ifcClassLookupEntity.getOid();
	}
	
	public void createClassLookupTable() {
		LOGGER.info("create classLookupTable.");
		Short cidCounter = 1;
    	for (PackageMetaData packageMetaData : server.getMetaDataManager().getAll()) {
    		List<EClass> allClass = packageMetaData.getAllClasses();
    		for (EClass eclass : allClass) {
    			CatalogIfc catalogIfc = new CatalogIfc();
    			catalogIfc.setCid(cidCounter);
    			catalogIfc.setPackageClassName(packageMetaData.getEPackage().getName() + "_" + eclass.getName());
    	    	eClassToCid.put(eclass, cidCounter);
    	    	cidToEclass[cidCounter] = eclass;
    	    	oidCounters.put(eclass, new AtomicLong(getInitCounter(eclass)));
    	    	catalogIfcDao.save(catalogIfc);
    	    	cidCounter++;
    		}
    	}
	}
	
	private void createPlatformVersions() {
		PlatformVersions platformVersions = new PlatformVersions();
		platformVersions.setPlatformVersionId(PlatformServerConfig.PLATFORM_VERSION);
		platformVersions.setCurrentTopRevisionId(1);
		platformVersionsDao.save(platformVersions);
	}

	public Integer newRevisionId() {
		Query query = new Query();
		query.addCriteria(Criteria.where("platformVersionId").is(PlatformServerConfig.PLATFORM_VERSION));
		Update update = new Update();
		update.inc("currentTopRevisionId", 1);
		PlatformVersions platformVersions = platformVersionsDao.update(query, update);
		return platformVersions.getCurrentTopRevisionId();
	}

	@Override
	public void save(VirtualObject virtualObject) {
		ifcModelDao.insertVirtualObject(virtualObject);
	}

	@Override
	public void update(VirtualObject virtualObject) {
		ifcModelDao.updateVirtualObject(virtualObject);
	}
	
	@Override
	public void saveBatch(VirtualObject virtualObject) {
		Map<String, List<VirtualObject>> localThreadMap = localBatch.newGet();
		List<VirtualObject> localBatchList = localThreadMap.get(SAVE_BATCH_KEY);
		if (localBatchList == null) {
			localBatchList = new ArrayList<>();
			localThreadMap.put(SAVE_BATCH_KEY, localBatchList);
		}
		localBatchList.add(virtualObject);
		if (localBatchList.size() >= AUTO_COMMIT_SIZE) {
			ifcModelDao.insertAllVirtualObject(localBatchList);
			localBatch.get().get(SAVE_BATCH_KEY).clear();
		}
	}
	
	
//	@Override
//	public void commitSaveBatch() {
//		List<VirtualObject> localBatchList = localBatch.newGet().get(SAVE_BATCH_KEY);
//		if (localBatchList == null || localBatchList.isEmpty()) {
//			return;
//		}
//		ifcModelDao.insertAllVirtualObject(localBatchList);
//		localBatch.get().clear();
//	}

	@Override
	public List<VirtualObject> queryVirtualObject(Integer rid, List<Short> cids) {
		return ifcModelDao.queryVirtualObject(rid, cids);
	}
	
	@Override
	public CloseableIterator<VirtualObject> streamVirtualObjectByRid(Integer rid) {
		return ifcModelDao.streamVirtualObjectByRid(rid);
	}

	@Override
	public VirtualObject queryVirtualObject(Integer rid, Long oid) {
		return ifcModelDao.queryVirtualObject(rid, oid);
	}

	@Override
	public CloseableIterator<VirtualObject> streamVirtualObject(Integer rid, Short cid) {
		return ifcModelDao.streamVirtualObject(rid, cid);
	}

	@Override
	public void updateBatch(VirtualObject virtualObject) {
		Map<String, List<VirtualObject>> localThreadMap = localBatch.newGet();
		List<VirtualObject> localBatchList = localThreadMap.get(UPDATE_BATCH_KEY);
		if (localBatchList == null) {
			localBatchList = new ArrayList<>();
			localThreadMap.put(UPDATE_BATCH_KEY, localBatchList);
		}
		localBatchList.add(virtualObject);
		if (localBatchList.size() >= AUTO_COMMIT_SIZE) {
			ifcModelDao.updateAllVirtualObject(localBatchList);
			localBatch.get().get(UPDATE_BATCH_KEY).clear();
		}
	}

//	@Override
//	public void commitUpdateBatch() {
//		List<VirtualObject> localBatchList = localBatch.newGet().get(UPDATE_BATCH_KEY);
//		if (localBatchList == null) {
//			return;
//		}
//		ifcModelDao.updateAllVirtualObject(localBatchList);
//		localBatch.get().clear();
//	}

	@Override
	public void commitAllBatch() {
		List<VirtualObject> updateLocalBatchList = localBatch.newGet().get(UPDATE_BATCH_KEY);
		if (updateLocalBatchList != null && !updateLocalBatchList.isEmpty()) {
			ifcModelDao.updateAllVirtualObject(updateLocalBatchList);
		}
		List<VirtualObject> saveLocalBatchList = localBatch.newGet().get(SAVE_BATCH_KEY);
		if (saveLocalBatchList != null && !saveLocalBatchList.isEmpty()) {
			ifcModelDao.insertAllVirtualObject(saveLocalBatchList);
		}
		localBatch.get().clear();
	}
}
