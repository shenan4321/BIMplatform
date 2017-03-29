package cn.dlb.bim.dao.entity;

import java.io.Serializable;

public class IdEObjectEntity implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -57438738805850482L;
	
	private Long oid;
	private byte[] objectBytes;
	
	public Long getOid() {
		return oid;
	}
	public void setOid(Long oid) {
		this.oid = oid;
	}
	public byte[] getObjectBytes() {
		return objectBytes;
	}
	public void setObjectBytes(byte[] objectBytes) {
		this.objectBytes = objectBytes;
	}
	
}
