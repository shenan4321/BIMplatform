package cn.dlb.bim.dao;

import java.util.List;

import cn.dlb.bim.dao.entity.ModelAndOutputTemplateMap;
import cn.dlb.bim.dao.entity.OutputTemplate;

public interface OutputTemplateDao {
	public void insertOutputTemplate(OutputTemplate template);
	public void deleteOutputTemplate(Long otid);
	public void modifyOutputTemplate(OutputTemplate template);
	public OutputTemplate queryOutputTemplateByOtid(Long otid);
	public void insertModelAndOutputTemplateMap(ModelAndOutputTemplateMap map);
	public void deleteModelAndOutputTemplateMap(ModelAndOutputTemplateMap map);
	public List<ModelAndOutputTemplateMap> queryModelAndOutputTemplateMapByRid(Integer rid);
}
