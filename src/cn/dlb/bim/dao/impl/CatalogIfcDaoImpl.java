package cn.dlb.bim.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import cn.dlb.bim.dao.entity.ConcreteRevision;
import cn.dlb.bim.dao.entity.CatalogIfc;

@Repository("CatalogIfcDaoImpl")
public class CatalogIfcDaoImpl extends AbstractBaseMongoDao<CatalogIfc> {
    @Autowired  
	@Override
	protected void setMongoTemplate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
}
