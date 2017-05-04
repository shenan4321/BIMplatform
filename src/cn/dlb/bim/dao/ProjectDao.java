package cn.dlb.bim.dao;

import java.util.List;

import cn.dlb.bim.dao.entity.BIMProject;

public interface ProjectDao {
	public void insertProject(BIMProject project);
	public BIMProject queryProject(Long pid);
	public List<BIMProject> queryAllProject();
}
