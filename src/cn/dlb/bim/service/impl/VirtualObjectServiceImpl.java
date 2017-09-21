package cn.dlb.bim.service.impl;

import java.util.Collection;
import org.eclipse.emf.ecore.EClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Service;
import cn.dlb.bim.dao.VirtualObjectDao;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.service.CatalogService;
import cn.dlb.bim.service.VirtualObjectService;

@Service("VirtualObjectServiceImpl")
public class VirtualObjectServiceImpl implements VirtualObjectService {

	@Autowired
	private VirtualObjectDao virtualObjectDao;
	
	@Autowired
	private CatalogService catalogService;
	
	public void save(VirtualObject virtualObject) {
		virtualObjectDao.save(virtualObject);
	}
	
	public void saveAll(Collection<VirtualObject> virtualObjects) {
		if (virtualObjects.size() > 0) {
			virtualObjectDao.saveAll(virtualObjects);
		}
	}
	
	@Override
	public VirtualObject findOneByRidAndOid(Integer rid, Long oid) {
		VirtualObject virtualObject = virtualObjectDao.findOneByRidAndOid(rid, oid);
		Short cid = virtualObject.getEClassId();
		EClass eClass = catalogService.getEClassForCid(cid);
		virtualObject.setEClass(eClass);
		return virtualObject;
	}
	
	@Override
	public Collection<VirtualObject> findByRidAndOids(Integer rid, Collection<Long> oids) {
		Collection<VirtualObject> result = virtualObjectDao.findByRidAndOids(rid, oids);
		for (VirtualObject virtualObject : result) {
			Short cid = virtualObject.getEClassId();
			EClass eClass = catalogService.getEClassForCid(cid);
			virtualObject.setEClass(eClass);
		} 
		return result;
	}


	@Override
	public VirtualObject findOneByRidAndCid(Integer rid, Short cid) {
		VirtualObject virtualObject = virtualObjectDao.findOneByRidAndCid(rid, cid);
		EClass eClass = catalogService.getEClassForCid(cid);
		virtualObject.setEClass(eClass);
		return virtualObject;
	}

	@Override
	public Collection<VirtualObject> findByRidAndCid(Integer rid, Short cid) {
		Collection<VirtualObject> result = virtualObjectDao.findByRidAndCid(rid, cid);
		EClass eClass = catalogService.getEClassForCid(cid);
		for (VirtualObject virtualObject : result) {
			virtualObject.setEClass(eClass);
		}
		return result;
	}

	@Override
	public CloseableIterator<VirtualObject> streamByRidAndCid(Integer rid, Short cid) {
		CloseableIterator<VirtualObject> origin = virtualObjectDao.streamByRidAndCid(rid, cid);
		VirtualObjectCloseableIterator result = new VirtualObjectCloseableIterator(origin, catalogService);
		return result;
	}

	@Override
	public VirtualObject update(VirtualObject virtualObject) {
		return virtualObjectDao.update(virtualObject);
	}

	@Override
	public Collection<VirtualObject> findByRidAndCids(Integer rid, Collection<Short> cids) {
		Collection<VirtualObject> result = virtualObjectDao.findByRidAndCids(rid, cids);
		for (VirtualObject virtualObject : result) {
			Short cid = virtualObject.getEClassId();
			EClass eClass = catalogService.getEClassForCid(cid);
			virtualObject.setEClass(eClass);
		} 
		return result;
	}

	@Override
	public CloseableIterator<VirtualObject> streamByRid(Integer rid) {
		CloseableIterator<VirtualObject> origin = virtualObjectDao.streamByRid(rid);
		VirtualObjectCloseableIterator result = new VirtualObjectCloseableIterator(origin, catalogService);
		return result;
	}

	@Override
	public int updateAllVirtualObject(Collection<VirtualObject> virtualObjects) {
		if (virtualObjects.size() > 0) {
			return virtualObjectDao.updateAllVirtualObject(virtualObjects);
		} else {
			return 0;
		}
	}

}
