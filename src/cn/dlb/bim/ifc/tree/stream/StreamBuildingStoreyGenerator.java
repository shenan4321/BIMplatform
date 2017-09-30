package cn.dlb.bim.ifc.tree.stream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EClass;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import cn.dlb.bim.dao.entity.ConcreteRevision;
import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.query.Query;
import cn.dlb.bim.ifc.stream.query.QueryException;
import cn.dlb.bim.ifc.stream.query.multithread.MultiThreadQueryObjectProvider;
import cn.dlb.bim.ifc.tree.BuildingStorey;
import cn.dlb.bim.service.CatalogService;
import cn.dlb.bim.service.VirtualObjectService;

public class StreamBuildingStoreyGenerator {
	private PackageMetaData packageMetaData;
	private CatalogService catalogService;
	private VirtualObjectService virtualObjectService;
	private ConcreteRevision concreteRevision;
	
	private Map<Short, List<VirtualObject>> cidContainer = new HashMap<>();
	private Map<Long, VirtualObject> oidContainer = new HashMap<>();
	
	public StreamBuildingStoreyGenerator(ThreadPoolTaskExecutor executor, PackageMetaData packageMetaData, CatalogService catalogService,
			VirtualObjectService virtualObjectService, ConcreteRevision concreteRevision) {
		this.packageMetaData = packageMetaData;
		this.catalogService = catalogService;
		this.virtualObjectService = virtualObjectService;
		this.concreteRevision = concreteRevision;
		try {
			StreamBuildingStoreyScript streamProjectTreeScript = new StreamBuildingStoreyScript(packageMetaData);
			Query query = streamProjectTreeScript.getQuery();
			MultiThreadQueryObjectProvider objectProvider = new MultiThreadQueryObjectProvider(executor, catalogService, virtualObjectService, query, concreteRevision.getRevisionId(), packageMetaData);
			VirtualObject next = objectProvider.next();
			while (next != null) {
				if (!cidContainer.containsKey(next.getEClassId())) {
					cidContainer.put(next.getEClassId(), new ArrayList<>());
				}
				cidContainer.get(next.getEClassId()).add(next);
				oidContainer.put(next.getOid(), next);
				next = objectProvider.next();
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QueryException e) {
			e.printStackTrace();
		}
	}

	public List<BuildingStorey> proccessBuild() {
		EClass ifcBuildingStoreyEclass = packageMetaData.getEClass("IfcBuildingStorey");
		Integer rid = concreteRevision.getRevisionId();
		Short cid = catalogService.getCidOfEClass(ifcBuildingStoreyEclass);
		List<VirtualObject> list = cidContainer.get(cid);
		List<BuildingStorey> result = new ArrayList<>();
		for (VirtualObject buildingStoreyObject : list) {
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

					VirtualObject containsElementObject = oidContainer.get((Long) containsElement);
					Object relatedElements = containsElementObject.get("RelatedElements");
					if (relatedElements != null) {
						List relatedElementsList = (List) relatedElements;
						for (Object relatedElement : relatedElementsList) {
							VirtualObject relatedElementObject = oidContainer.get((Long) relatedElement);
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
				VirtualObject isDecomposedBy = oidContainer.get((Long) isDecomposedByRef);

				EClass ifcRelAggregatesEclass = packageMetaData.getEClass("IfcRelAggregates");
				if (ifcRelAggregatesEclass.isSuperTypeOf(isDecomposedBy.eClass())) {
					Object relatedObjects = isDecomposedBy.get("RelatedObjects");
					List relatedObjectsList = (List) relatedObjects;
					for (Object relatedObject : relatedObjectsList) {
						VirtualObject relatedVirtualObject = oidContainer.get((Long) relatedObject);
						collectBuildingStorey(relatedVirtualObject, buildingStorey);
						buildingStorey.getOidContains().add(relatedVirtualObject.getOid());
					}
				}
			}

		}
	}

}
