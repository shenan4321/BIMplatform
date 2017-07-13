package cn.dlb.bim.component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.tree.BuildingStorey;
import cn.dlb.bim.ifc.tree.BuildingStoreyGenerator;
import cn.dlb.bim.ifc.tree.PropertyGenerator;
import cn.dlb.bim.ifc.tree.PropertySet;
import cn.dlb.bim.lucene.IfcProductRecordText;
import cn.dlb.bim.lucene.IfcProductRecordTextSearch;
import cn.dlb.bim.service.BimService;

@Component("RecordSearchManager")
public class RecordSearchManager {
	
	@Autowired
	@Qualifier("BimServiceImpl")
	private BimService bimService;
	@Autowired
	private PlatformServerConfig platformServerConfig;
	
	public static Integer Record_Limit = Integer.MAX_VALUE;
	
	public Boolean isBuildedIndex(Integer rid) {
		Path indexPath = getIndexPath(rid);
		if (indexPath.toFile().isDirectory()) {
			return true;
		} else {
			return false;
		}
	}
	
	public List<IfcProductRecordText> search(Integer rid, String keyword) {
		List<IfcProductRecordText> result = new ArrayList<>();
		if (!isBuildedIndex(rid)) {
			IfcModelInterface model = bimService.queryModelByRid(rid, null);
			if (model != null) {
				buildIndex(rid, model);
			} else {
				return result;
			}
		}
		Path indexPath = getIndexPath(rid);
		IfcProductRecordTextSearch search = new IfcProductRecordTextSearch(indexPath.toFile());
		String[] fields = new String[]{
				IfcProductRecordTextSearch.Key_Oid, 
				IfcProductRecordTextSearch.Key_Location, 
				IfcProductRecordTextSearch.Key_Type, 
				IfcProductRecordTextSearch.Key_Name, 
				IfcProductRecordTextSearch.Key_Detail};
		
		for (String field : fields) {
			result.addAll(search.search(keyword, field, Record_Limit));
		}
		return result;
	}
	
	public void buildIndex(Integer rid, IfcModelInterface model) {
		Path indexPath = getIndexPath(rid);
		if (!indexPath.toFile().isDirectory()) {
			indexPath.toFile().mkdirs();
		}
		IfcProductRecordTextSearch search = new IfcProductRecordTextSearch(indexPath.toFile());
		
		EClass productClass = (EClass) model.getPackageMetaData().getEClass("IfcProduct");
		List<IdEObject> productList = model.getAllWithSubTypes(productClass);
		
		BuildingStoreyGenerator generator = new BuildingStoreyGenerator(model.getPackageMetaData());
		List<BuildingStorey> buildingStoreys = generator.generateBuildingStorey(model);
		BiMap<String, Long> buildingStoreyMap = HashBiMap.create();
		for (BuildingStorey buildingStorey : buildingStoreys) {
			List<Long> oidList = buildingStorey.getOidContains();
			for (Long oid : oidList) {
				buildingStoreyMap.put(buildingStorey.getName(), oid);
			}
		}
		List<IfcProductRecordText> records = new ArrayList<>();
		for (IdEObject ifcProduct : productList) {
			String name = (String) ifcProduct.eGet(ifcProduct.eClass().getEStructuralFeature("Name"));
			String floorName = buildingStoreyMap.inverse().get(ifcProduct.getOid());
			
			PropertyGenerator propertyGenerator = new PropertyGenerator();
			List<PropertySet> porpertySetList = propertyGenerator.getProperty(model.getPackageMetaData(), ifcProduct);
			
			String detail = "";
			for (PropertySet propertySet : porpertySetList) {
				detail += propertySet.text() + ";";
			}
			
			IfcProductRecordText record = new IfcProductRecordText();
			record.setOid(String.valueOf(ifcProduct.getOid()));
			record.setName(name);
			record.setType(ifcProduct.eClass().getName());
			record.setLocation(floorName);
			record.setDetail(detail);
			
			records.add(record);
		}
		
		search.createIndex(records);
	}
	
	public Path getIndexPath(Integer rid) {
		Path tempPath = platformServerConfig.getTempDir();
		Path indexPath = tempPath.resolve("index/" + rid + "/");
		return indexPath;
	}
}
