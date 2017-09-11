package cn.dlb.bim.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Service;

import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.dao.BaseMongoDao;
import cn.dlb.bim.dao.VirtualObjectDao;
import cn.dlb.bim.dao.entity.ConcreteRevision;
import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.deserializers.DeserializeException;
import cn.dlb.bim.ifc.deserializers.StepParser;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.Schema;
import cn.dlb.bim.ifc.engine.cells.GenerateGeometryResult;
import cn.dlb.bim.ifc.engine.cells.Vector3d;
import cn.dlb.bim.ifc.shared.ProgressReporter;
import cn.dlb.bim.ifc.stream.GeometryGeneratingException;
import cn.dlb.bim.ifc.stream.StreamingGeometryGenerator;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.deserializers.IfcStepStreamingDeserializer;
import cn.dlb.bim.ifc.stream.query.QueryContext;
import cn.dlb.bim.ifc.tree.BuildingCellContainer;
import cn.dlb.bim.ifc.tree.BuildingStorey;
import cn.dlb.bim.ifc.tree.ProjectTree;
import cn.dlb.bim.ifc.tree.PropertySet;
import cn.dlb.bim.ifc.tree.stream.StreamBuildingCellGenerator;
import cn.dlb.bim.ifc.tree.stream.StreamBuildingStoreyGenerator;
import cn.dlb.bim.ifc.tree.stream.StreamProjectTreeGenerator;
import cn.dlb.bim.service.BimService;
import cn.dlb.bim.service.PlatformService;
import cn.dlb.bim.vo.GeometryInfoVo;
import cn.dlb.bim.vo.GlbVo;
import cn.dlb.bim.vo.ModelAndOutputTemplateVo;
import cn.dlb.bim.vo.ModelInfoVo;
import cn.dlb.bim.vo.ModelLabelVo;
import cn.dlb.bim.vo.OutputTemplateVo;
import cn.dlb.bim.vo.Vector3f;

@Service("StreamBimServiceImpl")
public class StreamBimServiceImpl implements BimService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StreamBimServiceImpl.class);

	private static String IFC2X3_SCHEMA_SHORT = "IFC2X3";
	private static String IFC4_SCHEMA_SHORT = "IFC4";

	@Autowired
	@Qualifier("PlatformServer")
	private PlatformServer server;
	
	@Autowired
	@Qualifier("VirtualObjectDaoImpl")
	private VirtualObjectDao virtualObjectDao;
	
	@Autowired
	@Qualifier("ConcreteRevisionDaoImpl")
	private BaseMongoDao<ConcreteRevision> concreteRevisionDao;

	@Autowired
	@Qualifier("PlatformServiceImpl")
	private PlatformService platformService;

	@Override
	public List<GeometryInfoVo> queryGeometryInfo(Integer rid, ProgressReporter progressReporter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer addRevision(ModelInfoVo modelInfo, File modelFile) {
		Integer rid = -1;
		try {
			ConcreteRevision concreteRevision = new ConcreteRevision();
			concreteRevision.setApplyType(modelInfo.getApplyType());
			concreteRevision.setDate(modelInfo.getUploadDate());
			concreteRevision.setFileName(modelInfo.getFileName());
			concreteRevision.setFileSize(modelInfo.getFileSize());
			concreteRevision.setName(modelInfo.getName());
			concreteRevision.setPid(modelInfo.getPid());
			
			IfcStepStreamingDeserializer deserializer = new IfcStepStreamingDeserializer();
			Schema schema = preReadSchema(modelFile);
			
			concreteRevision.setSchema(schema.getEPackageName());

			Long start = System.nanoTime();
			LOGGER.info("Deserialising ifc file...");
			
			PackageMetaData packageMetaData = server.getMetaDataManager().getPackageMetaData(schema.getEPackageName());
			deserializer.init(packageMetaData, platformService);
			deserializer.read(modelFile);
			
			Long end = System.nanoTime();
			LOGGER.info("Deserialise spend time: " + ((end - start) / 1000000) + "ms.");
			
			
			start = System.nanoTime();
			LOGGER.info("Start fix inverses...");
			rid = deserializer.getRid();
			fixInverses(packageMetaData, rid);
			end = System.nanoTime();
			LOGGER.info("Fix inverses spend time: " + ((end - start) / 1000000) + "ms.");
			
			concreteRevision.setRevisionId(rid);
			
			start = System.nanoTime();
			LOGGER.info("Generate geometry...");
			
			StreamingGeometryGenerator generator = new StreamingGeometryGenerator(server, platformService, rid, deserializer.getIfcHeader());
			QueryContext queryContext = new QueryContext(platformService, packageMetaData, rid);
			GenerateGeometryResult result = generator.generateGeometry(queryContext);
			
			end = System.nanoTime();
			LOGGER.info("Generate spend time: " + ((end - start) / 1000000) + "ms.");
			
			Double maxX = result.getMaxBoundsAsVector3f().getX();
			Double maxY = result.getMaxBoundsAsVector3f().getY();
			Double maxZ = result.getMaxBoundsAsVector3f().getZ();
			Double minX = result.getMinBoundsAsVector3f().getX();
			Double minY = result.getMinBoundsAsVector3f().getY();
			Double minZ = result.getMinBoundsAsVector3f().getZ();
			Vector3f maxBound = new Vector3f(maxX, maxY, maxZ);
			Vector3f minBound = new Vector3f(minX, minY, minZ);
			
			concreteRevision.setMaxBounds(maxBound);
			concreteRevision.setMinBounds(minBound);
			concreteRevision.setIfcHeader(deserializer.getIfcHeader());
			
			concreteRevisionDao.save(concreteRevision);
			
			LOGGER.info("Add revision over.");
			
		} catch (GeometryGeneratingException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DeserializeException e) {
			e.printStackTrace();
		}

		return rid;
	}

	@Override
	public GlbVo queryGlbByRid(Integer rid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector3d queryGlbLonlatByRid(Integer rid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IfcModelInterface queryModelByRid(Integer rid, ProgressReporter progressReporter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ModelInfoVo> queryModelInfoByPid(Long pid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("pid").is(pid));
		List<ConcreteRevision> concreteRevisionList = concreteRevisionDao.find(query);
		
		List<ModelInfoVo> result = new ArrayList<>();
		for (ConcreteRevision concreteRevision : concreteRevisionList) {
			ModelInfoVo modelInfo = new ModelInfoVo();
			modelInfo.setName(concreteRevision.getName());
			modelInfo.setPid(concreteRevision.getPid());
			modelInfo.setApplyType(concreteRevision.getApplyType());
			modelInfo.setRid(concreteRevision.getRevisionId());
			modelInfo.setFileName(concreteRevision.getFileName());
			modelInfo.setFileSize(concreteRevision.getFileSize());
			modelInfo.setUploadDate(concreteRevision.getDate());
			result.add(modelInfo);
		}
		return result;
	}

	@Override
	public void deleteModel(Integer rid) {
		// TODO Auto-generated method stub

	}

	@Override
	public ModelInfoVo queryModelInfoByRid(Integer rid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("revisionId").is(rid));
		ConcreteRevision concreteRevision = concreteRevisionDao.findOne(query);
		ModelInfoVo modelInfo = new ModelInfoVo();
		modelInfo.setName(concreteRevision.getName());
		modelInfo.setPid(concreteRevision.getPid());
		modelInfo.setApplyType(concreteRevision.getApplyType());
		modelInfo.setRid(concreteRevision.getRevisionId());
		modelInfo.setFileName(concreteRevision.getFileName());
		modelInfo.setFileSize(concreteRevision.getFileSize());
		modelInfo.setUploadDate(concreteRevision.getDate());
		return modelInfo;
	}

	@Override
	public void setGlbLonlat(Integer rid, Double lon, Double lat) {
		// TODO Auto-generated method stub

	}

	@Override
	public void insertModelLabel(ModelLabelVo modelLabel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteModelLabel(Integer labelId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void modifyModelLabel(ModelLabelVo modelLabel) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ModelLabelVo> queryAllModelLabelByRid(Integer rid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProjectTree queryModelTree(Integer rid) {
		
		Query concreteRevisionQuery = new Query();
		concreteRevisionQuery.addCriteria(Criteria.where("revisionId").is(rid));
		ConcreteRevision concreteRevision = concreteRevisionDao.findOne(concreteRevisionQuery);
		String schema = concreteRevision.getSchema();
		PackageMetaData packageMetaData = server.getMetaDataManager().getPackageMetaData(schema);
		
		StreamProjectTreeGenerator generator = new StreamProjectTreeGenerator(packageMetaData, platformService, virtualObjectDao, concreteRevision);
		generator.proccessBuild();
		return generator.getTree();
	}

	@Override
	public List<BuildingStorey> queryModelBuildingStorey(Integer rid) {
		Query concreteRevisionQuery = new Query();
		concreteRevisionQuery.addCriteria(Criteria.where("revisionId").is(rid));
		ConcreteRevision concreteRevision = concreteRevisionDao.findOne(concreteRevisionQuery);
		String schema = concreteRevision.getSchema();
		PackageMetaData packageMetaData = server.getMetaDataManager().getPackageMetaData(schema);
		StreamBuildingStoreyGenerator generator = new StreamBuildingStoreyGenerator(packageMetaData, platformService, virtualObjectDao, concreteRevision);
		return generator.proccessBuild();
	}

	@Override
	public List<BuildingCellContainer> queryBuildingCells(Integer rid) {
		Query concreteRevisionQuery = new Query();
		concreteRevisionQuery.addCriteria(Criteria.where("revisionId").is(rid));
		ConcreteRevision concreteRevision = concreteRevisionDao.findOne(concreteRevisionQuery);
		String schema = concreteRevision.getSchema();
		PackageMetaData packageMetaData = server.getMetaDataManager().getPackageMetaData(schema);
		StreamBuildingCellGenerator generator = new StreamBuildingCellGenerator(packageMetaData, platformService, virtualObjectDao, concreteRevision);
		return generator.proccessBuild();
	}

	@Override
	public List<PropertySet> queryProperty(Integer rid, Long oid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long convertIfcToGlbOffline(File modelFile) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GlbVo queryGlbByGlbId(Long glbId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long insertOutputTemplate(Integer rid, OutputTemplateVo template) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteOutputTemplate(Long otid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void modifyOutputTemplate(Integer rid, OutputTemplateVo template) {
		// TODO Auto-generated method stub

	}

	@Override
	public OutputTemplateVo queryOutputTemplate(Integer rid, Long otid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputTemplateVo genModelDefaultOutputTemplate(Integer rid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteModelAndOutputTemplateMap(Integer rid, Long otid) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<ModelAndOutputTemplateVo> queryModelAndOutputTemplateByRid(Integer rid) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("resource")
	private Schema preReadSchema(File file) throws IOException, DeserializeException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		Schema result = null;
		while ((line = br.readLine()) != null) {
			if (line.startsWith("ENDSEC;")) {
				break;
			} else if (line.startsWith("FILE_SCHEMA")) {
				String fileschema = line.substring("FILE_SCHEMA".length()).trim();
				String innerLine = fileschema.substring(1, fileschema.length() - 2);
				innerLine = innerLine.replace("\r\n", "");
				StepParser stepParser = new StepParser(innerLine);
				String schemaVersion = stepParser.readNextString();
				if (schemaVersion.startsWith(IFC2X3_SCHEMA_SHORT)) {
					result = Schema.IFC2X3TC1;
				} else if (schemaVersion.startsWith(IFC4_SCHEMA_SHORT)) {
					result = Schema.IFC4;
				}
			}
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private void fixInverses(PackageMetaData packageMetaData, Integer rid) throws DatabaseException {
		
		Map<Long, VirtualObject> cache = new HashMap<Long, VirtualObject>();

		CloseableIterator<VirtualObject> objectIterator = platformService.streamVirtualObjectByRid(rid);

		while (objectIterator.hasNext()) {
			VirtualObject next = objectIterator.next();
			EClass eclass = platformService.getEClassForCid(next.getEClassId());
			if (packageMetaData.hasInverses(eclass)) {
				for (EReference eReference : packageMetaData.getAllHasInverseReferences(eclass)) {
					Object reference = next.eGet(eReference);
					if (reference != null) {
						if (eReference.isMany()) {
							List<Long> references = (List<Long>) reference;
							for (Long refOid : references) {
								fixInverses(packageMetaData, rid, cache, next, eReference, refOid);
							}
						} else {
							fixInverses(packageMetaData, rid, cache, next, eReference, (Long) reference);
						}
					}
				}
			}
		}
		for (VirtualObject referencedObject : cache.values()) {
			platformService.updateBatch(referencedObject);
		}
		platformService.commitAllBatch();
	}

	private void fixInverses(PackageMetaData packageMetaData, Integer rid, Map<Long, VirtualObject> cache,
			VirtualObject next, EReference eReference, long refOid) throws DatabaseException {
		VirtualObject referencedObject = cache.get(refOid);
		if (referencedObject == null) {
			referencedObject = platformService.queryVirtualObject(rid, refOid);
			if (referencedObject == null) {
				EClass eclass = platformService.getEClassForCid(next.getEClassId());
				throw new DatabaseException("Referenced object with oid " + refOid + ", referenced from "
						+ eclass.getName() + " not found");
			}
			cache.put(refOid, referencedObject);
		}
		EClass referencedObjectEclass = platformService.getEClassForCid(referencedObject.getEClassId());
		EReference oppositeReference = packageMetaData.getInverseOrOpposite(referencedObjectEclass, eReference);
		if (oppositeReference == null) {
			if (eReference.getName().equals("RelatedElements") && referencedObjectEclass.getName().equals("IfcSpace")) {
				// Ignore, IfcSpace should have a field called RelatedElements, but it doesn't.
			} else {
				// LOGGER.error("No opposite " + eReference.getName() + " found");
			}
		} else {
			if (oppositeReference.isMany()) {
				Object existingList = referencedObject.eGet(oppositeReference);
				if (existingList != null) {
					int currentSize = ((List<?>) existingList).size();
					referencedObject.setListItemReference(oppositeReference, currentSize, next.getOid());
				} else {
					referencedObject.setListItemReference(oppositeReference, 0, next.getOid());
				}
			} else {
				referencedObject.setReference(oppositeReference, next.getOid());
			}
		}
	}

	@Override
	public void test() {
		// TODO Auto-generated method stub

	}

}
