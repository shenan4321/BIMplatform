package cn.dlb.bim.dao;


import java.util.List;

import cn.dlb.bim.dao.entity.IfcClassLookup;

public interface IIfcClassLookupDAO {
	public int insertIfcClassLookup(IfcClassLookup ifcClassLookup);

	public List<IfcClassLookup> queryAllIfcClassLookup();

	public IfcClassLookup queryIfcClassLookupByCid(Short cid);
	
	public IfcClassLookup queryIfcClassLookupByPackageClassName(String packageClassName);
}