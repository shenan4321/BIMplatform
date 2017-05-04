package cn.dlb.bim.ifc.tree;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;

import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.models.geometry.GeometryInfo;
import cn.dlb.bim.models.ifc2x3tc1.IfcObjectDefinition;
import cn.dlb.bim.models.ifc2x3tc1.IfcProduct;
import cn.dlb.bim.models.ifc2x3tc1.IfcProject;
import cn.dlb.bim.models.ifc2x3tc1.IfcRelAggregates;
import cn.dlb.bim.models.ifc2x3tc1.IfcRelContainedInSpatialStructure;
import cn.dlb.bim.models.ifc2x3tc1.IfcRelDecomposes;
import cn.dlb.bim.models.ifc2x3tc1.IfcSpatialStructureElement;

public class ProjectTree {
	
	private List<TreeItem> treeRoots = new ArrayList<>();
	
	public void buildProjectTree(IfcModelInterface ifcModel) {
		List<IfcProject> projectList = ifcModel.getAllWithSubTypes(IfcProject.class);
		PackageMetaData packageMetaData = ifcModel.getPackageMetaData();
		for (IfcProject ifcProject : projectList) {
			TreeItem root = buildTree(ifcProject, packageMetaData);
			treeRoots.add(root);
		}
	}
	
	public TreeItem buildTree(IfcObjectDefinition object, PackageMetaData packageMetaData) {
		TreeItem curTree = new TreeItem();
		String objectName = object.getName();
		curTree.setName(objectName);
		curTree.setOid(object.getOid());
		EClass eclass = object.eClass();
		curTree.setIfcClassType(eclass.getName());
		curTree.setSelected(true);
		if (isInstanceOf(packageMetaData, object.eClass(), "IfcProduct")) {
			IfcProduct product = (IfcProduct) object;
			GeometryInfo geometryInfo = product.getGeometry();
			if (geometryInfo != null) {
				curTree.setGeometryOid(geometryInfo.getOid());
			}
			if (isInstanceOf(packageMetaData, product.eClass(), "IfcSpatialStructureElement")) {
				IfcSpatialStructureElement ifcElement = (IfcSpatialStructureElement) product; //TODO IFC4
				EList<IfcRelContainedInSpatialStructure> ifcRelContainedInSpatialStructureList = ifcElement.getContainsElements();
				for (IfcRelContainedInSpatialStructure ifcRelContainedInSpatialStructure : ifcRelContainedInSpatialStructureList) {
					EList<IfcProduct> ifcProductList = ifcRelContainedInSpatialStructure.getRelatedElements();
					for (IfcProduct ifcProduct : ifcProductList) {
						TreeItem subTree = buildTree(ifcProduct, packageMetaData);
						curTree.getContains().add(subTree);
						subTree.setParent(curTree);
					}
				}
			} 
		}
		EList<IfcRelDecomposes> isDecomposedBy = object.getIsDecomposedBy();
		for (IfcRelDecomposes ifcRelDecomposes : isDecomposedBy) {
			if (isInstanceOf(packageMetaData, ifcRelDecomposes.eClass(), "IfcRelAggregates")) {
				IfcRelAggregates ifcRelAggregates = (IfcRelAggregates) ifcRelDecomposes;
				EList<IfcObjectDefinition> ifcObjectDefinitionList = ifcRelAggregates.getRelatedObjects();
				for (IfcObjectDefinition ifcObjectDefinition : ifcObjectDefinitionList) {
					TreeItem subTree = buildTree(ifcObjectDefinition, packageMetaData);
					curTree.getDecomposition().add(subTree);
					subTree.setParent(curTree);
				}
			}
		}
		return curTree;
	}
	
	private Boolean isInstanceOf(PackageMetaData packageMetaData, EClass originClass, String type) {
		EClass eClass = (EClass) packageMetaData.getEClassifierCaseInsensitive(type);
		return eClass.isSuperTypeOf(originClass);
	}
	
}
