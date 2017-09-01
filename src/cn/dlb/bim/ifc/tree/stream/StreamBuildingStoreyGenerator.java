package cn.dlb.bim.ifc.tree.stream;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.springframework.data.util.CloseableIterator;

import cn.dlb.bim.dao.VirtualObjectDao;
import cn.dlb.bim.dao.entity.ConcreteRevision;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.tree.BuildingStorey;
import cn.dlb.bim.service.PlatformService;

public class StreamBuildingStoreyGenerator {
	private PackageMetaData packageMetaData;
	private PlatformService platformService;
	private VirtualObjectDao virtualObjectDao;
	private ConcreteRevision concreteRevision;

	public StreamBuildingStoreyGenerator(PackageMetaData packageMetaData, PlatformService platformService,
			VirtualObjectDao virtualObjectDao, ConcreteRevision concreteRevision) {
		this.packageMetaData = packageMetaData;
		this.platformService = platformService;
		this.virtualObjectDao = virtualObjectDao;
		this.concreteRevision = concreteRevision;
	}

	public List<BuildingStorey> proccessBuild() {
		EClass ifcBuildingStoreyEclass = packageMetaData.getEClass("IfcBuildingStorey");
		Integer rid = concreteRevision.getRevisionId();
		Short cid = platformService.getCidOfEClass(ifcBuildingStoreyEclass);
		CloseableIterator<VirtualObject> iterator = virtualObjectDao.streamByRidAndCid(rid, cid);
		List<BuildingStorey> result = new ArrayList<>();
		while (iterator.hasNext()) {
			VirtualObject buildingStoreyObject = iterator.next();
			BuildingStorey buildingStorey = new BuildingStorey();
			buildingStorey.setName((String) buildingStoreyObject.get("Name"));
			collectBuildingStorey(buildingStoreyObject, buildingStorey);
			result.add(buildingStorey);
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void collectBuildingStorey(VirtualObject object, BuildingStorey buildingStorey) {
		EClass ifcObjectDefinitionEclass = packageMetaData.getEClass("IfcObjectDefinition");
		if (!ifcObjectDefinitionEclass.isSuperTypeOf(object.eClass())) {
			return;
		}
		Integer rid = concreteRevision.getRevisionId();
		EClass ifcProductEclass = packageMetaData.getEClass("IfcProduct");
		if (ifcProductEclass.isSuperTypeOf(object.eClass())) {
			Object containsElements = object.get("ContainsElements");
			
			if (containsElements != null) {
				List containsElementsList = (List) containsElements;
				for (Object containsElement : containsElementsList) {

					VirtualObject containsElementObject = virtualObjectDao.findOneByRidAndOid(rid,
							(Long) containsElement);
					Object relatedElements = containsElementObject.get("RelatedElements");
					if (relatedElements != null) {
						List relatedElementsList = (List) relatedElements;
						for (Object relatedElement : relatedElementsList) {
							VirtualObject relatedElementObject = virtualObjectDao.findOneByRidAndOid(rid,
									(Long) relatedElement);
							collectBuildingStorey(relatedElementObject, buildingStorey);
							buildingStorey.getOidContains().add(relatedElementObject.getOid());
						}
					}
				}
			}
		}

		Object isDecomposedByObject = object.get("IsDecomposedBy");

		if (isDecomposedByObject != null) {

			List isDecomposedByList = (List) isDecomposedByObject;

			for (Object isDecomposedByRef : isDecomposedByList) {
				VirtualObject isDecomposedBy = virtualObjectDao.findOneByRidAndOid(rid, (Long) isDecomposedByRef);

				EClass ifcRelAggregatesEclass = packageMetaData.getEClass("IfcRelAggregates");
				if (ifcRelAggregatesEclass.isSuperTypeOf(isDecomposedBy.eClass())) {
					Object relatedObjects = isDecomposedBy.get("RelatedObjects");
					List relatedObjectsList = (List) relatedObjects;
					for (Object relatedObject : relatedObjectsList) {
						VirtualObject relatedVirtualObject = virtualObjectDao.findOneByRidAndOid(rid,
								(Long) relatedObject);
						collectBuildingStorey(relatedVirtualObject, buildingStorey);
						buildingStorey.getOidContains().add(relatedVirtualObject.getOid());
					}
				}
			}

		}
	}

}
