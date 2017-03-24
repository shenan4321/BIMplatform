package cn.dlb.bim.dao.impl;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import cn.dlb.bim.dao.IIfcClassLookupDao;
import cn.dlb.bim.dao.entity.IfcClassLookup;

@Repository("IfcClassLookupDaoImpl")
public class IfcClassLookupDaoImpl implements IIfcClassLookupDao {

	@Autowired  
    private MongoTemplate mongoTemplate; 
	
	public void insertIfcClassLookup(IfcClassLookup ifcClassLookup) {
		mongoTemplate.insert(ifcClassLookup);
	}

	public List<IfcClassLookup> queryAllIfcClassLookup() {
		Query query = new Query();
		query.addCriteria(Criteria.where(""));
		return mongoTemplate.find(query, IfcClassLookup.class);
	}

	public IfcClassLookup queryIfcClassLookupByCid(Short cid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("cid").is(cid));
		return mongoTemplate.findOne(query, IfcClassLookup.class);
	}
	
	public IfcClassLookup queryIfcClassLookupByPackageClassName(String packageClassName) {
		Query query = new Query();
		query.addCriteria(Criteria.where("packageClassName").is(packageClassName));
		return mongoTemplate.findOne(query, IfcClassLookup.class);
	}
	
}