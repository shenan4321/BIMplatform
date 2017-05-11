package cn.dlb.bim.ifc.tree;

import java.util.List;

public class PropertySetCollection {
	private Long oid;
	private List<PropertySet> propertySetCollection;
	
	public Long getOid() {
		return oid;
	}
	public void setOid(Long oid) {
		this.oid = oid;
	}
	public List<PropertySet> getPropertySetCollection() {
		return propertySetCollection;
	}
	public void setPropertySetCollection(List<PropertySet> propertySetCollection) {
		this.propertySetCollection = propertySetCollection;
	}
	
}
