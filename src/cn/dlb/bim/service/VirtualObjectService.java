package cn.dlb.bim.service;

import java.util.Collection;

import org.springframework.data.util.CloseableIterator;

import cn.dlb.bim.ifc.stream.VirtualObject;

public interface VirtualObjectService {
	public void save(VirtualObject virtualObject);
	public void saveAll(Collection<VirtualObject> virtualObjects);
	public VirtualObject findOneByRidAndOid(Integer rid, Long oid);
	public VirtualObject findOneByRidAndCid(Integer rid, Short cid);
	public Collection<VirtualObject> findByRidAndCid(Integer rid, Short cid);
	public CloseableIterator<VirtualObject> streamByRidAndCid(Integer rid, Short cid);
	public VirtualObject update(VirtualObject virtualObject);
	public Collection<VirtualObject> findByRidAndCids(Integer rid, Collection<Short> cids);
	public CloseableIterator<VirtualObject> streamByRid(Integer rid);
	public int updateAllVirtualObject(Collection<VirtualObject> virtualObjects);
}
