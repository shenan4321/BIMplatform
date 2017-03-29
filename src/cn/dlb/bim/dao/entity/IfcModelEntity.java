package cn.dlb.bim.dao.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import cn.dlb.bim.ifc.emf.ModelMetaData;

/**
 * @author shenan4321
 *
 */
//@Document(collection = "IfcModelEntity")
public class IfcModelEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8076654045775078661L;

	private Integer rid;
	private List<IdEObjectEntity> objectEntities = new ArrayList<>();
	private List<IdEObjectEntity> unidentifiedObjectEntities = new ArrayList<>();
	private ModelMetaData modelMetaData;
	
	public Integer getRid() {
		return rid;
	}
	public void setRid(Integer rid) {
		this.rid = rid;
	}
	public List<IdEObjectEntity> getObjectEntities() {
		return objectEntities;
	}
	public void setObjectEntities(List<IdEObjectEntity> objectEntities) {
		this.objectEntities = objectEntities;
	}
	public ModelMetaData getModelMetaData() {
		return modelMetaData;
	}
	public void setModelMetaData(ModelMetaData modelMetaData) {
		this.modelMetaData = modelMetaData;
	}
	public List<IdEObjectEntity> getUnidentifiedObjectEntities() {
		return unidentifiedObjectEntities;
	}
	public void setUnidentifiedObjectEntities(List<IdEObjectEntity> unidentifiedObjectEntities) {
		this.unidentifiedObjectEntities = unidentifiedObjectEntities;
	}
	
}
