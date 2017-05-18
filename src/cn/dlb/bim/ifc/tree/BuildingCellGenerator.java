package cn.dlb.bim.ifc.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.dlb.bim.ifc.emf.IfcModelInterface;

public class BuildingCellGenerator {
	
	public List<BuildingCellContainer> buildBuildingCells(IfcModelInterface ifcModel) {
		ProjectTreeGenerator treeGenerator = new ProjectTreeGenerator(ifcModel.getPackageMetaData());
		treeGenerator.buildProjectTree(ifcModel, ProjectTreeGenerator.KeyWord_IfcProject);
		ProjectTree tree = treeGenerator.getTree();
		Map<String, BuildingCellContainer> containers = new HashMap<>();
		for (TreeItem item : tree.getTreeRoots()) {
			processCell(item, containers);
		}
		List<BuildingCellContainer> result = new ArrayList<>();
		result.addAll(containers.values());
		return result;
	}
	
	private void processCell(TreeItem item, Map<String, BuildingCellContainer> containers) {
		if (item.getContains().size() == 0 && item.getDecomposition().size() == 0) {
			String typeNameWithOutIfc = getTypeNameWithOutIfc(item.getIfcClassType());
			if (!containers.containsKey(typeNameWithOutIfc)) {
				BuildingCellContainer cellContainer = new BuildingCellContainer(typeNameWithOutIfc);
				containers.put(typeNameWithOutIfc, cellContainer);
			}
			containers.get(typeNameWithOutIfc).getOids().add(item.getOid());
		} else if (item.getContains().size() > 0) {
			for (TreeItem contain : item.getContains()) {
				processCell(contain, containers);
			}
		} else {//item.getDecomposition().size() > 0
			for (TreeItem decomposition : item.getDecomposition()) {
				processCell(decomposition, containers);
			}
		}
	}
	
	private String getTypeNameWithOutIfc(String ifcTypeName) {
		return ifcTypeName.replaceFirst("Ifc", "");
	}
}
