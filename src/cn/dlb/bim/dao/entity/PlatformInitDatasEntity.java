package cn.dlb.bim.dao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "PlatformInitDatasEntity")
public class PlatformInitDatasEntity {
	@Id
	String platformVersionId;
	int revisionId;
	
	public String getPlatformVersionId() {
		return platformVersionId;
	}
	public void setPlatformVersionId(String platformVersionId) {
		this.platformVersionId = platformVersionId;
	}
	public int getRevisionId() {
		return revisionId;
	}
	public void setRevisionId(int revisionId) {
		this.revisionId = revisionId;
	}
	
}
