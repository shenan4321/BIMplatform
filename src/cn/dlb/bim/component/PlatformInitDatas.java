package cn.dlb.bim.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import cn.dlb.bim.dao.IIfcClassLookupDAO;
import cn.dlb.bim.dao.entity.IfcClassLookup;
import cn.dlb.bim.ifc.binary.IfcDataBase;
import cn.dlb.bim.ifc.engine.jvm.JvmRenderEngineFactory;
import cn.dlb.bim.utils.IdentifyUtil;

/**
 * @author shenan4321
 *
 */
@Component("PlatformInitDatas")
public class PlatformInitDatas implements InitializingBean, IfcDataBase {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JvmRenderEngineFactory.class);
	
	private final EClass[] cidToEclass;
	private final Map<EClass, Short> eClassToCid;
	
	@Autowired
	private IIfcClassLookupDAO ifcClassLookupDao;
	
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
	
	private void initialize() {
		List<IfcClassLookup> allLookup = ifcClassLookupDao.queryAllIfcClassLookup();
		for (IfcClassLookup ifcClassLookup : allLookup) {
			String packageClassName = ifcClassLookup.getPackageClassName();
			String packageName = packageClassName.substring(0, packageClassName.indexOf("_"));
			String className = packageClassName.substring(packageClassName.indexOf("_") + 1);
			EClass eClass = (EClass) getEClassifier(packageName, className);
			cidToEclass[ifcClassLookup.getCid()] = eClass;
			eClassToCid.put(eClass, ifcClassLookup.getCid());
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
		Short cid = getCidOfEClass(eClass);
		long rawId = IdentifyUtil.nextId();
		String oid = "" + cid + rawId;
		return Long.valueOf(oid);
	}
	
}
