package cn.dlb.bim.lucene;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.search.IndexSearcher;

public class LuceneCacheHelper {

	private static Map<String, IndexSearcher> readerCache;
	
	static {
		readerCache = new HashMap<String, IndexSearcher>();
	}
	
	public static boolean exist(String key) {
		return readerCache.containsKey(key);
	}
	
	public static IndexSearcher get(String key) {
		return readerCache.get(key);
	}
	
	public static void remove(String key) {
		readerCache.remove(key);
	}
	
	public static void insert(String key, IndexSearcher searcher) {
		readerCache.put(key, searcher);
	}
	
}
