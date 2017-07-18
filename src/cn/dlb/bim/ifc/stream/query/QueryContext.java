package cn.dlb.bim.ifc.stream.query;

import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.service.PlatformService;

public class QueryContext {
	private int rid;
	private PackageMetaData packageMetaData;
	private PlatformService platformService;
	
	public QueryContext(PlatformService platformService, PackageMetaData packageMetaData, int rid) {
		this.platformService = platformService;
		this.packageMetaData = packageMetaData;
		this.rid = rid;
	}
	
	public PackageMetaData getPackageMetaData() {
		return packageMetaData;
	}
	
	public int getRid() {
		return rid;
	}

	public PlatformService getPlatformService() {
		return platformService;
	}

}