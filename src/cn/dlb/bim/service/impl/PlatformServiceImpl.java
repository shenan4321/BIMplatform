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
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Component;

import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.component.PlatformServerConfig;
import cn.dlb.bim.dao.IfcModelDao;
import cn.dlb.bim.dao.PlatformInitDatasDao;
import cn.dlb.bim.dao.entity.IfcClassLookupEntity;
import cn.dlb.bim.dao.entity.PlatformInitDatasEntity;
import cn.dlb.bim.ifc.database.BatchThreadLocal;
import cn.dlb.bim.ifc.database.DatabaseException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.model.IfcHeader;
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
	
	private final EClass[] cidToEclass;
	private final Map<EClass, Short> eClassToCid;
	private final Map<EClass, AtomicLong> oidCounters;
	private final Map<EClass, Boolean> oidChanged;
	
	private final BatchThreadLocal<ArrayList<VirtualObject>> localBatch;
	
	@Autowired
	@Qualifier("PlatformInitDatasDaoImpl")
	private PlatformInitDatasDao platformInitDatasDao;
	
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
		this.oidChanged = new HashMap<>();
		this.localBatch = new BatchThreadLocal<>(ArrayList.class);
	}
	
	private Long getInitCounter(EClass eClass) {
		ByteBuffer cidBuffer = ByteBuffer.wrap(new byte[8]);
		cidBuffer.putShort(6, getCidOfEClass(eClass));
		long startOid = cidBuffer.getLong(0);
		return startOid;
	}
	
	private void initialize() {
		initPlatformInitDatas();
		initIfcClassLookupTable();
	}
	
	private void initPlatformInitDatas() {
		PlatformInitDatasEntity platformInitDatasEntity = platformInitDatasDao.queryPlatformInitDatasEntityByPlatformVersionId(PlatformServerConfig.PLATFORM_VERSION);
		if (platformInitDatasEntity == null) {
			createPlatformInitDatas();
		} 
	}
	
	private void initIfcClassLookupTable() {
		List<IfcClassLookupEntity> allLookup = platformInitDatasDao.queryAllIfcClassLookup();
		if (allLookup.size() > 0) {
			for (IfcClassLookupEntity ifcClassLookup : allLookup) {
				String packageClassName = ifcClassLookup.getPackageClassName();
				String packageName = packageClassName.substring(0, packageClassName.indexOf("_"));
				String className = packageClassName.substring(packageClassName.indexOf("_") + 1);
				EClass eClass = (EClass) getEClassifier(packageName, className);
				cidToEclass[ifcClassLookup.getCid()] = eClass;
				eClassToCid.put(eClass, ifcClassLookup.getCid());
				Long oid = ifcClassLookup.getOid();
				oidCounters.put(eClass, new AtomicLong(oid));
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
		oidChanged.put(eClass, true);
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
    			IfcClassLookupEntity ifcClassLookup = new IfcClassLookupEntity();
    	    	ifcClassLookup.setCid(cidCounter);
    	    	ifcClassLookup.setPackageClassName(packageMetaData.getEPackage().getName() + "_" + eclass.getName());
    	    	eClassToCid.put(eclass, cidCounter);
    	    	cidToEclass[cidCounter] = eclass;
    	    	ifcClassLookup.setOid(getInitCounter(eclass));
    	    	platformInitDatasDao.insertIfcClassLookup(ifcClassLookup);
    	    	cidCounter++;
    		}
    	}
	}
	
	private void createPlatformInitDatas() {
		PlatformInitDatasEntity platformInitDatasEntity = new PlatformInitDatasEntity();
		platformInitDatasEntity.setPlatformVersionId(PlatformServerConfig.PLATFORM_VERSION);
		platformInitDatasEntity.setRevisionId(1);
		platformInitDatasDao.insertPlatformInitDatasEntity(platformInitDatasEntity);
	}

	public Integer newRevisionId() {
		PlatformInitDatasEntity platformInitDatasEntity = platformInitDatasDao.findAndIncreateRevisionId(PlatformServerConfig.PLATFORM_VERSION, 1);
		return platformInitDatasEntity.getRevisionId();
	}

	public void syncOid() {
		for (EClass eClass : oidChanged.keySet()) {
			IfcClassLookupEntity ifcClassLookup = new IfcClassLookupEntity();
			Short cid = eClassToCid.get(eClass);
			AtomicLong oid = oidCounters.get(eClass);
			ifcClassLookup.setCid(cid);
			ifcClassLookup.setOid(oid.get());
			platformInitDatasDao.updateOidInIfcClassLookup(ifcClassLookup);
		}
		oidChanged.clear();
	}

	@Override
	public void save(VirtualObject virtualObject) throws DatabaseException {
		ifcModelDao.insertVirtualObject(virtualObject);
	}

	@Override
	public void saveOverwrite(VirtualObject virtualObject) throws DatabaseException {
		ifcModelDao.insertVirtualObject(virtualObject);
	}
	
	@Override
	public void saveBatch(VirtualObject virtualObject) {
		ArrayList<VirtualObject> localBatchList = localBatch.newGet();
		localBatchList.add(virtualObject);
		if (localBatchList.size() >= AUTO_COMMIT_SIZE) {
			autoCommitSaveBatch();
		}
	}
	
	private void autoCommitSaveBatch() {
		ArrayList<VirtualObject> localBatchList = localBatch.newGet();
		ifcModelDao.insertAllVirtualObject(localBatchList);
		localBatch.get().clear();
	}
	
	@Override
	public void commitSaveBatch() {
		ArrayList<VirtualObject> localBatchList = localBatch.newGet();
		ifcModelDao.insertAllVirtualObject(localBatchList);
		localBatch.get().clear();
	}

	@Override
	public List<VirtualObject> queryVirtualObject(Integer rid, List<Short> cids) {
		return ifcModelDao.queryVirtualObject(rid, cids);
	}
	
	@Override
	public CloseableIterator<VirtualObject> streamVirtualObjectByRid(Integer rid) {
		return ifcModelDao.streamVirtualObjectByRid(rid);
	}

	@Override
	public void saveIfcHeader(IfcHeader ifcHeader) {
		ifcModelDao.saveIfcHeader(ifcHeader);
	}

	@Override
	public IfcHeader queryIfcHeader(Integer rid) {
		return ifcModelDao.queryIfcHeader(rid);
	}
}
