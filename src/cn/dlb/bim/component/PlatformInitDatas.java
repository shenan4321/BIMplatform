package cn.dlb.bim.component;

import java.nio.ByteBuffer;
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
import org.springframework.stereotype.Component;

import cn.dlb.bim.dao.IIfcClassLookupDao;
import cn.dlb.bim.dao.entity.IfcClassLookup;
import cn.dlb.bim.ifc.database.binary.IfcDataBase;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.engine.jvm.JvmRenderEngineFactory;

/**
 * @author shenan4321
 *
 */
@Component("PlatformInitDatas")
public class PlatformInitDatas implements InitializingBean, IfcDataBase {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JvmRenderEngineFactory.class);
	
	private final EClass[] cidToEclass;
	private final Map<EClass, Short> eClassToCid;
	private final Map<EClass, AtomicLong> oidCounters = new HashMap<EClass, AtomicLong>();
	
	@Autowired
	@Qualifier("IfcClassLookupDaoImpl")
	private IIfcClassLookupDao ifcClassLookupDao;
	
	@Autowired
	@Qualifier("PlatformServer")
	private PlatformServer server;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		initialize();
	}
	
	public PlatformInitDatas() {
		this.cidToEclass = new EClass[Short.MAX_VALUE]; 
		this.eClassToCid = new HashMap<EClass, Short>();
	}
	
	private void initCounter(EClass eClass) {
		ByteBuffer cidBuffer = ByteBuffer.wrap(new byte[8]);
		cidBuffer.putShort(6, getCidOfEClass(eClass));
		long startOid = cidBuffer.getLong(0);
		oidCounters.put(eClass, new AtomicLong(startOid));
	}
	
	private void initialize() {
		List<IfcClassLookup> allLookup = ifcClassLookupDao.queryAllIfcClassLookup();
		if (allLookup.size() > 0) {
			for (IfcClassLookup ifcClassLookup : allLookup) {
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

	@Override
	public long newOid(EClass eClass) {
		return oidCounters.get(eClass).addAndGet(65536);
	}
	
	public void createClassLookupTable() {
		Short cidCounter = 1;
    	for (PackageMetaData packageMetaData : server.getMetaDataManager().getAll()) {
    		List<EClass> allClass = packageMetaData.getAllClasses();
    		for (EClass eclass : allClass) {
    			IfcClassLookup ifcClassLookup = new IfcClassLookup();
    	    	ifcClassLookup.setCid(cidCounter);
    	    	ifcClassLookup.setPackageClassName(packageMetaData.getEPackage().getName() + "_" + eclass.getName());
    	    	eClassToCid.put(eclass, cidCounter);
    	    	cidToEclass[cidCounter] = eclass;
    	    	initCounter(eclass);
    	    	ifcClassLookup.setOid(oidCounters.get(eclass).longValue());
    	    	ifcClassLookupDao.insertIfcClassLookup(ifcClassLookup);
    	    	cidCounter++;
    		}
    	}
	}
	
}
