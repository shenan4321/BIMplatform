package cn.dlb.bim.service;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.springframework.data.util.CloseableIterator;

import cn.dlb.bim.ifc.database.DatabaseException;
import cn.dlb.bim.ifc.emf.OidProvider;
import cn.dlb.bim.ifc.model.IfcHeader;
import cn.dlb.bim.ifc.stream.VirtualObject;

public interface PlatformService extends OidProvider {
	public EClassifier getEClassifier(String packageName, String classifierName);
	public Short getCidOfEClass(EClass eClass);
	public EClass getEClassForCid(short cid);
	public EClass getEClassForOid(long oid) throws DatabaseException;
	public Integer newRevisionId();
	public void syncOid();
	void save(VirtualObject virtualObject) throws DatabaseException;
	void saveOverwrite(VirtualObject virtualObject) throws DatabaseException;
	public void saveBatch(VirtualObject virtualObject);
	public void commitSaveBatch();
	public List<VirtualObject> queryVirtualObject(Integer rid, List<Short> cids);
	public CloseableIterator<VirtualObject> streamVirtualObjectByRid(Integer rid);
	public void saveIfcHeader(IfcHeader ifcHeader);
	public IfcHeader queryIfcHeader(Integer rid);
}
