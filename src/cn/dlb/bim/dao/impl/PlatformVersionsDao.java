package cn.dlb.bim.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import cn.dlb.bim.dao.entity.PlatformVersions;

@Repository("PlatformVersionsDaoImpl")
public class PlatformVersionsDao extends AbstractBaseMongoDao<PlatformVersions>  {
	@Autowired  
	@Override
	protected void setMongoTemplate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}
}
