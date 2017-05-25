package cn.dlb.bim.utils;

import java.io.IOException;
import java.util.List;

import cn.dlb.bim.cache.CacheDescriptor;
import cn.dlb.bim.cache.NewDiskCacheManager;

public class CacheUtils<T> {
	private NewDiskCacheManager cacheManager;
	
	public CacheUtils(NewDiskCacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
	
	public void cacheList(CacheDescriptor downloadDescriptor, List<T> listToCache) {
		try {
			JsonUtils.objectToJson(cacheManager.startCaching(downloadDescriptor), listToCache);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void cacheObject(CacheDescriptor downloadDescriptor, T ObjectToCache) {
		try {
			JsonUtils.objectToJson(cacheManager.startCaching(downloadDescriptor), ObjectToCache);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public List<T> readListFromCache(CacheDescriptor downloadDescriptor, Class<T> classType) {
		List<T> result = null;
		if (cacheManager.contains(downloadDescriptor)) {
			byte[] dataBytes = cacheManager.getData(downloadDescriptor);
			try {
				result = JsonUtils.readList(dataBytes, classType);
			} catch (IOException e) {
				cacheManager.remove(downloadDescriptor);
				e.printStackTrace();
			}
		}
		return result;
	}
	
	public T readObjectFromCache(CacheDescriptor downloadDescriptor, Class<T> classType) {
		T result = null;
		if (cacheManager.contains(downloadDescriptor)) {
			byte[] dataBytes = cacheManager.getData(downloadDescriptor);
			try {
				result = JsonUtils.readObject(dataBytes, classType);
			} catch (IOException e) {
				cacheManager.remove(downloadDescriptor);
				e.printStackTrace();
			}
		}
		return result;
	}
	
}
