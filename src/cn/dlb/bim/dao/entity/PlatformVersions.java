package cn.dlb.bim.dao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "PlatformVersions")
public class PlatformVersions {
	@Id
	private String platformVersionId;
	private int currentTopRevisionId;
	
	public String getPlatformVersionId() {
		return platformVersionId;
	}
	public void setPlatformVersionId(String platformVersionId) {
		this.platformVersionId = platformVersionId;
	}
	public int getCurrentTopRevisionId() {
		return currentTopRevisionId;
	}
	public void setCurrentTopRevisionId(int currentTopRevisionId) {
		this.currentTopRevisionId = currentTopRevisionId;
	}
}
