package cn.dlb.bim.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.dlb.bim.dao.IIfcObjectDao;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.model.IfcStoreModel;
import cn.dlb.bim.service.IIfcStoreService;
import cn.dlb.bim.utils.ByteUtil;

@Service("IfcStoreService")
public class IfcStoreServiceImpl implements IIfcStoreService {
	
	@Autowired
	private IIfcObjectDao ifcObjectDao;

	@Override
	public void insert(IfcModelInterface model) {
		IfcStoreModel ifcStoreModel = new IfcStoreModel();
		ifcStoreModel.setGid(1l);
		byte[] ifcObjectBytes = ByteUtil.toByteArray(model);
		ifcStoreModel.setIfcObjectBytes(ifcObjectBytes);
		ifcObjectDao.insertIfcStoreModel(ifcStoreModel);
	}

	@Override
	public IfcModelInterface getModelById(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

}
