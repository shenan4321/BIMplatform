package cn.dlb.bim.cache;

import cn.dlb.bim.ifc.emf.IfcModelInterface;

public class ModelCacheManager {
	private final static int cacheSize = 10;//根据服务器性能配置
	LRUCache<Integer, IfcModelInterface> modelCache = new LRUCache<>(cacheSize);
	
	public IfcModelInterface getIfcModel(Integer rid) {
		return modelCache.get(rid);
	}
	
	public void cleanAll() {
		modelCache.clear();
	}
	
	public void cacheModel(Integer rid, IfcModelInterface model) {
		modelCache.put(rid, model);
	}
	
	public boolean contains(Integer rid) {
		return modelCache.contains(rid);
	}
}
