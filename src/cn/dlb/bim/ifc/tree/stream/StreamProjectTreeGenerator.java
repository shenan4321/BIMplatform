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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import cn.dlb.bim.dao.entity.ConcreteRevision;
import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.query.Query;
import cn.dlb.bim.ifc.stream.query.QueryException;
import cn.dlb.bim.ifc.stream.query.multithread.MultiThreadQueryObjectProvider;
import cn.dlb.bim.ifc.stream.serializers.ObjectProvider;
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
	
	private Map<Short, List<VirtualObject>> cidContainer = new HashMap<>();
	private Map<Long, VirtualObject> oidContainer = new HashMap<>();
	
	private final ThreadPoolExecutor queryExecutor = new ThreadPoolExecutor(10, 10, 24, TimeUnit.HOURS,
			new ArrayBlockingQueue<Runnable>(10000000));

	public StreamProjectTreeGenerator(PackageMetaData packageMetaData, CatalogService catalogService,
			VirtualObjectService virtualObjectService, ConcreteRevision concreteRevision) {
		this.packageMetaData = packageMetaData;
		this.catalogService = catalogService;
		this.virtualObjectService = virtualObjectService;
		this.concreteRevision = concreteRevision;
		
		try {
			StreamProjectTreeScript streamProjectTreeScript = new StreamProjectTreeScript(packageMetaData);
			Query query = streamProjectTreeScript.getQuery();
			ObjectProvider objectProvider = new MultiThreadQueryObjectProvider(queryExecutor, catalogService, virtualObjectService, query, concreteRevision.getRevisionId(), packageMetaData);
			VirtualObject next = null;
			while ((next = objectProvider.next()) != null) {
				if (cidContainer.containsKey(next.getEClassId())) {
					cidContainer.get(next.getEClassId()).add(next);
				} else {
					cidContainer.put(next.getEClassId(), new ArrayList<>());
					cidContainer.get(next.getEClassId()).add(next);
				}
				oidContainer.putIfAbsent(next.getOid(), next);
			}
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void proccessBuild() {
		EClass projectEClass = (EClass) packageMetaData.getEClassifierCaseInsensitive("IfcProject");
		Short cid = catalogService.getCidOfEClass(projectEClass);
		Integer rid = concreteRevision.getRevisionId();
		List<VirtualObject> projects = cidContainer.get(cid);
		if (projects.isEmpty()) {
			return;
		}
		VirtualObject project = projects.iterator().next();
//		VirtualObject project = virtualObjectService.findOneByRidAndCid(rid, cid);
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
//				VirtualObject geometryInfo = oidContainer.get((Long) refGeoId);
////				VirtualObject geometryInfo = virtualObjectService.findOneByRidAndOid(rid, (Long) refGeoId);
//				if (geometryInfo == null) {
//					System.out.println();
//				}
				curTree.setGeometryOid((Long) refGeoId);
			}

			Object containsElements = object.get("ContainsElements");

			if (containsElements != null) {
				List containsElementsList = (List) containsElements;
				for (Object containsElementObject : containsElementsList) {
					VirtualObject containsElement = oidContainer.get((Long) containsElementObject);
//					VirtualObject containsElement = virtualObjectService.findOneByRidAndOid(rid, (Long) containsElementObject);

					Object relatedElements = containsElement.get("RelatedElements");
					List relatedElementsList = (List) relatedElements;
					for (Object relatedElementObject : relatedElementsList) {
						VirtualObject relatedElement = oidContainer.get((Long) relatedElementObject);
//						VirtualObject relatedElement = virtualObjectService.findOneByRidAndOid(rid, (Long) relatedElementObject);
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
				VirtualObject isDecomposedBy = oidContainer.get((Long) isDecomposedByRef);
//				VirtualObject isDecomposedBy = virtualObjectService.findOneByRidAndOid(rid, (Long)isDecomposedByRef);
				
				EClass ifcRelAggregatesEclass = packageMetaData.getEClass("IfcRelAggregates");
				if (ifcRelAggregatesEclass.isSuperTypeOf(isDecomposedBy.eClass())) {
					Object relatedObjects = isDecomposedBy.get("RelatedObjects");
					List relatedObjectsList = (List) relatedObjects;
					for (Object relatedObject : relatedObjectsList) {
						VirtualObject relatedVirtualObject = oidContainer.get((Long) relatedObject);
//						VirtualObject relatedVirtualObject = virtualObjectService.findOneByRidAndOid(rid, (Long)relatedObject);
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
