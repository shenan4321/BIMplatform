package cn.dlb.bim.dao;

import java.util.Collection;

import cn.dlb.bim.dao.entity.ConcreteRevision;

public interface ConcreteRevisionDao extends BaseMongoDao<ConcreteRevision> {
	public ConcreteRevision save(ConcreteRevision concreteRevision);
	public Collection<ConcreteRevision> findByPid(Long pid);
	public void deleteByRid(Integer rid);
	public ConcreteRevision findByRid(Integer rid);
}
