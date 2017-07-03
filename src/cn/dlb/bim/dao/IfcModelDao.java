package cn.dlb.bim.dao;

import java.util.List;

import org.springframework.data.util.CloseableIterator;

import cn.dlb.bim.dao.entity.IdEObjectEntity;
import cn.dlb.bim.dao.entity.IfcModelEntity;
import cn.dlb.bim.dao.entity.ModelLabel;
import cn.dlb.bim.ifc.deserializers.stream.VirtualObject;
import cn.dlb.bim.ifc.model.IfcHeader;

public interface IfcModelDao {
	public void insertAllIdEObjectEntity(List<IdEObjectEntity> idEObjectEntitys);
	public void insertIdEObjectEntity(IdEObjectEntity idEObjectEntity);
	public List<IdEObjectEntity> queryIdEObjectEntityByRid(Integer rid);
	public IdEObjectEntity queryIdEObjectEntityByOid(Long oid);
	public List<IdEObjectEntity> queryAllIdEObjectEntityByOids(List<Long> oids);
	public void insertIfcModelEntity(IfcModelEntity ifcModelEntity);
	public IfcModelEntity queryIfcModelEntityByRid(Integer rid);
	public List<IfcModelEntity> queryIfcModelEntityByPid(Long pid);
	public void deleteIdEObjectEntity(Integer rid);
	public void deleteIfcModelEntity(Integer rid);
	/**
	 * 标签操作
	 */
	public void insertModelLabel(ModelLabel modelLabel);
	public void deleteModelLabel(Integer labelId);
	public void modifyModelLabel(ModelLabel modelLabel);
	public List<ModelLabel> queryAllModelLabelByRid(Integer rid);
	
	public void insertVirtualObject(VirtualObject virtualObject);
	public void insertAllVirtualObject(List<VirtualObject> virtualObjects);
	public List<VirtualObject> queryVirtualObject(Integer rid, List<Short> cids);
	public CloseableIterator<VirtualObject> streamVirtualObjectByRid(Integer rid);
	
	public void saveIfcHeader(IfcHeader ifcHeader);
	public IfcHeader queryIfcHeader(Integer rid);
}
