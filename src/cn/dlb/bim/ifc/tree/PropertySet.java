package cn.dlb.bim.ifc.tree;

import java.util.ArrayList;
import java.util.List;

public class PropertySet {
	
	private Long oid;
	private String name;
	private List<Propertry> propertiySet = new ArrayList<>();
	
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
	public List<Propertry> getPropertiySet() {
		return propertiySet;
	}
	public void setPropertiySet(List<Propertry> propertiySet) {
		this.propertiySet = propertiySet;
	}
	
	public String text() {
		String text = "";
		for (Propertry propertry : propertiySet) {
			String propertryText = propertry.text();
			text += propertryText + ";";
		}
		return text;
	}
}
