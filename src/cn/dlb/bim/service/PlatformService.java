package cn.dlb.bim.service;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;

import cn.dlb.bim.ifc.database.DatabaseException;
import cn.dlb.bim.ifc.deserializers.stream.VirtualObject;
import cn.dlb.bim.ifc.emf.OidProvider;

public interface PlatformService extends OidProvider {
	public EClassifier getEClassifier(String packageName, String classifierName);
	public Short getCidOfEClass(EClass eClass);
	public EClass getEClassForCid(short cid);
	public EClass getEClassForOid(long oid) throws DatabaseException;
	public Integer newRevisionId();
	public void syncOid();
	void save(VirtualObject virtualObject) throws DatabaseException;
	void saveOverwrite(VirtualObject virtualObject) throws DatabaseException;
}
