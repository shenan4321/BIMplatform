package cn.dlb.bim.component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.emf.ecore.EClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import cn.dlb.bim.dao.entity.ConcreteRevision;
import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.query.Query;
import cn.dlb.bim.ifc.stream.query.QueryException;
import cn.dlb.bim.ifc.stream.query.QueryPart;
import cn.dlb.bim.ifc.stream.query.multithread.MultiThreadQueryObjectProvider;
import cn.dlb.bim.ifc.tree.BuildingStorey;
import cn.dlb.bim.ifc.tree.BuildingStoreyGenerator;
import cn.dlb.bim.ifc.tree.PropertyGenerator;
import cn.dlb.bim.ifc.tree.PropertySet;
import cn.dlb.bim.ifc.tree.stream.StreamPropertyGenerator;
import cn.dlb.bim.lucene.IfcProductRecordText;
import cn.dlb.bim.lucene.IfcProductRecordTextSearch;
import cn.dlb.bim.service.BimService;
import cn.dlb.bim.service.CatalogService;
import cn.dlb.bim.service.ConcreteRevisionService;
import cn.dlb.bim.service.VirtualObjectService;

@Component("RecordSearchManager")
public class RecordSearchManager {
	
	@Autowired
	private PlatformServer server;
	
	@Autowired
	private VirtualObjectService virtualObjectService;//queryExecutor
	
	@Autowired
	@Qualifier("queryExecutor")
	private ThreadPoolTaskExecutor threadPoolTaskExecutor;
	
	@Autowired
	private CatalogService catalogService;
	
	@Autowired
	private ConcreteRevisionService concreteRevisionService;
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
	
	public List<IfcProductRecordText> search(Integer rid, String keyword) throws IOException, QueryException, DatabaseException, InterruptedException, ExecutionException {
		List<IfcProductRecordText> result = new ArrayList<>();
		ConcreteRevision concreteRevision = concreteRevisionService.findByRid(rid);
		if (!isBuildedIndex(rid)) {
			if (concreteRevision != null) {
				buildIndex(rid, concreteRevision);
			} else {
				return result;
			}
		}
		Path indexPath = getIndexPath(rid);
		IfcProductRecordTextSearch search = new IfcProductRecordTextSearch(indexPath.toFile());
		String[] fields = new String[]{
				IfcProductRecordTextSearch.Key_Oid, 
				IfcProductRecordTextSearch.Key_Type, 
				IfcProductRecordTextSearch.Key_Name, 
				IfcProductRecordTextSearch.Key_Detail};
		try {
			for (String field : fields) {
				result.addAll(search.search(keyword, field, Record_Limit));
			}
		} catch (Exception e) {
			buildIndex(rid, concreteRevision);//重建索引
		}
		
		return result;
	}
	
	public void buildIndex(Integer rid, ConcreteRevision concreteRevision) throws IOException, QueryException, DatabaseException, InterruptedException, ExecutionException {
		Path indexPath = getIndexPath(rid);
		indexPath.toFile().delete();
		indexPath.toFile().mkdirs();
		
		IfcProductRecordTextSearch search = new IfcProductRecordTextSearch(indexPath.toFile());
		
		String schema = concreteRevision.getSchema();
		PackageMetaData packageMetaData = server.getMetaDataManager().getPackageMetaData(schema);
		
		EClass productClass = packageMetaData.getEClass("IfcProduct");
		Query query = new Query(packageMetaData);
		QueryPart queryPart = query.createQueryPart();
		queryPart.addType(productClass, true);
		MultiThreadQueryObjectProvider objectProvider = new MultiThreadQueryObjectProvider(threadPoolTaskExecutor, catalogService, virtualObjectService, query, rid, packageMetaData);
		
		List<IfcProductRecordText> records = new ArrayList<>();
		
		VirtualObject next = objectProvider.next();
		while (next != null) {
			String name = (String) next.get("Name");
			
			StreamPropertyGenerator propertyGenerator = new StreamPropertyGenerator(threadPoolTaskExecutor, packageMetaData, catalogService, virtualObjectService, concreteRevision, next);
			
			List<PropertySet> porpertySetList = propertyGenerator.getProperty();
			
			String detail = "";
			for (PropertySet propertySet : porpertySetList) {
				detail += propertySet.text() + ";";
			}
			
			IfcProductRecordText record = new IfcProductRecordText();
			record.setOid(String.valueOf(next.getOid()));
			record.setName(name);
			record.setType(next.eClass().getName());
			record.setDetail(detail);
			
			records.add(record);
			next = objectProvider.next();
		}
		
		search.createIndex(records);
	}
	
	public Path getIndexPath(Integer rid) {
		Path tempPath = platformServerConfig.getTempDir();
		Path indexPath = tempPath.resolve("index/" + rid + "/");
		return indexPath;
	}
}
