package cn.dlb.bim.dao.entity;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ModelAndOutputTemplateMap")
public class ModelAndOutputTemplateMap {
	@Id
	private Integer rid;
	private Map<Long, String> otid2Name;
	
	public ModelAndOutputTemplateMap() {
		otid2Name = new HashMap<>();
	}
	
	public Integer getRid() {
		return rid;
	}
	public void setRid(Integer rid) {
		this.rid = rid;
	}
	public Map<Long, String> getOtid2Name() {
		return otid2Name;
	}
	public void setOtid2Name(Map<Long, String> otid2Name) {
		this.otid2Name = otid2Name;
	}

}
