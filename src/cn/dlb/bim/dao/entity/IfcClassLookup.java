package cn.dlb.bim.dao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "IfcClassLookup")
public class IfcClassLookup {
	
	@Id
	private Short cid;
	private String packageClassName;
	private Long oid;
	
	public Short getCid() {
		return cid;
	}
	public void setCid(Short cid) {
		this.cid = cid;
	}
	public String getPackageClassName() {
		return packageClassName;
	}
	public void setPackageClassName(String packageClassName) {
		this.packageClassName = packageClassName;
	}
	public Long getOid() {
		return oid;
	}
	public void setOid(Long oid) {
		this.oid = oid;
	}
	
}
