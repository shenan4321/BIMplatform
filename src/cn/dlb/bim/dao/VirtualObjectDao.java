package cn.dlb.bim.dao;

import java.util.Collection;
import org.springframework.data.util.CloseableIterator;
import cn.dlb.bim.ifc.stream.VirtualObject;

public interface VirtualObjectDao extends BaseMongoDao<VirtualObject> {
	public VirtualObject findOneByRidAndOid(Integer rid, Long oid);
	public Collection<VirtualObject> findByRidAndOids(Integer rid, Collection<Long> oids);
	public VirtualObject findOneByRidAndCid(Integer rid, Short cid);
	public Collection<VirtualObject> findByRidAndCid(Integer rid, Short cid);
	public CloseableIterator<VirtualObject> streamByRidAndCid(Integer rid, Short cid);
	public VirtualObject update(VirtualObject virtualObject);
	public Collection<VirtualObject> findByRidAndCids(Integer rid, Collection<Short> cids);
	public CloseableIterator<VirtualObject> streamByRid(Integer rid);
	public int updateAllVirtualObject(Collection<VirtualObject> virtualObjects);
}
