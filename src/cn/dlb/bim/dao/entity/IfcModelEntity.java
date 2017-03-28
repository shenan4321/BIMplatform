package cn.dlb.bim.dao.entity;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import cn.dlb.bim.ifc.emf.ModelMetaData;

@Document(collection = "IfcModelEntity")
public class IfcModelEntity {

	private Integer rid;
	private List<Long> objectOids;
	private ModelMetaData modelMetaData;
	
	public Integer getRid() {
		return rid;
	}
	public void setRid(Integer rid) {
		this.rid = rid;
	}
	public List<Long> getObjectOids() {
		return objectOids;
	}
	public void setObjectOids(List<Long> objectOids) {
		this.objectOids = objectOids;
	}
	public ModelMetaData getModelMetaData() {
		return modelMetaData;
	}
	public void setModelMetaData(ModelMetaData modelMetaData) {
		this.modelMetaData = modelMetaData;
	}
	
	
	
}
