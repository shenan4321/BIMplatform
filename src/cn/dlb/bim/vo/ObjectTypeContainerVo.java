package cn.dlb.bim.vo;

import java.util.HashSet;
import java.util.Set;

public class ObjectTypeContainerVo {
	private String objectType;
	private Boolean selected;
	private Set<Long> oids;
	public ObjectTypeContainerVo() {
		oids = new HashSet<>();
	}
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public Boolean getSelected() {
		return selected;
	}
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}
	public Set<Long> getOids() {
		return oids;
	}
	public void setOids(Set<Long> oids) {
		this.oids = oids;
	}
}
