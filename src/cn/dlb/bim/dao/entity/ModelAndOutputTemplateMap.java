package cn.dlb.bim.dao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ModelAndOutputTemplateMap")
public class ModelAndOutputTemplateMap {
	@Id
	private Integer rid;
	@Indexed
	private Long otid;
	public Integer getRid() {
		return rid;
	}
	public void setRid(Integer rid) {
		this.rid = rid;
	}
	public Long getOtid() {
		return otid;
	}
	public void setOtid(Long otid) {
		this.otid = otid;
	}
}
