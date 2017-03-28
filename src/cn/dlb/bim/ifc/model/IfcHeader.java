package cn.dlb.bim.ifc.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IfcHeader {

	private List<String> description;
	private String implementationLevel;
	private String filename;
	private Date timeStamp;
	private List<String> author;
	private List<String> organization;
	private String preProcessorVersion;
	private String originatingSystem;
	private String ifcSchemaVersion;
	private String authorization;
	
	public IfcHeader() {
		description = new ArrayList<>();
		author = new ArrayList<>();
		organization = new ArrayList<>();
	}
	
	public List<String> getDescription() {
		return description;
	}
	public void setDescription(List<String> description) {
		this.description = description;
	}
	public String getImplementationLevel() {
		return implementationLevel;
	}
	public void setImplementationLevel(String implementationLevel) {
		this.implementationLevel = implementationLevel;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public Date getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}
	public List<String> getAuthor() {
		return author;
	}
	public void setAuthor(List<String> author) {
		this.author = author;
	}
	public List<String> getOrganization() {
		return organization;
	}
	public void setOrganization(List<String> organization) {
		this.organization = organization;
	}
	public String getPreProcessorVersion() {
		return preProcessorVersion;
	}
	public void setPreProcessorVersion(String preProcessorVersion) {
		this.preProcessorVersion = preProcessorVersion;
	}
	public String getOriginatingSystem() {
		return originatingSystem;
	}
	public void setOriginatingSystem(String originatingSystem) {
		this.originatingSystem = originatingSystem;
	}
	public String getIfcSchemaVersion() {
		return ifcSchemaVersion;
	}
	public void setIfcSchemaVersion(String ifcSchemaVersion) {
		this.ifcSchemaVersion = ifcSchemaVersion;
	}
	public String getAuthorization() {
		return authorization;
	}
	public void setAuthorization(String authorization) {
		this.authorization = authorization;
	}
	
	
}
