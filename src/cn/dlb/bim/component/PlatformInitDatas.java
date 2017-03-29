package cn.dlb.bim.component;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import cn.dlb.bim.PlatformContext;
import cn.dlb.bim.dao.PlatformInitDatasDao;
import cn.dlb.bim.dao.entity.IfcClassLookupEntity;
import cn.dlb.bim.dao.entity.PlatformInitDatasEntity;
import cn.dlb.bim.ifc.database.IfcModelDbException;
import cn.dlb.bim.ifc.database.binary.IfcDataBase;
import cn.dlb.bim.ifc.emf.PackageMetaData;

/**
 * @author shenan4321
 *
 */
@Component("PlatformInitDatas")
public class PlatformInitDatas implements InitializingBean, IfcDataBase {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(PlatformInitDatas.class);
	
	private final EClass[] cidToEclass;
	private final Map<EClass, Short> eClassToCid;
	private final Map<EClass, AtomicLong> oidCounters;
	private AtomicInteger revisionIdCounter;
	
	private final Map<EClass, Boolean> oidChanged;//recording witch eclass oidcounter changed
	
	@Autowired
	@Qualifier("PlatformInitDatasDaoImpl")
	private PlatformInitDatasDao platformInitDatasDao;
	
	@Autowired
	@Qualifier("PlatformServer")
	private PlatformServer server;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		initialize();
	}
	
	public PlatformInitDatas() {
		this.oidCounters = new HashMap<EClass, AtomicLong>();
		this.cidToEclass = new EClass[Short.MAX_VALUE]; 
		this.eClassToCid = new HashMap<EClass, Short>();
		this.revisionIdCounter = new AtomicInteger(0);
		this.oidChanged = new HashMap<EClass, Boolean>();
	}
	
	private void initCounter(EClass eClass) {
		ByteBuffer cidBuffer = ByteBuffer.wrap(new byte[8]);
		cidBuffer.putShort(6, getCidOfEClass(eClass));
		long startOid = cidBuffer.getLong(0);
		oidCounters.put(eClass, new AtomicLong(startOid));
	}
	
	private void initialize() {
		initPlatformInitDatas();
		initIfcClassLookupTable();
	}
	
	private void initPlatformInitDatas() {
		PlatformInitDatasEntity platformInitDatasEntity = platformInitDatasDao.queryPlatformInitDatasEntityByPlatformVersionId(PlatformContext.getPlatformVersion());
		if (platformInitDatasEntity == null) {
			createPlatformInitDatas();
		} else {
			revisionIdCounter.set(platformInitDatasEntity.getRevisionId());
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
				long oid = ifcClassLookup.getOid();
				initCounter(eClass);
				if (oid > oidCounters.get(eClass).get()) {
					oidCounters.put(eClass, new AtomicLong(oid));
				}
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
	
	public EClass getEClassForOid(long oid) throws IfcModelDbException {
		short cid = (short)oid;
		EClass eClass = getEClassForCid(cid);
		if (eClass == null) {
			throw new IfcModelDbException("No class for cid " + cid + " (cid came from oid: " + oid  + ")");
		}
		return eClass;
	}

	@Override
	public long newOid(EClass eClass) {
		oidChanged.put(eClass, true);
		return oidCounters.get(eClass).addAndGet(65536);
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
    	    	initCounter(eclass);
    	    	ifcClassLookup.setOid(oidCounters.get(eclass).longValue());
    	    	platformInitDatasDao.insertIfcClassLookup(ifcClassLookup);
    	    	cidCounter++;
    		}
    	}
	}
	
	private void createPlatformInitDatas() {
		revisionIdCounter.set(0);
		PlatformInitDatasEntity platformInitDatasEntity = new PlatformInitDatasEntity();
		platformInitDatasEntity.setPlatformVersionId(PlatformContext.getPlatformVersion());
		platformInitDatasEntity.setRevisionId(revisionIdCounter.get());
		platformInitDatasDao.insertPlatformInitDatasEntity(platformInitDatasEntity);
	}

	public Integer newRevisionId() {
		return revisionIdCounter.incrementAndGet();
	}

	@Override
	public void updateDataBase() {
		PlatformInitDatasEntity platformInitDatasEntity = new PlatformInitDatasEntity();
		platformInitDatasEntity.setPlatformVersionId(PlatformContext.getPlatformVersion());
		platformInitDatasEntity.setRevisionId(revisionIdCounter.get());
		platformInitDatasDao.updatePlatformInitDatasEntity(platformInitDatasEntity);
		
		for (EClass eclass : oidChanged.keySet()) {
			IfcClassLookupEntity ifcClassLookup = new IfcClassLookupEntity();
			Short cid = eClassToCid.get(eclass);
			AtomicLong oid = oidCounters.get(eclass);
			ifcClassLookup.setCid(cid);
			ifcClassLookup.setOid(oid.get());
			platformInitDatasDao.updateOidInIfcClassLookup(ifcClassLookup);
		}
		oidChanged.clear();
	}

}
