package cn.dlb.bim.ifc.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;

import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.models.geometry.GeometryInfo;

public class BuildingCellGenerator {
	
	public List<BuildingCellContainer> buildBuildingCells(IfcModelInterface ifcModel) {
		PackageMetaData packageMetaData = ifcModel.getPackageMetaData();
		EClass productClass = (EClass) packageMetaData.getEClass("IfcProduct");
		List<IdEObject> productList = ifcModel.getAllWithSubTypes(productClass);
		Map<String, BuildingCellContainer> containers = new HashMap<>();
		for (IdEObject product : productList) {
			GeometryInfo geometryInfo = (GeometryInfo) product.eGet(product.eClass().getEStructuralFeature("geometry"));
			if (geometryInfo != null && geometryInfo.getTransformation() != null && !packageMetaData.getEClass("IfcSpace").isSuperTypeOf(product.eClass())) { 
				processCell(product, containers);
			}
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
