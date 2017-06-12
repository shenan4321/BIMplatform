package cn.dlb.bim.ifc.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;

import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;

public class BuildingCellGenerator {
	
	public List<BuildingCellContainer> buildBuildingCells(IfcModelInterface ifcModel) {
		EClass productClass = (EClass) ifcModel.getPackageMetaData().getEClass("IfcProduct");
		List<IdEObject> productList = ifcModel.getAllWithSubTypes(productClass);
		Map<String, BuildingCellContainer> containers = new HashMap<>();
		for (IdEObject product : productList) {
			processCell(product, containers);
		}
		List<BuildingCellContainer> result = new ArrayList<>();
		result.addAll(containers.values());
		return result;
	}
	
	private void processCell(IdEObject product, Map<String, BuildingCellContainer> containers) {
		String typeNameWithOutIfc = getTypeNameWithOutIfc(product.eClass().getName());
		if (!containers.containsKey(typeNameWithOutIfc)) {
			BuildingCellContainer cellContainer = new BuildingCellContainer(typeNameWithOutIfc);
			containers.put(typeNameWithOutIfc, cellContainer);
		}
		containers.get(typeNameWithOutIfc).getOids().add(product.getOid());
	}
	
	private String getTypeNameWithOutIfc(String ifcTypeName) {
		return ifcTypeName.replaceFirst("Ifc", "");
	}
}
