package cn.dlb.bim.dao.impl;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import cn.dlb.bim.dao.ConcreteRevisionDao;
import cn.dlb.bim.dao.entity.ConcreteRevision;

@Repository("ConcreteRevisionDaoImpl")
public class ConcreteRevisionDaoImpl extends AbstractBaseMongoDao<ConcreteRevision> implements ConcreteRevisionDao {
    @Autowired  
	@Override
	protected void setMongoTemplate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
    
    @Override
	public ConcreteRevision save(ConcreteRevision concreteRevision) {
		return save(concreteRevision);
	}

	@Override
	public Collection<ConcreteRevision> findByPid(Long pid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("pid").is(pid));
		return find(query);
	}

	@Override
	public void deleteByRid(Integer rid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("revisionId").is(rid));
		findAndDelete(query);
	}

	@Override
	public ConcreteRevision findByRid(Integer rid) {
		Query concreteRevisionQuery = new Query();
		concreteRevisionQuery.addCriteria(Criteria.where("revisionId").is(rid));
		return findOne(concreteRevisionQuery);
	}
}
