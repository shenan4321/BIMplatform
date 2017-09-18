package cn.dlb.bim.ifc.stream.query;

import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.service.CatalogService;
import cn.dlb.bim.service.VirtualObjectService;

public class QueryContext {
	private int rid;
	private PackageMetaData packageMetaData;
	private CatalogService catalogService;
	private VirtualObjectService virtualObjectService;
	
	public QueryContext(CatalogService catalogService, VirtualObjectService virtualObjectService, PackageMetaData packageMetaData, int rid) {
		this.catalogService = catalogService;
		this.virtualObjectService = virtualObjectService;
		this.packageMetaData = packageMetaData;
		this.rid = rid;
	}
	
	public PackageMetaData getPackageMetaData() {
		return packageMetaData;
	}
	
	public int getRid() {
		return rid;
	}
	
	public CatalogService getCatalogService() {
		return catalogService;
	}

	public VirtualObjectService getVirtualObjectService() {
		return virtualObjectService;
	}

}