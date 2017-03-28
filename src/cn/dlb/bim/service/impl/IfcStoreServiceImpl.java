package cn.dlb.bim.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.dlb.bim.dao.IfcModelDao;
import cn.dlb.bim.dao.entity.IdEObjectEntity;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.service.IIfcStoreService;

@Service("IfcStoreService")
public class IfcStoreServiceImpl implements IIfcStoreService {
	
	@Autowired
	@Qualifier("IfcModelDaoImpl")
	private IfcModelDao ifcObjectDao;

	@Override
	public void insert(IfcModelInterface model) {
		// TODO Auto-generated method stub
	}

	@Override
	public IfcModelInterface getModelById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

}
