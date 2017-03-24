package cn.dlb.bim.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.dlb.bim.dao.IIfcObjectDao;
import cn.dlb.bim.dao.entity.IfcStoreModel;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.service.IIfcStoreService;

@Service("IfcStoreService")
public class IfcStoreServiceImpl implements IIfcStoreService {
	
	@Autowired
	@Qualifier("IfcObjectDaoImpl")
	private IIfcObjectDao ifcObjectDao;

	@Override
	public void insert(IfcModelInterface model) {
		IfcStoreModel ifcStoreModel = new IfcStoreModel();
		ifcStoreModel.setGid(1l);
	}

	@Override
	public IfcModelInterface getModelById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

}
