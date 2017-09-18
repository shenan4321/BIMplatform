package cn.dlb.bim.ifc.tree.stream;

import java.util.List;

import org.eclipse.emf.ecore.EClass;

import cn.dlb.bim.dao.VirtualObjectDao;
import cn.dlb.bim.dao.entity.ConcreteRevision;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.tree.ProjectTree;
import cn.dlb.bim.ifc.tree.TreeItem;
import cn.dlb.bim.service.CatalogService;
import cn.dlb.bim.service.VirtualObjectService;

public class StreamProjectTreeGenerator {
	private PackageMetaData packageMetaData;
	private CatalogService catalogService;
	private VirtualObjectService virtualObjectService;
	private ConcreteRevision concreteRevision;
	private ProjectTree tree = new ProjectTree();

	public StreamProjectTreeGenerator(PackageMetaData packageMetaData, CatalogService catalogService,
			VirtualObjectService virtualObjectService, ConcreteRevision concreteRevision) {
		this.packageMetaData = packageMetaData;
		this.catalogService = catalogService;
		this.virtualObjectService = virtualObjectService;
		this.concreteRevision = concreteRevision;
	}

	public void proccessBuild() {
		EClass projectEClass = (EClass) packageMetaData.getEClassifierCaseInsensitive("IfcProject");
		Short cid = catalogService.getCidOfEClass(projectEClass);
		Integer rid = concreteRevision.getRevisionId();
		VirtualObject project = virtualObjectService.findOneByRidAndCid(rid, cid);
		tree.getTreeRoots().add(buildTreeCell(project));
	}

	@SuppressWarnings("rawtypes")
	private TreeItem buildTreeCell(VirtualObject object) {
		Integer rid = concreteRevision.getRevisionId();
		EClass ifcObjectDefinitionEclass = packageMetaData.getEClass("IfcObjectDefinition");
		if (!ifcObjectDefinitionEclass.isSuperTypeOf(object.eClass())) {
			return null;
		}
		TreeItem curTree = new TreeItem();
		String objectName = (String) object.get("Name");
		curTree.setName(objectName);
		curTree.setOid(object.getOid());
		curTree.setIfcClassType(object.eClass().getName());
		curTree.setSelected(true);

		EClass ifcProductEclass = packageMetaData.getEClass("IfcProduct");
		if (ifcProductEclass.isSuperTypeOf(object.eClass())) {
			Object refGeoId = object.get("geometry");
			if (refGeoId != null) {
				VirtualObject geometryInfo = virtualObjectService.findOneByRidAndOid(rid, (Long) refGeoId);
				curTree.setGeometryOid(geometryInfo.getOid());
			}

			Object containsElements = object.get("ContainsElements");

			if (containsElements != null) {
				List containsElementsList = (List) containsElements;
				for (Object containsElementObject : containsElementsList) {
					VirtualObject containsElement = virtualObjectService.findOneByRidAndOid(rid, (Long) containsElementObject);

					Object relatedElements = containsElement.get("RelatedElements");
					List relatedElementsList = (List) relatedElements;
					for (Object relatedElementObject : relatedElementsList) {
						VirtualObject relatedElement = virtualObjectService.findOneByRidAndOid(rid, (Long) relatedElementObject);
						TreeItem subTree = buildTreeCell(relatedElement);
						curTree.getContains().add(subTree);
					}
				}
			}

		}
		
		Object isDecomposedByObject = object.get("IsDecomposedBy");
		
		if (isDecomposedByObject != null) {
			
			List isDecomposedByList = (List) isDecomposedByObject;
			
			for (Object isDecomposedByRef : isDecomposedByList) {
				VirtualObject isDecomposedBy = virtualObjectService.findOneByRidAndOid(rid, (Long)isDecomposedByRef);
				
				EClass ifcRelAggregatesEclass = packageMetaData.getEClass("IfcRelAggregates");
				if (ifcRelAggregatesEclass.isSuperTypeOf(isDecomposedBy.eClass())) {
					Object relatedObjects = isDecomposedBy.get("RelatedObjects");
					List relatedObjectsList = (List) relatedObjects;
					for (Object relatedObject : relatedObjectsList) {
						VirtualObject relatedVirtualObject = virtualObjectService.findOneByRidAndOid(rid, (Long)relatedObject);
						TreeItem subTree = buildTreeCell(relatedVirtualObject);
						curTree.getDecomposition().add(subTree);
					}
				}
			}
			
		}
		
		return curTree;
	}
	
	public ProjectTree getTree() {
		return tree;
	}
}
