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

import cn.dlb.bim.component.PlatformInitDatas;
import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.dao.IfcModelDao;
import cn.dlb.bim.dao.entity.IfcModelEntity;
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
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.ProjectInfo;
import cn.dlb.bim.ifc.emf.Schema;
import cn.dlb.bim.ifc.engine.IRenderEngine;
import cn.dlb.bim.ifc.engine.MaterialGetter;
import cn.dlb.bim.ifc.engine.RenderEngineException;
import cn.dlb.bim.ifc.engine.cells.Material;
import cn.dlb.bim.ifc.engine.cells.Vector3d;
import cn.dlb.bim.ifc.serializers.IfcStepSerializer;
import cn.dlb.bim.ifc.serializers.SerializerException;
import cn.dlb.bim.models.geometry.GeometryInfo;
import cn.dlb.bim.models.ifc2x3tc1.IfcSite;
import cn.dlb.bim.service.BimService;
import cn.dlb.bim.utils.BinUtils;
import cn.dlb.bim.vo.GeometryInfoVo;
import cn.dlb.bim.vo.GlbVo;
import cn.dlb.bim.vo.ModelInfoVo;

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
	public List<GeometryInfoVo> queryGeometryInfo(Integer rid) {
		IfcModelInterface model = queryModelByRid(rid);
		List<GeometryInfoVo> geometryList = new ArrayList<>();
		PackageMetaData packageMetaData = model.getPackageMetaData();
		EClass productClass = (EClass) model.getPackageMetaData().getEClassifierCaseInsensitive("IfcProduct");
		List<IdEObject> projectList = model.getAllWithSubTypes(productClass);

		for (IdEObject ifcProduct : projectList) {
			GeometryInfoVo adaptor = new GeometryInfoVo();
			GeometryInfo geometryInfo = (GeometryInfo) ifcProduct.eGet(ifcProduct.eClass().getEStructuralFeature("geometry"));
			if (geometryInfo != null) {
				Boolean defualtVisiable = !ifcProduct.eClass().isSuperTypeOf(packageMetaData.getEClass("IfcSpace"));
				MaterialGetter materialGetter = new MaterialGetter(model);
				Material material = materialGetter.getMaterial(ifcProduct);
				adaptor.transform(geometryInfo, ifcProduct.getOid(), ifcProduct.eClass().getName(), defualtVisiable, material.getAmbient());
				geometryList.add(adaptor);
			}
		}
		return geometryList;
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
			IfcModelDbSession session = new IfcModelDbSession(server.getIfcModelDao(), server.getMetaDataManager(), platformInitDatas, null, server.getModelCacheManager());
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
		//如果在文件缓存中直接从文件缓存中取
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
		double[] level = {60, 60, 1000000};
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
	
	private void generateGlbAndCache(Integer rid) {
		IfcModelInterface model = queryModelByRid(rid);
		
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
		List<IfcSite> IfcSiteList = model.getAllWithSubTypes(IfcSite.class);
		if (IfcSiteList.size() > 0) {
			IfcSite site = IfcSiteList.get(0);
			longitude = getDegreeFromCompoundPlaneAngle(site.getRefLongitude());
			latitude = getDegreeFromCompoundPlaneAngle(site.getRefLatitude());
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
	public IfcModelInterface queryModelByRid(Integer rid) {
		IfcModelEntity ifcModelEntity = ifcModelDao.queryIfcModelEntityByRid(rid);
		String ifcSchemaVersion = ifcModelEntity.getModelMetaData().getIfcHeader().getIfcSchemaVersion();
		PackageMetaData packageMetaData = null;
		if (ifcSchemaVersion.startsWith(IFC2X3_SCHEMA_SHORT)) {
			packageMetaData = server.getMetaDataManager()
					.getPackageMetaData(Schema.IFC2X3TC1.getEPackageName());
		} else {
			packageMetaData = server.getMetaDataManager()
					.getPackageMetaData(Schema.IFC4.getEPackageName());
		}
		PlatformInitDatas platformInitDatas = server.getPlatformInitDatas();
		IfcModelDbSession session = new IfcModelDbSession(server.getIfcModelDao(), server.getMetaDataManager(), platformInitDatas, null, server.getModelCacheManager());
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
		for (IfcModelEntity ifcModelEntity :ifcModelEntityList) {
			ModelInfoVo modelInfo = new ModelInfoVo();
			modelInfo.setName(ifcModelEntity.getName());
			modelInfo.setPid(ifcModelEntity.getPid());
			modelInfo.setApplyType(ifcModelEntity.getApplyType());
			modelInfo.setRid(ifcModelEntity.getRid());
			result.add(modelInfo);
		}
		return result;
	}

	@Override
	public void deleteModel(Integer rid) {
		ifcModelDao.deleteIdEObjectEntity(rid);
		ifcModelDao.deleteIfcModelEntity(rid);
	}
}
