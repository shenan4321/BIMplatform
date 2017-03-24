package cn.dlb.bim.ifc.database;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IdEObjectImpl;

public class ObjectCache {
	private final BiMap<Long, IdEObjectImpl> oidCache = HashBiMap.create();

	public void put(long oid, IdEObject object) {
		if (!oidCache.containsValue(object)) {
			oidCache.put(oid, (IdEObjectImpl) object);
		}
	}
	
	public IdEObject get(long oid) {
		return oidCache.get(oid);
	}
	
	public long get(IdEObject object) {
		return oidCache.inverse().get(object);
	}
	
	public boolean contains(IdEObject object) {
		return oidCache.inverse().containsKey(object);
	}

	public boolean contains(long oid) {
		return oidCache.containsKey(oid);
	}

	public void clear() {
		oidCache.clear();
	}
}