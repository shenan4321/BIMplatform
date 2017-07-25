package cn.dlb.bim.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Service;

import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.dao.IfcModelDao;
import cn.dlb.bim.dao.OutputTemplateDao;
import cn.dlb.bim.ifc.database.DatabaseException;
import cn.dlb.bim.ifc.deserializers.DeserializeException;
import cn.dlb.bim.ifc.deserializers.StepParser;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.Schema;
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
import cn.dlb.bim.service.BimService;
import cn.dlb.bim.service.PlatformService;
import cn.dlb.bim.vo.GeometryInfoVo;
import cn.dlb.bim.vo.GlbVo;
import cn.dlb.bim.vo.ModelAndOutputTemplateVo;
import cn.dlb.bim.vo.ModelInfoVo;
import cn.dlb.bim.vo.ModelLabelVo;
import cn.dlb.bim.vo.OutputTemplateVo;

@Service("StreamBimServiceImpl")
public class StreamBimServiceImpl implements BimService {

	private static String IFC2X3_SCHEMA_SHORT = "IFC2X3";
	private static String IFC4_SCHEMA_SHORT = "IFC4";

	@Autowired
	@Qualifier("PlatformServer")
	private PlatformServer server;

	@Autowired
	@Qualifier("IfcModelDaoImpl")
	private IfcModelDao ifcModelDao;

	@Autowired
	@Qualifier("OutputTemplateDaoImpl")
	private OutputTemplateDao outputTemplateDao;

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
			IfcStepStreamingDeserializer deserializer = new IfcStepStreamingDeserializer();
			Schema schema = preReadSchema(modelFile);

			PackageMetaData packageMetaData = server.getMetaDataManager().getPackageMetaData(schema.getEPackageName());
			deserializer.init(packageMetaData, platformService);
			deserializer.read(modelFile);
			
			rid = deserializer.getRid();
			fixInverses(packageMetaData, rid);

			StreamingGeometryGenerator generator = new StreamingGeometryGenerator(server, platformService, rid);
			QueryContext queryContext = new QueryContext(platformService, packageMetaData, rid);
			generator.generateGeometry(queryContext);
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteModel(Integer rid) {
		// TODO Auto-generated method stub

	}

	@Override
	public ModelInfoVo queryModelInfoByRid(Integer rid) {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BuildingStorey> queryModelBuildingStorey(Integer rid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<BuildingCellContainer> queryBuildingCells(Integer rid) {
		// TODO Auto-generated method stub
		return null;
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
		platformService.commitUpdateBatch();
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
