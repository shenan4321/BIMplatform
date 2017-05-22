package cn.dlb.bim.service;

import java.util.List;

import cn.dlb.bim.dao.entity.Project;

public interface ProjectService {
	public void addProject(Project project);
	public void deleteProject(Long pid);
	public void updateProject(Project project);
	public Project queryProject(Long pid);
	public List<Project> queryAllProject();
	public Project queryProjectByRid(Integer rid);
}
