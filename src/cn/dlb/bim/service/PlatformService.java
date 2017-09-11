package cn.dlb.bim.service;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.springframework.data.util.CloseableIterator;

import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.emf.OidProvider;
import cn.dlb.bim.ifc.stream.VirtualObject;

public interface PlatformService extends OidProvider {
	public EClassifier getEClassifier(String packageName, String classifierName);
	public Short getCidOfEClass(EClass eClass);
	public EClass getEClassForCid(short cid);
	public EClass getEClassForOid(long oid, Integer rid) throws DatabaseException;
	public Integer newRevisionId();
	void save(VirtualObject virtualObject);
	public void saveBatch(VirtualObject virtualObject);
//	public void commitSaveBatch();
	void update(VirtualObject virtualObject);
	void updateBatch(VirtualObject virtualObject);
//	void commitUpdateBatch();
	void commitAllBatch();
	public List<VirtualObject> queryVirtualObject(Integer rid, List<Short> cids);
	public CloseableIterator<VirtualObject> streamVirtualObject(Integer rid, Short cid);
	public VirtualObject queryVirtualObject(Integer rid, Long oid);
	public CloseableIterator<VirtualObject> streamVirtualObjectByRid(Integer rid);
}
