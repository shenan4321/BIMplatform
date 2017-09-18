package cn.dlb.bim.ifc.tree.stream;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import cn.dlb.bim.dao.BaseMongoDao;
import cn.dlb.bim.dao.entity.ConcreteRevision;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.tree.PropertySet;
import cn.dlb.bim.service.CatalogService;

public class StreamPropertyGenerator {
	private PackageMetaData packageMetaData;
	private CatalogService platformService;
	private ConcreteRevision concreteRevision;
	private BaseMongoDao<VirtualObject> virtualObjectDao;
	
	public StreamPropertyGenerator(PackageMetaData packageMetaData, CatalogService platformService,
			BaseMongoDao<VirtualObject> virtualObjectDao, ConcreteRevision concreteRevision) {
		this.packageMetaData = packageMetaData;
		this.platformService = platformService;
		this.virtualObjectDao = virtualObjectDao;
		this.concreteRevision = concreteRevision;
	}
	
//	public List<PropertySet> getProperty() {
//		
//		EClass projectEClass = (EClass) packageMetaData.getEClassifierCaseInsensitive("IfcProduct");
//		Short cid = platformService.getCidOfEClass(projectEClass);
//		Query virtualObjectQuery = new Query();
//		Integer rid = concreteRevision.getRevisionId();
//		virtualObjectQuery.addCriteria(Criteria.where("eClassId").is(cid).andOperator(Criteria.where("rid").is(rid)));
//		VirtualObject project = virtualObjectDao.findOne(virtualObjectQuery);
//		tree.getTreeRoots().add(buildTreeCell(project));
//		
//	}
	
}
