package cn.dlb.bim.model;

public class IfcStoreModel {
	private Long gid;
	private byte[] ifcObjectBytes;
	public Long getGid() {
		return gid;
	}
	public void setGid(Long gid) {
		this.gid = gid;
	}
	public byte[] getIfcObjectBytes() {
		return ifcObjectBytes;
	}
	public void setIfcObjectBytes(byte[] ifcObjectBytes) {
		this.ifcObjectBytes = ifcObjectBytes;
	}
	
}
