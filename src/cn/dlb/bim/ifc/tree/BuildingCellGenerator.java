package cn.dlb.bim.ifc.tree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cn.dlb.bim.ifc.emf.IfcModelInterface;

public class BuildingCellGenerator {
	
	public Collection<BuildingCellContainer> buildBuildingCells(IfcModelInterface ifcModel) {
		ProjectTreeGenerator treeGenerator = new ProjectTreeGenerator(ifcModel.getPackageMetaData());
		treeGenerator.buildProjectTree(ifcModel, ProjectTreeGenerator.KeyWord_IfcProject);
		ProjectTree tree = treeGenerator.getTree();
		Map<String, BuildingCellContainer> result = new HashMap<>();
		for (TreeItem item : tree.getTreeRoots()) {
			processCell(item, result);
		}
		return result.values();
	}
	
	private void processCell(TreeItem item, Map<String, BuildingCellContainer> result) {
		if (item.getContains().size() == 0 && item.getDecomposition().size() == 0) {
			String typeNameWithOutIfc = getTypeNameWithOutIfc(item.getIfcClassType());
			if (!result.containsKey(typeNameWithOutIfc)) {
				BuildingCellContainer cellContainer = new BuildingCellContainer();
				cellContainer.setName(typeNameWithOutIfc);
				result.put(typeNameWithOutIfc, new BuildingCellContainer());
			}
			result.get(typeNameWithOutIfc).getOids().add(item.getOid());
		} else if (item.getContains().size() > 0) {
			for (TreeItem contain : item.getContains()) {
				processCell(contain, result);
			}
		} else {//item.getDecomposition().size() > 0
			for (TreeItem decomposition : item.getDecomposition()) {
				processCell(decomposition, result);
			}
		}
	}
	
	private String getTypeNameWithOutIfc(String ifcTypeName) {
		return ifcTypeName.substring(ifcTypeName.indexOf("Ifc"));
	}
}
