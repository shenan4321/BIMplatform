package cn.dlb.bim.ifc.tree;

import java.util.ArrayList;
import java.util.List;

public class ProjectTree {
	private List<TreeItem> treeRoots = new ArrayList<>();

	public List<TreeItem> getTreeRoots() {
		return treeRoots;
	}

	public void setTreeRoots(List<TreeItem> treeRoots) {
		this.treeRoots = treeRoots;
	}
	
}
