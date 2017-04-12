package cn.dlb.bim.dao.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "IdEObjectEntity")
public class IdEObjectEntity {
	
	@Id
	private Long oid;
	@Indexed
	private Integer rid;
	private byte[] objectBytes;
	
	public Long getOid() {
		return oid;
	}
	public void setOid(Long oid) {
		this.oid = oid;
	}
	public Integer getRid() {
		return rid;
	}
	public void setRid(Integer rid) {
		this.rid = rid;
	}
	public byte[] getObjectBytes() {
		return objectBytes;
	}
	public void setObjectBytes(byte[] objectBytes) {
		this.objectBytes = objectBytes;
	}
	
}
