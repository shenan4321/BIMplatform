package cn.dlb.bim.dao.entity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import cn.dlb.bim.ifc.emf.ModelMetaData;

/**
 * @author shenan4321
 *
 */
@Document(collection = "IfcModelEntity")
public class IfcModelEntity {

	@Id
	private Integer rid;
	private ModelMetaData modelMetaData;
	
	public Integer getRid() {
		return rid;
	}
	public void setRid(Integer rid) {
		this.rid = rid;
	}
	public ModelMetaData getModelMetaData() {
		return modelMetaData;
	}
	public void setModelMetaData(ModelMetaData modelMetaData) {
		this.modelMetaData = modelMetaData;
	}
	
}
