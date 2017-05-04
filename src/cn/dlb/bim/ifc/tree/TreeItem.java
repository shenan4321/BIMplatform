package cn.dlb.bim.ifc.tree;

import java.util.ArrayList;
import java.util.List;

public class TreeItem {
	
	private Long oid = -1l;
	private String name;
	private Long geometryOid = -1l;
	private List<TreeItem> decomposition;
	private List<TreeItem> contains;
	private Boolean selected = false;
	private TreeItem parent;
	private String ifcClassType;
	
	public TreeItem() {
		decomposition = new ArrayList<>();
		contains = new ArrayList<>();
	}
	
	public Long getOid() {
		return oid;
	}
	public void setOid(Long oid) {
		this.oid = oid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Long getGeometryOid() {
		return geometryOid;
	}
	public void setGeometryOid(Long geometryOid) {
		this.geometryOid = geometryOid;
	}
	public List<TreeItem> getDecomposition() {
		return decomposition;
	}
	public void setDecomposition(List<TreeItem> decomposition) {
		this.decomposition = decomposition;
	}
	public List<TreeItem> getContains() {
		return contains;
	}
	public void setContains(List<TreeItem> contains) {
		this.contains = contains;
	}
	public Boolean getSelected() {
		return selected;
	}
	public void setSelected(Boolean selected) {
		this.selected = selected;
	}
	public TreeItem getParent() {
		return parent;
	}
	public void setParent(TreeItem parent) {
		this.parent = parent;
	}
	public String getIfcClassType() {
		return ifcClassType;
	}
	public void setIfcClassType(String ifcClassType) {
		this.ifcClassType = ifcClassType;
	}
	
}
