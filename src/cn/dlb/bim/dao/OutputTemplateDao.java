package cn.dlb.bim.dao;

import cn.dlb.bim.dao.entity.ModelAndOutputTemplateMap;
import cn.dlb.bim.dao.entity.OutputTemplate;

public interface OutputTemplateDao {
	public void insertOutputTemplate(OutputTemplate template);
	public void deleteOutputTemplate(Long otid);
	public void modifyOutputTemplate(OutputTemplate template);
	public OutputTemplate queryOutputTemplateByOtid(Long otid);
	public void saveModelAndOutputTemplateMap(ModelAndOutputTemplateMap map);
	public void deleteModelAndOutputTemplateMap(Integer rid, Long otid);
	public ModelAndOutputTemplateMap queryModelAndOutputTemplateMapByRid(Integer rid);
}
