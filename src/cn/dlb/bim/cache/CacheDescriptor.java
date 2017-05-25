package cn.dlb.bim.cache;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

public class CacheDescriptor {
	private static final HashFunction hf = Hashing.md5();
	private Number[] ids;
	private String queryKey;

	public CacheDescriptor(String queryKey, Number... ids) {
		this.queryKey = queryKey;
		this.ids = ids;
	}
	
	public String getFileNameWithoutExtension() {
		return getCacheKey();
	}
	
	public String getCacheKey() {
		Hasher hasher = hf.newHasher();
		hasher.putUnencodedChars(queryKey);
		for (Number id : ids) {
			if (id instanceof Integer) {
				hasher.putInt(id.intValue());
			} else if (id instanceof Long) {
				hasher.putLong(id.longValue());
			} else if (id instanceof Short) {
				hasher.putLong(id.shortValue());
			} else if (id instanceof Double) {
				hasher.putDouble(id.doubleValue());
			} else if (id instanceof Float) {
				hasher.putFloat(id.floatValue());
			} else if (id instanceof Byte) {
				hasher.putFloat(id.byteValue());
			}
		}
		HashCode hashcode = hasher.hash();
		return hashcode.toString();
	}

}
