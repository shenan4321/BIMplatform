package cn.dlb.bim.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.dlb.bim.dao.ConcreteRevisionDao;
import cn.dlb.bim.dao.entity.ConcreteRevision;
import cn.dlb.bim.service.ConcreteRevisionService;

@Service("ConcreteRevisionServiceImpl")
public class ConcreteRevisionServiceImpl implements ConcreteRevisionService {
	
	@Autowired
	private ConcreteRevisionDao oncreteRevisionDao;

	@Override
	public ConcreteRevision findByRid(Integer rid) {
		return oncreteRevisionDao.findByRid(rid);
	}

}
