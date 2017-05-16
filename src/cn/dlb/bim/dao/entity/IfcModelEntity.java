package cn.dlb.bim.dao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
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
	@Indexed
	private Long pid;
	private ModelMetaData modelMetaData;
	private String name;
	private Integer applyType;
	private String fileName;
	private String uploadDate;
	private Long fileSize;
	
	public Integer getRid() {
		return rid;
	}
	public void setRid(Integer rid) {
		this.rid = rid;
	}
	public Long getPid() {
		return pid;
	}
	public void setPid(Long pid) {
		this.pid = pid;
	}
	public ModelMetaData getModelMetaData() {
		return modelMetaData;
	}
	public void setModelMetaData(ModelMetaData modelMetaData) {
		this.modelMetaData = modelMetaData;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getApplyType() {
		return applyType;
	}
	public void setApplyType(Integer applyType) {
		this.applyType = applyType;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getUploadDate() {
		return uploadDate;
	}
	public void setUploadDate(String uploadDate) {
		this.uploadDate = uploadDate;
	}
	public Long getFileSize() {
		return fileSize;
	}
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
	
}
