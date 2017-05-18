package cn.dlb.bim.ifc.tree;

import java.util.ArrayList;
import java.util.List;

public class BuildingCellContainer {
	private String name;
	private List<Long> oids = new ArrayList<>();
	
	public BuildingCellContainer(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Long> getOids() {
		return oids;
	}
	public void setOids(List<Long> oids) {
		this.oids = oids;
	}
	
}
