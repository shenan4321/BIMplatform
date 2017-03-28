package cn.dlb.bim.dao;

import java.util.List;

import cn.dlb.bim.dao.entity.IdEObjectEntity;
import cn.dlb.bim.dao.entity.IfcModelEntity;

public interface IfcModelDao {
	public void insertIdEObjectEntity(IdEObjectEntity idEObjectEntity);
	public List<IdEObjectEntity> queryIdEObjectEntityByRid(Integer rid);
	public IdEObjectEntity queryIdEObjectEntityByOid(Long oid);
	public void insertIfcModelEntity(IfcModelEntity ifcModelEntity);
	public IfcModelEntity queryIfcModelEntityByRid(Integer rid);
}
