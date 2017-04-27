package cn.dlb.bim.ifc.collada;

import java.io.InputStream;

import com.mongodb.gridfs.GridFSDBFile;

import cn.dlb.bim.component.MongoGridFs;
import cn.dlb.bim.component.PlatformServer;

public class ColladaCacheManager {
	
	public final PlatformServer server;
	
	public ColladaCacheManager(PlatformServer server) {
		this.server = server;
	}
	
	public GridFSDBFile getGlbCache(int rid) {
		MongoGridFs gridFs = server.getMongoGridFs();
		return gridFs.findGlbFile(rid);
	}
	
	public void saveGlb(InputStream glbInput, String fileName, int rid, double lon, double lat) {
		MongoGridFs gridFs = server.getMongoGridFs();
		gridFs.saveGlbFile(glbInput, fileName, rid, lon, lat);
	}
}
