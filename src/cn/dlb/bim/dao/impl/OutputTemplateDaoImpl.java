package cn.dlb.bim.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import cn.dlb.bim.dao.OutputTemplateDao;
import cn.dlb.bim.dao.entity.ModelAndOutputTemplateMap;
import cn.dlb.bim.dao.entity.OutputTemplate;

@Repository("OutputTemplateDaoImpl")
public class OutputTemplateDaoImpl implements OutputTemplateDao {
	
	@Autowired  
    private MongoTemplate mongoTemplate;

	@Override
	public void insertOutputTemplate(OutputTemplate template) {
		mongoTemplate.save(template);
	}

	@Override
	public void deleteOutputTemplate(Long otid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("otid").is(otid));
		mongoTemplate.remove(query, OutputTemplate.class);
	}

	@Override
	public void modifyOutputTemplate(OutputTemplate template) {
		Query query = new Query();
		query.addCriteria(Criteria.where("otid").is(template.getOtid()));
		Update update = new Update();
		update.set("name", template.getName()).set("namespaceSelectorMap", template.getNamespaceSelectorMap());
		mongoTemplate.findAndModify(query, update, OutputTemplate.class);
	}

	@Override
	public OutputTemplate queryOutputTemplateByOtid(Long otid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("otid").is(otid));
		return mongoTemplate.findOne(query, OutputTemplate.class);
	}

	@Override
	public void insertModelAndOutputTemplateMap(ModelAndOutputTemplateMap map) {
		mongoTemplate.save(map);
	}

	@Override
	public void deleteModelAndOutputTemplateMap(ModelAndOutputTemplateMap map) {
		Query query = new Query();
		query.addCriteria(Criteria.where("rid").is(map.getRid()));
		query.addCriteria(Criteria.where("otid").is(map.getOtid()));
		mongoTemplate.remove(query, OutputTemplate.class);
	}

	@Override
	public List<ModelAndOutputTemplateMap> queryModelAndOutputTemplateMapByRid(Integer rid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("rid").is(rid));
		return mongoTemplate.find(query, ModelAndOutputTemplateMap.class);
	}
	
}
