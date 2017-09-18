package cn.dlb.bim.cache;

import java.util.concurrent.TimeoutException;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.exception.MemcachedException;

public class MemcachedCache implements Cache {

	private final String name;
	private final MemcachedClient memcachedClient;
	private final int expire;  

	public MemcachedCache(String name, int expire, MemcachedClient memcachedClient) {
		this.name = name;
		this.memcachedClient = memcachedClient;
		this.expire = expire;
	}

	@Override
	public void clear() {
		try {  
            memcachedClient.flushAll();  
        } catch (TimeoutException e) {  
            e.printStackTrace();  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        } catch (MemcachedException e) {  
            e.printStackTrace();  
        }  
	}

	@Override
	public void evict(Object key) {
		try {  
            memcachedClient.delete(getKey(key));  
        } catch (TimeoutException e) {  
            e.printStackTrace();  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        } catch (MemcachedException e) {  
            e.printStackTrace();  
        }    
	}

	@Override
	public ValueWrapper get(Object key) {
		Object object = null;  
        try {
            object = memcachedClient.get(getKey(key));  
        } catch (TimeoutException e) {  
            e.printStackTrace();  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        } catch (MemcachedException e) {  
            e.printStackTrace();  
        }  
        return (object != null ? new SimpleValueWrapper(object) : null); 
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public MemcachedClient getNativeCache() {
		return this.memcachedClient;
	}

	@Override
	public void put(Object key, Object value) {
		try {  
            memcachedClient.setWithNoReply(getKey(key), expire, value);  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        } catch (MemcachedException e) {  
            e.printStackTrace();  
        }  
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Object key, Class<T> valueClass) {
		Object object = null;  
        try {  
            object = memcachedClient.get(getKey(key));  
        } catch (TimeoutException e) {  
            e.printStackTrace();  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        } catch (MemcachedException e) {  
            e.printStackTrace();  
        }  
        return (T)object;  
	}

	@Override
	public ValueWrapper putIfAbsent(Object key, Object value) {
		ValueWrapper wrapper = get(key);
		if (wrapper == null) {
			put(key, value);
			wrapper = new SimpleValueWrapper(value);
		}
		return wrapper;
	}
	
	private String getKey(Object key){  
        return name + "_" + key;  
    }

}
