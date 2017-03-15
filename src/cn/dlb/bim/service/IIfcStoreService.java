package cn.dlb.bim.service;

import cn.dlb.bim.emf.IfcModelInterface;

public interface IIfcStoreService {

	public void insert(IfcModelInterface model);
	public IfcModelInterface getModelById(Long id);
	
}
