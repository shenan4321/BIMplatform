package cn.dlb.bim.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import cn.dlb.bim.dao.ProjectDao;
import cn.dlb.bim.dao.entity.Project;

@Repository("ProjectDaoImpl")
public class ProjectDaoImpl implements ProjectDao {
	@Autowired  
    private MongoTemplate mongoTemplate; 
	
	@Override
	public void insertProject(Project project) {
		mongoTemplate.insert(project);
	}

	@Override
	public Project queryProject(Long pid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("pid").is(pid));
		return mongoTemplate.findOne(query, Project.class);
	}

	@Override
	public List<Project> queryAllProject() {
		return mongoTemplate.findAll(Project.class);
	}

	@Override
	public void deleteProject(Long pid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("pid").is(pid));
		mongoTemplate.remove(query, Project.class);
	}

	@SuppressWarnings("static-access")
	@Override
	public void updateProject(Project project) {
		Query query = new Query();
		query.addCriteria(Criteria.where("pid").is(project.getPid()));
		mongoTemplate.findAndModify(query, Update.update("author", project.getAuthor())
				.update("title", project.getTitle())
				.update("stars", project.getStars())
				.update("picUrl", project.getPicUrl()), Project.class);
	}

}
