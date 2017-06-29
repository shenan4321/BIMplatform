package cn.dlb.bim.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import cn.dlb.bim.dao.IfcModelDao;
import cn.dlb.bim.dao.entity.IdEObjectEntity;
import cn.dlb.bim.dao.entity.IfcModelEntity;
import cn.dlb.bim.dao.entity.ModelLabel;
import cn.dlb.bim.dao.entity.Project;
import cn.dlb.bim.ifc.deserializers.stream.VirtualObject;

@Repository("IfcModelDaoImpl")
public class IfcModelDaoImpl implements IfcModelDao {
	
	@Autowired  
    private MongoTemplate mongoTemplate; 
	
	@Override
	public void insertIdEObjectEntity(IdEObjectEntity idEObjectEntity) {
		mongoTemplate.insert(idEObjectEntity);
	}
	
	@Override
	public void insertAllIdEObjectEntity(List<IdEObjectEntity> idEObjectEntitys) {
		mongoTemplate.insertAll(idEObjectEntitys);
	}

	@Override
	public List<IdEObjectEntity> queryIdEObjectEntityByRid(Integer rid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("rid").is(rid));
		return mongoTemplate.find(query, IdEObjectEntity.class);
	}

	@Override
	public IdEObjectEntity queryIdEObjectEntityByOid(Long oid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("oid").is(oid));
		return mongoTemplate.findOne(query, IdEObjectEntity.class);
	}
	
	@Override
	public void insertIfcModelEntity(IfcModelEntity ifcModelEntity) {
		mongoTemplate.insert(ifcModelEntity);
	}
	
	@Override
	public IfcModelEntity queryIfcModelEntityByRid(Integer rid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("rid").is(rid));
		return mongoTemplate.findOne(query, IfcModelEntity.class);
	}

	@Override
	public List<IdEObjectEntity> queryAllIdEObjectEntityByOids(List<Long> oids) {
		Query query = new Query();
		query.addCriteria(Criteria.where("oid").in(oids));
		return mongoTemplate.find(query, IdEObjectEntity.class);
	}

	@Override
	public List<IfcModelEntity> queryIfcModelEntityByPid(Long pid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("pid").is(pid));
		return mongoTemplate.find(query, IfcModelEntity.class);
	}

	@Override
	public void deleteIdEObjectEntity(Integer rid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("rid").is(rid));
		mongoTemplate.findAllAndRemove(query, IdEObjectEntity.class);
	}

	@Override
	public void deleteIfcModelEntity(Integer rid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("rid").is(rid));
		mongoTemplate.findAllAndRemove(query, IfcModelEntity.class);
	}

	@Override
	public void insertModelLabel(ModelLabel modelLabel) {
		mongoTemplate.save(modelLabel);
	}

	@Override
	public void deleteModelLabel(Integer labelId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("labelId").is(labelId));
		mongoTemplate.findAllAndRemove(query, ModelLabel.class);
	}

	@Override
	public void modifyModelLabel(ModelLabel modelLabel) {
		Query query = new Query();
		query.addCriteria(Criteria.where("labelId").is(modelLabel.getLabelId()));
		
		Update update = new Update();
		update.set("name", modelLabel.getName())
			.set("description", modelLabel.getDescription())
			.set("developData", modelLabel.getDevelopData())
			.set("x", modelLabel.getX())
			.set("y", modelLabel.getY())
			.set("z", modelLabel.getZ())
			.set("red", modelLabel.getRed())
			.set("green", modelLabel.getGreen())
			.set("blue", modelLabel.getBlue());
		
		mongoTemplate.findAndModify(query, update, ModelLabel.class);
	}

	@Override
	public List<ModelLabel> queryAllModelLabelByRid(Integer rid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("rid").is(rid));
		return mongoTemplate.find(query, ModelLabel.class);
	}

	@Override
	public void insertVirtualObject(VirtualObject virtualObject) {
		mongoTemplate.save(virtualObject);
	}

}
