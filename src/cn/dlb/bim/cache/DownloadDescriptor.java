package cn.dlb.bim.cache;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import cn.dlb.bim.ifc.emf.PackageMetaData;

public class DownloadDescriptor {
	private static final HashFunction hf = Hashing.md5();
	private Integer rid;
	private String queryKey;

	public DownloadDescriptor(Integer rid, String queryKey) {
		this.queryKey = queryKey;
		this.rid = rid;
	}
	
	public String getFileNameWithoutExtension() {
		return rid + "_" + getCacheKey();
	}
	
	public String getCacheKey() {
		Hasher hasher = hf.newHasher();
		// TODO This serializerOid actually makes the cache a per-user cache... Maybe not the most useful feature
		hasher.putLong(rid);
		hasher.putUnencodedChars(queryKey);
		HashCode hashcode = hasher.hash();
		return hashcode.toString();
	}

}
