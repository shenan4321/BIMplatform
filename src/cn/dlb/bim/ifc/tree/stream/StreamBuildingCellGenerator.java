package cn.dlb.bim.ifc.tree.stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.springframework.data.util.CloseableIterator;
import cn.dlb.bim.dao.VirtualObjectDao;
import cn.dlb.bim.dao.entity.ConcreteRevision;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.tree.BuildingCellContainer;
import cn.dlb.bim.service.CatalogService;
import cn.dlb.bim.service.VirtualObjectService;

public class StreamBuildingCellGenerator {
	
	private PackageMetaData packageMetaData;
	private CatalogService catalogService;
	private ConcreteRevision concreteRevision;
	private VirtualObjectService virtualObjectService;
	
	public StreamBuildingCellGenerator(PackageMetaData packageMetaData, CatalogService catalogService,
			VirtualObjectService virtualObjectService, ConcreteRevision concreteRevision) {
		this.packageMetaData = packageMetaData;
		this.catalogService = catalogService;
		this.virtualObjectService = virtualObjectService;
		this.concreteRevision = concreteRevision;
	}
	
	public List<BuildingCellContainer> proccessBuild() {
		EClass ifcProjectEClass = (EClass) packageMetaData.getEClassifierCaseInsensitive("IfcProduct");
		Set<EClass> subClasses = packageMetaData.getAllSubClasses(ifcProjectEClass);
		Integer rid = concreteRevision.getRevisionId();
		Map<String, BuildingCellContainer> containers = new HashMap<>();
		EClass ifcSpaceEClass = packageMetaData.getEClass("IfcSpace");
		for (EClass eClass : subClasses) {
			Short cid = catalogService.getCidOfEClass(eClass);
			CloseableIterator<VirtualObject> productIterator = virtualObjectService.streamByRidAndCid(rid, cid);
			while (productIterator.hasNext()) {
				VirtualObject product = productIterator.next();
				Object geometryInfo = product.get("geometry");
				if (geometryInfo != null && !ifcSpaceEClass.isSuperTypeOf(product.eClass())) {
					processCell(product, containers);
				}
			}
		}
		List<BuildingCellContainer> result = new ArrayList<>();
		result.addAll(containers.values());
		return result;
	}
	
	private void processCell(VirtualObject product, Map<String, BuildingCellContainer> containers) {
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
