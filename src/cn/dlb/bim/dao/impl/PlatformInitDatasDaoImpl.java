package cn.dlb.bim.dao.impl;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import cn.dlb.bim.dao.PlatformInitDatasDao;
import cn.dlb.bim.dao.entity.IfcClassLookupEntity;
import cn.dlb.bim.dao.entity.PlatformInitDatasEntity;

@Repository("PlatformInitDatasDaoImpl")
public class PlatformInitDatasDaoImpl implements PlatformInitDatasDao {

	@Autowired  
    private MongoTemplate mongoTemplate; 
	
	public void insertIfcClassLookup(IfcClassLookupEntity ifcClassLookup) {
		mongoTemplate.insert(ifcClassLookup);
	}

	public List<IfcClassLookupEntity> queryAllIfcClassLookup() {
		Query query = new Query();
		query.addCriteria(Criteria.where(""));
		return mongoTemplate.find(query, IfcClassLookupEntity.class);
	}

	public IfcClassLookupEntity queryIfcClassLookupByCid(Short cid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("cid").is(cid));
		return mongoTemplate.findOne(query, IfcClassLookupEntity.class);
	}
	
	public IfcClassLookupEntity queryIfcClassLookupByPackageClassName(String packageClassName) {
		Query query = new Query();
		query.addCriteria(Criteria.where("packageClassName").is(packageClassName));
		return mongoTemplate.findOne(query, IfcClassLookupEntity.class);
	}

	@Override
	public void updateOidInIfcClassLookup(IfcClassLookupEntity ifcClassLookup) {
		Query query = new Query();
		query.addCriteria(Criteria.where("cid").is(ifcClassLookup.getCid()));
		Update update = Update.update("oid", ifcClassLookup.getOid());
		mongoTemplate.updateFirst(query, update, IfcClassLookupEntity.class);
	}

	@Override
	public void insertPlatformInitDatasEntity(PlatformInitDatasEntity platformInitDatasEntity) {
		mongoTemplate.insert(platformInitDatasEntity);
	}

	@Override
	public PlatformInitDatasEntity queryPlatformInitDatasEntityByPlatformVersionId(String platformVersionId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("platformVersionId").is(platformVersionId));
		return mongoTemplate.findOne(query, PlatformInitDatasEntity.class);
	}

	@Override
	public void updatePlatformInitDatasEntity(PlatformInitDatasEntity platformInitDatasEntity) {
		Query query = new Query();
		query.addCriteria(Criteria.where("platformVersionId").is(platformInitDatasEntity.getPlatformVersionId()));
		Update update = Update.update("revisionId", platformInitDatasEntity.getRevisionId());
		mongoTemplate.updateFirst(query, update, PlatformInitDatasEntity.class);
	}
	
}