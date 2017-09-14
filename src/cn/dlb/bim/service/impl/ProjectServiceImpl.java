package cn.dlb.bim.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.dlb.bim.dao.ProjectDao;
import cn.dlb.bim.dao.entity.Project;
import cn.dlb.bim.service.ProjectService;

@Service("ProjectServiceImpl")
public class ProjectServiceImpl implements ProjectService {

	@Autowired
	@Qualifier("ProjectDaoImpl")
	private ProjectDao projectDao;
	
	@Override
	public void addProject(Project project) {
		projectDao.insertProject(project);
	}

	@Override
	public void deleteProject(Long pid) {
		projectDao.deleteProject(pid);
	}

	@Override
	public void updateProject(Project project) {
		projectDao.updateProject(project);
	}

	@Override
	public Project queryProject(Long pid) {
		return projectDao.queryProject(pid);
	}

	@Override
	public List<Project> queryAllProject() {
		return projectDao.queryAllProject();
	}

}
