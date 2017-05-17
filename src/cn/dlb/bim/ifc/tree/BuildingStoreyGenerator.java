package cn.dlb.bim.ifc.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.models.geometry.GeometryInfo;

public class BuildingStoreyGenerator {
	public final static String KeyWord_IfcBuildingStorey = "IfcBuildingStorey";
	private PackageMetaData packageMetaData;
	
	public BuildingStoreyGenerator(PackageMetaData packageMetaData) {
		this.packageMetaData = packageMetaData;
	}
	
	public List<BuildingStorey> generateBuildingStorey(IfcModelInterface ifcModel) {
		
		EClass productClass = (EClass) packageMetaData.getEClass("IfcBuildingStorey");
		List<IdEObject> projectList = ifcModel.getAllWithSubTypes(productClass);
		List<BuildingStorey> result = new ArrayList<>();
		for (IdEObject ifcBuildingStorey : projectList) {
			BuildingStorey buildingStorey = new BuildingStorey();
			String name = (String) ifcBuildingStorey.eGet(ifcBuildingStorey.eClass().getEStructuralFeature("Name"));
			buildingStorey.setName(name);
			collectBuildingStorey(ifcBuildingStorey, buildingStorey);
			result.add(buildingStorey);
		}
		
		return result;
	}
	
	private void collectBuildingStorey(IdEObject object, BuildingStorey buildingStorey) {
		if (!isInstanceOf(object, "IfcObjectDefinition")) {
			return;
		}
		
		if (isInstanceOf(object, "IfcProduct")) {
			
			EReference containElementsReference = packageMetaData.getEReference(object.eClass().getName(), "ContainsElements");
			
			if (containElementsReference != null) {
				List<IdEObject> ifcRelContainedInSpatialStructureList = (List<IdEObject>) object.eGet(containElementsReference);
				for (IdEObject ifcRelContainedInSpatialStructure : ifcRelContainedInSpatialStructureList) {
					EReference relatedElementsReference = packageMetaData.getEReference(ifcRelContainedInSpatialStructure.eClass().getName(), "RelatedElements");
					if (relatedElementsReference != null) {
						List<IdEObject> subIfcProductList = (List<IdEObject>) ifcRelContainedInSpatialStructure.eGet(relatedElementsReference);
						for (IdEObject subIfcProduct : subIfcProductList) {
							collectBuildingStorey(subIfcProduct, buildingStorey);
							buildingStorey.getOidContains().add(subIfcProduct.getOid());
						}
					}
				}
			}
		}
			
		EReference isDecomposedByReference = packageMetaData.getEReference(object.eClass().getName(), "IsDecomposedBy");
		if (isDecomposedByReference != null) {
			List ifcRelDecomposes = (List) object.eGet(isDecomposedByReference);
			for (Object ifcRelDecompose : ifcRelDecomposes) {
				if (ifcRelDecompose instanceof IdEObject) {
					IdEObject ifcRelAggregates = (IdEObject) ifcRelDecompose;
					if (isInstanceOf(ifcRelAggregates, "IfcRelAggregates")) {
						EReference relatedObjectsReference = packageMetaData.getEReference(ifcRelAggregates.eClass().getName(), "RelatedObjects");
						if (relatedObjectsReference != null) {
							List relatedObjects = (List) ifcRelAggregates.eGet(relatedObjectsReference);
							for (Object relatedObject : relatedObjects) {
								if (relatedObject instanceof IdEObject) {
									IdEObject relatedIdEObject = (IdEObject) relatedObject;
									collectBuildingStorey(relatedIdEObject, buildingStorey);
									buildingStorey.getOidContains().add(relatedIdEObject.getOid());
								}
							}
						}
					}
				}
				
			}
			
		}
				
	}
	
	private Boolean isInstanceOf(IdEObject originObject, String type) {
		EClass eClass = packageMetaData.getEClass(type);
		return eClass.isSuperTypeOf(originObject.eClass());
	}
}
