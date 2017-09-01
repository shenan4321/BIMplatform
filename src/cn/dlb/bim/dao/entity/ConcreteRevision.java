package cn.dlb.bim.dao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import cn.dlb.bim.ifc.model.IfcHeader;
import cn.dlb.bim.vo.Vector3f;

@Document(collection = "ConcreteRevision")
public class ConcreteRevision {
	@Id
	private Integer revisionId;
	@Indexed
	private Long pid;
	private IfcHeader ifcHeader;
	private String date;
	private String name;
	private Integer applyType;
	private String fileName;
	private Long fileSize;
	private String schema;
	private Vector3f minBounds;
	private Vector3f maxBounds;
	
	public Integer getRevisionId() {
		return revisionId;
	}
	public void setRevisionId(Integer revisionId) {
		this.revisionId = revisionId;
	}
	public Long getPid() {
		return pid;
	}
	public void setPid(Long pid) {
		this.pid = pid;
	}
	public IfcHeader getIfcHeader() {
		return ifcHeader;
	}
	public void setIfcHeader(IfcHeader ifcHeader) {
		this.ifcHeader = ifcHeader;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
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
	public Long getFileSize() {
		return fileSize;
	}
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public Vector3f getMinBounds() {
		return minBounds;
	}
	public void setMinBounds(Vector3f minBounds) {
		this.minBounds = minBounds;
	}
	public Vector3f getMaxBounds() {
		return maxBounds;
	}
	public void setMaxBounds(Vector3f maxBounds) {
		this.maxBounds = maxBounds;
	}
	
	
}
