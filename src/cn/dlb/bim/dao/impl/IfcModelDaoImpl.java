package cn.dlb.bim.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import cn.dlb.bim.dao.IfcModelDao;
import cn.dlb.bim.dao.entity.IdEObjectEntity;
import cn.dlb.bim.dao.entity.IfcModelEntity;

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

}
