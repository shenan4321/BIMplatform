package cn.dlb.bim.ifc.tree;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.models.geometry.GeometryInfo;

public class ProjectTreeGenerator {
	
	public final static String KeyWord_IfcProject = "IfcProject";
	public final static String KeyWord_IfcBuildingStorey = "IfcBuildingStorey";
	
	private PackageMetaData packageMetaData;
	private ProjectTree tree = new ProjectTree();
	
	public ProjectTreeGenerator(PackageMetaData packageMetaData) {
		this.packageMetaData = packageMetaData;
	}
	
	public void buildProjectTree(IfcModelInterface ifcModel, String keyword) {
		EClass productClass = (EClass) packageMetaData.getEClassifierCaseInsensitive(keyword);
		List<IdEObject> projectList = ifcModel.getAllWithSubTypes(productClass);
		
		for (IdEObject ifcProject : projectList) {
			TreeItem root = buildTree(ifcProject);
			tree.getTreeRoots().add(root);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TreeItem buildTree(IdEObject object) {
		if (!isInstanceOf(object.eClass(), "IfcObjectDefinition")) {
			return null;
		}
		TreeItem curTree = new TreeItem();
		String objectName = (String) object.eGet(object.eClass().getEStructuralFeature("Name"));
		curTree.setName(objectName);
		curTree.setOid(object.getOid());
		curTree.setIfcClassType(object.eClass().getName());
		curTree.setSelected(true);
		
		if (isInstanceOf(object.eClass(), "IfcProduct")) {
			EClass productClass = packageMetaData.getEClass("IfcProduct");
			GeometryInfo geometryInfo = (GeometryInfo) object.eGet(productClass.getEStructuralFeature("geometry"));
			if (geometryInfo != null) {
				curTree.setGeometryOid(geometryInfo.getOid());
			}
			
			EReference containElementsReference = packageMetaData.getEReference(object.eClass().getName(), "ContainsElements");
			
			if (containElementsReference != null) {
				List<IdEObject> ifcRelContainedInSpatialStructureList = (List<IdEObject>) object.eGet(containElementsReference.getEOpposite());
				for (IdEObject ifcRelContainedInSpatialStructure : ifcRelContainedInSpatialStructureList) {
					EReference relatedElementsReference = packageMetaData.getEReference(ifcRelContainedInSpatialStructure.eClass().getName(), "RelatedElements");
					List<IdEObject> subIfcProductList = (List<IdEObject>) ifcRelContainedInSpatialStructure.eGet(relatedElementsReference.getEOpposite());
					for (IdEObject subIfcProduct : subIfcProductList) {
						TreeItem subTree = buildTree(subIfcProduct);
						curTree.getContains().add(subTree);
						subTree.setParent(curTree);
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
					if (isInstanceOf(ifcRelAggregates.eClass(), "IfcRelAggregates")) {
						EReference relatedObjectsReference = packageMetaData.getEReference(ifcRelAggregates.eClass().getName(), "RelatedObjects");
						List relatedObjects = (List) ifcRelAggregates.eGet(relatedObjectsReference);
						for (Object relatedObject : relatedObjects) {
							if (relatedObject instanceof IdEObject) {
								IdEObject relatedIdEObject = (IdEObject) relatedObject;
								TreeItem subTree = buildTree(relatedIdEObject);
								curTree.getDecomposition().add(subTree);
								subTree.setParent(curTree);
							}
						}
					}
				}
				
			}
			
		}
				
		return curTree;
	}
	
	private Boolean isInstanceOf(EClass originClass, String type) {
		EClass eClass = packageMetaData.getEClass(type);
		return eClass.isSuperTypeOf(originClass);
	}
	
	public ProjectTree getTree() {
		return tree;
	}
}
