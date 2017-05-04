package cn.dlb.bim.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import cn.dlb.bim.dao.ProjectDao;
import cn.dlb.bim.dao.entity.BIMProject;

@Repository("ProjectDaoImpl")
public class ProjectDaoImpl implements ProjectDao {
	@Autowired  
    private MongoTemplate mongoTemplate; 
	
	@Override
	public void insertProject(BIMProject project) {
		mongoTemplate.insert(project);
	}

	@Override
	public BIMProject queryProject(Long pid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("pid").is(pid));
		return mongoTemplate.findOne(query, BIMProject.class);
	}

	@Override
	public List<BIMProject> queryAllProject() {
		return mongoTemplate.findAll(BIMProject.class);
	}

}
