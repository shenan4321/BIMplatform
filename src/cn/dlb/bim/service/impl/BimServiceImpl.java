package cn.dlb.bim.service.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;

import cn.dlb.bim.cache.CacheDescriptor;
import cn.dlb.bim.component.MongoGridFs;
import cn.dlb.bim.component.PlatformInitDatas;
import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.dao.IfcModelDao;
import cn.dlb.bim.dao.entity.IfcModelEntity;
import cn.dlb.bim.dao.entity.ModelLabel;
import cn.dlb.bim.ifc.GeometryGenerator;
import cn.dlb.bim.ifc.collada.GlbSerializer;
import cn.dlb.bim.ifc.database.IfcModelDbException;
import cn.dlb.bim.ifc.database.IfcModelDbSession;
import cn.dlb.bim.ifc.database.OldQuery;
import cn.dlb.bim.ifc.deserializers.DeserializeException;
import cn.dlb.bim.ifc.deserializers.IfcStepDeserializer;
import cn.dlb.bim.ifc.deserializers.StepParser;
import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.IfcModelInterfaceException;
import cn.dlb.bim.ifc.emf.OfflineOidProvider;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.ProjectInfo;
import cn.dlb.bim.ifc.emf.Schema;
import cn.dlb.bim.ifc.engine.IRenderEngine;
import cn.dlb.bim.ifc.engine.RenderEngineException;
import cn.dlb.bim.ifc.engine.cells.Vector3d;
import cn.dlb.bim.ifc.serializers.IfcStepSerializer;
import cn.dlb.bim.ifc.serializers.SerializerException;
import cn.dlb.bim.ifc.shared.ProgressReporter;
import cn.dlb.bim.ifc.tree.BuildingCellContainer;
import cn.dlb.bim.ifc.tree.BuildingCellGenerator;
import cn.dlb.bim.ifc.tree.BuildingStorey;
import cn.dlb.bim.ifc.tree.BuildingStoreyGenerator;
import cn.dlb.bim.ifc.tree.Material;
import cn.dlb.bim.ifc.tree.MaterialGenerator;
import cn.dlb.bim.ifc.tree.ProjectTree;
import cn.dlb.bim.ifc.tree.ProjectTreeGenerator;
import cn.dlb.bim.ifc.tree.PropertyGenerator;
import cn.dlb.bim.ifc.tree.PropertySet;
import cn.dlb.bim.models.geometry.GeometryInfo;
import cn.dlb.bim.service.BimService;
import cn.dlb.bim.utils.BinUtils;
import cn.dlb.bim.utils.CacheUtils;
import cn.dlb.bim.utils.IdentifyManager;
import cn.dlb.bim.vo.GeometryInfoVo;
import cn.dlb.bim.vo.GlbVo;
import cn.dlb.bim.vo.ModelInfoVo;
import cn.dlb.bim.vo.ModelLabelVo;

@Service("BimServiceImpl")
public class BimServiceImpl implements BimService {

	private static String IFC2X3_SCHEMA_SHORT = "IFC2X3";
	private static String IFC4_SCHEMA_SHORT = "IFC4";

	@Autowired
	@Qualifier("PlatformServer")
	private PlatformServer server;

	@Autowired
	@Qualifier("IfcModelDaoImpl")
	private IfcModelDao ifcModelDao;

	@Override
	public List<GeometryInfoVo> queryGeometryInfo(Integer rid, ProgressReporter progressReporter) {
		CacheDescriptor cacheDescriptor = new CacheDescriptor("queryGeometryInfo", rid);
		CacheUtils<GeometryInfoVo> utils = new CacheUtils<>(server.getDiskCacheManager());
		List<GeometryInfoVo> result = utils.readListFromCache(cacheDescriptor, GeometryInfoVo.class);
		if (result != null) {
			return result;
		}
		
		IfcModelInterface model = queryModelByRid(rid, progressReporter);
		result = new ArrayList<>();
		PackageMetaData packageMetaData = model.getPackageMetaData();
		EClass productClass = (EClass) model.getPackageMetaData().getEClassifierCaseInsensitive("IfcProduct");
		List<IdEObject> projectList = model.getAllWithSubTypes(productClass);

		for (IdEObject ifcProduct : projectList) {
			GeometryInfoVo adaptor = new GeometryInfoVo();
			GeometryInfo geometryInfo = (GeometryInfo) ifcProduct
					.eGet(ifcProduct.eClass().getEStructuralFeature("geometry"));
			if (geometryInfo != null) {
				Boolean defualtVisiable = !packageMetaData.getEClass("IfcSpace").isSuperTypeOf(ifcProduct.eClass()) 
						&& !packageMetaData.getEClass("IfcFeatureElementSubtraction").isSuperTypeOf(ifcProduct.eClass());//IfcFeatureElementSubtraction
				if (!defualtVisiable) {//TODO
					continue;
				}
				MaterialGenerator materialGetter = new MaterialGenerator(model);
				Material material = materialGetter.getMaterial(ifcProduct);
				adaptor.transform(geometryInfo, ifcProduct.getOid(), ifcProduct.eClass().getName(), defualtVisiable,
						material == null ? null : material.getAmbient());
				result.add(adaptor);
			}
		}
		
		utils.cacheList(cacheDescriptor, result);
		
		return result;
	}

	@Override
	public Integer addRevision(ModelInfoVo modelInfo, File modelFile) {

		Schema schema = null;
		try {
			schema = preReadSchema(modelFile);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DeserializeException e) {
			e.printStackTrace();
		}

		if (schema == null) {
			return -1;
		}

		IfcStepDeserializer deserializer = server.getSerializationManager().createIfcStepDeserializer(schema);
		IfcStepSerializer serializer = server.getSerializationManager().createIfcStepSerializer(schema);
		int rid = -1;
		try {
			deserializer.read(modelFile);

			IfcModelInterface model = deserializer.getModel();

			IRenderEngine renderEngine = server.getRenderEngineFactory().createRenderEngine(schema.getEPackageName());

			GeometryGenerator generator = new GeometryGenerator(model, serializer, renderEngine);
			generator.generateForAllElements();

			PlatformInitDatas platformInitDatas = server.getPlatformInitDatas();
			model.fixOids(platformInitDatas);
			IfcModelDbSession session = new IfcModelDbSession(server.getIfcModelDao(), server.getMetaDataManager(),
					platformInitDatas, null, server.getModelCacheManager());
			session.saveIfcModel(model, modelInfo);
			rid = model.getModelMetaData().getRevisionId();
		} catch (DeserializeException e) {
			e.printStackTrace();
		} catch (RenderEngineException e) {
			e.printStackTrace();
		} catch (IfcModelDbException e) {
			e.printStackTrace();
		}

		return rid;
	}

	@Override
	public GlbVo queryGlbByRid(Integer rid) {
		// 如果在文件缓存中直接从文件缓存中取
		GlbVo glbVo = queryGlbByRidFromCache(rid);
		if (glbVo != null) {
			return glbVo;
		}
		generateGlbAndCache(rid);
		glbVo = queryGlbByRidFromCache(rid);
		return glbVo;
	}

	private GlbVo queryGlbByRidFromCache(Integer rid) {
		GridFSDBFile glbFile = server.getColladaCacheManager().getGlbCache(rid);
		if (glbFile == null) {
			return null;
		}
		GlbVo glbVo = null;
		try {
			glbVo = convertFromGridFSDBFile(glbFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return glbVo;
	}

	private GlbVo convertFromGridFSDBFile(GridFSDBFile glbFile) throws IOException {
		DBObject metaData = glbFile.getMetaData();
		double lon = (double) metaData.get("lon");
		double lat = (double) metaData.get("lat");
		GlbVo glbVo = new GlbVo();
		glbVo.setLon(lon);
		glbVo.setLat(lat);
		InputStream inputStream = glbFile.getInputStream();
		byte[] data = BinUtils.readInputStream(inputStream);
		glbVo.setData(data);
		return glbVo;
	}

	private double getDegreeFromCompoundPlaneAngle(EList<Long> values) {
		if (values.size() > 4) {
			return 0;
		}
		double result = 0.0;
		double[] level = { 60, 60, 1000000 };
		double currentLevel = 1;
		for (int i = 0; i < values.size(); i++) {
			Long v = values.get(i);
			result += v / currentLevel;
			if (i < 3) {
				currentLevel *= level[i];
			}
		}
		return result;
	}

	@Override
	public Vector3d queryGlbLonlatByRid(Integer rid) {
		GridFSDBFile glbFile = server.getColladaCacheManager().getGlbCache(rid);
		if (glbFile == null) {
			generateGlbAndCache(rid);
			glbFile = server.getColladaCacheManager().getGlbCache(rid);
		}
		if (glbFile == null) {
			return new Vector3d(0, 0, 0);
		}
		DBObject metaData = glbFile.getMetaData();
		double lon = (double) metaData.get("lon");
		double lat = (double) metaData.get("lat");
		return new Vector3d(lon, lat, 0);
	}

	@SuppressWarnings("unchecked")
	private void generateGlbAndCache(Integer rid) {
		IfcModelInterface model = queryModelByRid(rid, null);

		GlbSerializer serializer = new GlbSerializer(server);
		ProjectInfo projectInfo = new ProjectInfo();
		projectInfo.setName("bim");
		projectInfo.setAuthorName("linfujun");
		ByteArrayOutputStream glbOutput = new ByteArrayOutputStream();
		try {
			serializer.init(model, projectInfo, true);
			serializer.writeToOutputStream(glbOutput, null);
		} catch (SerializerException e) {
			e.printStackTrace();
		}

		double longitude = 0.0;
		double latitude = 0.0;
		EClass ifcSiteClass = model.getPackageMetaData().getEClass("IfcSite");
		List<IdEObject> ifcSiteList = model.getAllWithSubTypes(ifcSiteClass);
		if (ifcSiteList.size() > 0) {
			IdEObject site = ifcSiteList.get(0);
			Object refLongitudeObject = site.eGet(site.eClass().getEStructuralFeature("RefLongitude"));
			Object refLatitudeObject = site.eGet(site.eClass().getEStructuralFeature("RefLatitude"));
			if (refLongitudeObject != null && refLatitudeObject != null) {
				EList<Long> refLongitude = (EList<Long>) refLongitudeObject;
				EList<Long> refLatitude = (EList<Long>) refLatitudeObject;
				longitude = getDegreeFromCompoundPlaneAngle(refLongitude);
				latitude = getDegreeFromCompoundPlaneAngle(refLatitude);
			}
		}

		ByteArrayInputStream glbInput = new ByteArrayInputStream(glbOutput.toByteArray());
		server.getColladaCacheManager().saveGlb(glbInput, rid.toString(), rid, longitude, latitude);
	}

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

	@Override
	public IfcModelInterface queryModelByRid(Integer rid, ProgressReporter progressReporter) {
		IfcModelEntity ifcModelEntity = ifcModelDao.queryIfcModelEntityByRid(rid);
		String ifcSchemaVersion = ifcModelEntity.getModelMetaData().getIfcHeader().getIfcSchemaVersion();
		PackageMetaData packageMetaData = null;
		if (ifcSchemaVersion.startsWith(IFC2X3_SCHEMA_SHORT)) {
			packageMetaData = server.getMetaDataManager().getPackageMetaData(Schema.IFC2X3TC1.getEPackageName());
		} else {
			packageMetaData = server.getMetaDataManager().getPackageMetaData(Schema.IFC4.getEPackageName());
		}
		PlatformInitDatas platformInitDatas = server.getPlatformInitDatas();
		IfcModelDbSession session = new IfcModelDbSession(server.getIfcModelDao(), server.getMetaDataManager(),
				platformInitDatas, progressReporter, server.getModelCacheManager());
		IfcModelInterface model = null;

		try {
			model = session.get(packageMetaData, rid, new OldQuery(packageMetaData, true));
		} catch (IfcModelDbException e) {
			e.printStackTrace();
		} catch (IfcModelInterfaceException e) {
			e.printStackTrace();
		}

		return model;
	}

	@Override
	public List<ModelInfoVo> queryModelInfoByPid(Long pid) {
		List<IfcModelEntity> ifcModelEntityList = ifcModelDao.queryIfcModelEntityByPid(pid);
		List<ModelInfoVo> result = new ArrayList<>();
		for (IfcModelEntity ifcModelEntity : ifcModelEntityList) {
			ModelInfoVo modelInfo = new ModelInfoVo();
			modelInfo.setName(ifcModelEntity.getName());
			modelInfo.setPid(ifcModelEntity.getPid());
			modelInfo.setApplyType(ifcModelEntity.getApplyType());
			modelInfo.setRid(ifcModelEntity.getRid());
			modelInfo.setFileName(ifcModelEntity.getFileName());
			modelInfo.setFileSize(ifcModelEntity.getFileSize());
			modelInfo.setUploadDate(ifcModelEntity.getUploadDate());
			result.add(modelInfo);
		}
		return result;
	}

	@Override
	public ModelInfoVo queryModelInfoByRid(Integer rid) {
		IfcModelEntity ifcModelEntity = ifcModelDao.queryIfcModelEntityByRid(rid);
		ModelInfoVo result = new ModelInfoVo();
		result.setName(ifcModelEntity.getName());
		result.setPid(ifcModelEntity.getPid());
		result.setApplyType(ifcModelEntity.getApplyType());
		result.setRid(ifcModelEntity.getRid());
		result.setFileName(ifcModelEntity.getFileName());
		result.setFileSize(ifcModelEntity.getFileSize());
		result.setUploadDate(ifcModelEntity.getUploadDate());
		return result;
	}

	@Override
	public void deleteModel(Integer rid) {
		ifcModelDao.deleteIdEObjectEntity(rid);
		ifcModelDao.deleteIfcModelEntity(rid);
	}

	@Override
	public void setGlbLonlat(Integer rid, Double lon, Double lat) {
		GridFSDBFile glbFile = server.getColladaCacheManager().getGlbCache(rid);
		if (glbFile == null) {
			generateGlbAndCache(rid);
		}
		server.getColladaCacheManager().modifyGlb(rid, lon, lat);
	}

	@Override
	public void insertModelLabel(ModelLabelVo modelLabel) {
		ModelLabel labelToSave = modelLabel.getEntity();
		ifcModelDao.insertModelLabel(labelToSave);
	}

	@Override
	public void deleteModelLabel(Integer labelId) {
		ifcModelDao.deleteModelLabel(labelId);
	}

	@Override
	public void modifyModelLabel(ModelLabelVo modelLabel) {
		ModelLabel labelToModify = modelLabel.getEntity();
		ifcModelDao.modifyModelLabel(labelToModify);
	}

	@Override
	public List<ModelLabelVo> queryAllModelLabelByRid(Integer rid) {
		List<ModelLabel> modelLabelList = ifcModelDao.queryAllModelLabelByRid(rid);
		List<ModelLabelVo> result = new ArrayList<>();
		for (ModelLabel modelLabel : modelLabelList) {
			ModelLabelVo modelLabelVo = new ModelLabelVo();
			modelLabelVo.setEntity(modelLabel);
			result.add(modelLabelVo);
		}
		return result;
	}

	@Override
	public ProjectTree queryModelTree(Integer rid) {
		CacheDescriptor downloadDescriptor = new CacheDescriptor("queryModelTree", rid);
		CacheUtils<ProjectTree> utils = new CacheUtils<>(server.getDiskCacheManager());
		ProjectTree result = utils.readObjectFromCache(downloadDescriptor, ProjectTree.class);
		if (result != null) {
			return result;
		}	
		
		IfcModelInterface model = queryModelByRid(rid, null);
		ProjectTreeGenerator treeGenerator = new ProjectTreeGenerator(model.getPackageMetaData());
		treeGenerator.buildProjectTree(model, ProjectTreeGenerator.KeyWord_IfcProject);
		result = treeGenerator.getTree();
		
		utils.cacheObject(downloadDescriptor, result);
		
		return result;
	}

	@Override
	public List<BuildingStorey> queryModelBuildingStorey(Integer rid) {
		CacheDescriptor downloadDescriptor = new CacheDescriptor("queryModelBuildingStorey", rid);
		CacheUtils<BuildingStorey> utils = new CacheUtils<>(server.getDiskCacheManager());
		List<BuildingStorey> result = utils.readListFromCache(downloadDescriptor, BuildingStorey.class);
		if (result != null) {
			return result;
		}	
		
		IfcModelInterface model = queryModelByRid(rid, null);
		BuildingStoreyGenerator generator = new BuildingStoreyGenerator(model.getPackageMetaData());
		result = generator.generateBuildingStorey(model);
		
		utils.cacheList(downloadDescriptor, result);
		
		return result;
	}

	@Override
	public List<BuildingCellContainer> queryBuildingCells(Integer rid) {
		CacheDescriptor downloadDescriptor = new CacheDescriptor("queryBuildingCells", rid);
		CacheUtils<BuildingCellContainer> utils = new CacheUtils<>(server.getDiskCacheManager());
		List<BuildingCellContainer> result = utils.readListFromCache(downloadDescriptor, BuildingCellContainer.class);
		if (result != null) {
			return result;
		}		
		
		IfcModelInterface model = queryModelByRid(rid, null);
		BuildingCellGenerator generator = new BuildingCellGenerator();
		result = generator.buildBuildingCells(model);
		
		utils.cacheList(downloadDescriptor, result);
		
		return result;
	}

	@Override
	public List<PropertySet> queryProperty(Integer rid, Long oid) {
		CacheDescriptor downloadDescriptor = new CacheDescriptor("queryProperty", rid, oid);
		CacheUtils<PropertySet> utils = new CacheUtils<>(server.getDiskCacheManager());
		List<PropertySet> result = utils.readListFromCache(downloadDescriptor, PropertySet.class);
		if (result != null) {
			return result;
		}
		
		IfcModelInterface model = queryModelByRid(rid, null);
		IdEObject targetObject = model.get(oid);
		PropertyGenerator propertyGenerator = new PropertyGenerator();
		result = propertyGenerator.getProperty(model.getPackageMetaData(), targetObject);

		utils.cacheList(downloadDescriptor, result);
		
		return result;
	}
	
	@Override
	public Long convertIfcToGlbOffline(File modelFile) {
		Long glbId = -1l;
		Schema schema = null;
		try {
			schema = preReadSchema(modelFile);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DeserializeException e) {
			e.printStackTrace();
		}

		if (schema == null) {
			return null;
		}
		ByteArrayOutputStream glbOutput = null;
		IfcStepDeserializer deserializer = server.getSerializationManager().createIfcStepDeserializer(schema);
		IfcStepSerializer serializer = server.getSerializationManager().createIfcStepSerializer(schema);
		try {
			deserializer.read(modelFile);

			IfcModelInterface model = deserializer.getModel();

			IRenderEngine renderEngine = server.getRenderEngineFactory().createRenderEngine(schema.getEPackageName());

			GeometryGenerator generator = new GeometryGenerator(model, serializer, renderEngine);
			generator.generateForAllElements();
			model.fixOids(new OfflineOidProvider());
			
			GlbSerializer glbSerializer = new GlbSerializer(server);
			ProjectInfo projectInfo = new ProjectInfo();
			projectInfo.setName("bim");
			projectInfo.setAuthorName("linfujun");
			glbOutput = new ByteArrayOutputStream();
			glbSerializer.init(model, projectInfo, true);
			glbSerializer.writeToOutputStream(glbOutput, null);
			
			double longitude = 0.0;
			double latitude = 0.0;
			EClass ifcSiteClass = model.getPackageMetaData().getEClass("IfcSite");
			List<IdEObject> ifcSiteList = model.getAllWithSubTypes(ifcSiteClass);
			if (ifcSiteList.size() > 0) {
				IdEObject site = ifcSiteList.get(0);
				Object refLongitudeObject = site.eGet(site.eClass().getEStructuralFeature("RefLongitude"));
				Object refLatitudeObject = site.eGet(site.eClass().getEStructuralFeature("RefLatitude"));
				if (refLongitudeObject != null && refLatitudeObject != null) {
					EList<Long> refLongitude = (EList<Long>) refLongitudeObject;
					EList<Long> refLatitude = (EList<Long>) refLatitudeObject;
					longitude = getDegreeFromCompoundPlaneAngle(refLongitude);
					latitude = getDegreeFromCompoundPlaneAngle(refLatitude);
				}
			}

			ByteArrayInputStream glbInput = new ByteArrayInputStream(glbOutput.toByteArray());
			MongoGridFs gridFs = server.getMongoGridFs();
			glbId = IdentifyManager.getIdentifyManager().nextId(IdentifyManager.OFFLINE_GLB_KEY);
			gridFs.saveGlbOffline(glbInput, modelFile.getName(), glbId, longitude, latitude);

		} catch (DeserializeException e) {
			e.printStackTrace();
		} catch (RenderEngineException e) {
			e.printStackTrace();
		} catch (SerializerException e) {
			e.printStackTrace();
		}

		return glbId;
	}

	@Override
	public GlbVo queryGlbByGlbId(Long glbId) {
		GlbVo glbVo = null;
		MongoGridFs gridFs = server.getMongoGridFs();
		GridFSDBFile glbFile = gridFs.findGlbFileOffline(glbId);
		try {
			glbVo = convertFromGridFSDBFile(glbFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return glbVo;
	}
}
