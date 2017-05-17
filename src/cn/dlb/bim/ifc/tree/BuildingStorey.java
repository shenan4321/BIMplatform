package cn.dlb.bim.ifc.tree;

import java.util.ArrayList;
import java.util.List;

public class BuildingStorey {
	private String name;
	private List<Long> oidContains = new ArrayList<>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Long> getOidContains() {
		return oidContains;
	}
	public void setOidContains(List<Long> oidContains) {
		this.oidContains = oidContains;
	}
	
}
