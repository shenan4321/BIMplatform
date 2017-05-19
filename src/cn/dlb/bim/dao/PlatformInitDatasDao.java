package cn.dlb.bim.dao;


import java.util.List;

import cn.dlb.bim.dao.entity.IfcClassLookupEntity;
import cn.dlb.bim.dao.entity.PlatformInitDatasEntity;

public interface PlatformInitDatasDao {
	public void insertIfcClassLookup(IfcClassLookupEntity ifcClassLookup);

	public List<IfcClassLookupEntity> queryAllIfcClassLookup();

	public IfcClassLookupEntity queryIfcClassLookupByCid(Short cid);
	
	public IfcClassLookupEntity queryIfcClassLookupByPackageClassName(String packageClassName);
	
	public void updateOidInIfcClassLookup(IfcClassLookupEntity ifcClassLookup);
	
	public void insertPlatformInitDatasEntity(PlatformInitDatasEntity platformInitDatasEntity);
	
	public PlatformInitDatasEntity queryPlatformInitDatasEntityByPlatformVersionId(String platformVersionId);
	
	public IfcClassLookupEntity findAndIncreateOid(Short cid, Integer increase);
	
	public PlatformInitDatasEntity findAndIncreateRevisionId(String platformVersionId, Integer increase);
}