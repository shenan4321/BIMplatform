package cn.dlb.bim.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Repository;

import cn.dlb.bim.dao.VirtualObjectDao;
import cn.dlb.bim.ifc.stream.VirtualObject;

@Repository("VirtualObjectDaoImpl")
public class VirtualObjectDaoImpl extends AbstractBaseMongoDao<VirtualObject> implements VirtualObjectDao {
	
    @Autowired  
	@Override
	protected void setMongoTemplate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	@Cacheable(value="virtualObject")  
	public VirtualObject findOneByRidAndOid(Integer rid, Long oid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("oid").is(oid).andOperator(Criteria.where("rid").is(rid)));
		return findOne(query);
	}

	@Override
	public VirtualObject findOneByRidAndCid(Integer rid, Short cid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("rid").is(rid).andOperator(Criteria.where("eClassId").is(cid)));
		return findOne(query);
	}

	@Override
	public List<VirtualObject> findByRidAndCid(Integer rid, Short cid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("rid").is(rid).andOperator(Criteria.where("eClassId").is(cid)));
		return find(query);
	}

	@Override
	public CloseableIterator<VirtualObject> streamByRidAndCid(Integer rid, Short cid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("rid").is(rid).andOperator(Criteria.where("eClassId").is(cid)));
		return stream(query);
	}
	
}
