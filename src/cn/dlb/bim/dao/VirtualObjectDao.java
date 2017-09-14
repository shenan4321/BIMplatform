package cn.dlb.bim.dao;

import java.util.List;
import org.springframework.data.util.CloseableIterator;
import cn.dlb.bim.ifc.stream.VirtualObject;

public interface VirtualObjectDao extends BaseMongoDao<VirtualObject> {
	public VirtualObject findOneByRidAndOid(Integer rid, Long oid);
	public VirtualObject findOneByRidAndCid(Integer rid, Short cid);
	public List<VirtualObject> findByRidAndCid(Integer rid, Short cid);
	public CloseableIterator<VirtualObject> streamByRidAndCid(Integer rid, Short cid);
	public VirtualObject update(VirtualObject virtualObject);
	public List<VirtualObject> findByRidAndCids(Integer rid, List<Short> cids);
	public CloseableIterator<VirtualObject> streamByRid(Integer rid);
	public int updateAllVirtualObject(List<VirtualObject> virtualObjects);
}
