package cn.dlb.bim.ifc.database;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterators;

import cn.dlb.bim.cache.ModelCacheManager;
import cn.dlb.bim.dao.IfcModelDao;
import cn.dlb.bim.dao.entity.IdEObjectEntity;
import cn.dlb.bim.dao.entity.IfcModelEntity;
import cn.dlb.bim.ifc.database.binary.IfcDataBase;
import cn.dlb.bim.ifc.database.binary.IfcModelBinary;
import cn.dlb.bim.ifc.database.binary.TodoList;
import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IdEObjectImpl;
import cn.dlb.bim.ifc.emf.IdEObjectImpl.State;
import cn.dlb.bim.ifc.model.BasicIfcModel;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.IfcModelInterfaceException;
import cn.dlb.bim.ifc.emf.MetaDataManager;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.QueryInterface;
import cn.dlb.bim.ifc.shared.ProgressReporter;
import cn.dlb.bim.models.geometry.GeometryData;
import cn.dlb.bim.models.geometry.GeometryInfo;
import cn.dlb.bim.models.ifc2x3tc1.IfcProduct;
import cn.dlb.bim.vo.ModelInfoVo;

public class IfcModelDbSession extends IfcModelBinary {

	private static final Logger LOGGER = LoggerFactory.getLogger(IfcModelDbSession.class);

	private final IfcModelDao ifcModelDao;
	private final MetaDataManager metaDataManager;
	private ProgressReporter progressReporter;
	private ModelCacheManager modelCacheManager;

	public IfcModelDbSession(IfcModelDao ifcModelDao, MetaDataManager metaDataManager, IfcDataBase ifcDataBase,
			ProgressReporter progressReporter, ModelCacheManager modelCacheManager) {
		super(ifcDataBase);
		this.ifcModelDao = ifcModelDao;
		this.metaDataManager = metaDataManager;
		this.progressReporter = progressReporter;
		this.modelCacheManager = modelCacheManager;
	}

	public void saveIfcModel(IfcModelInterface model, ModelInfoVo modelInfo) throws IfcModelDbException {
		IfcModelEntity ifcModelEntity = new IfcModelEntity();
		final Integer revisionId = ifcDataBase.newRevisionId();
		model.getModelMetaData().setRevisionId(revisionId);
		model.fixOids(ifcDataBase);
		ifcModelEntity.setModelMetaData(model.getModelMetaData());
		ifcModelEntity.setRid(revisionId);
		ifcModelEntity.setPid(modelInfo.getPid());
		ifcModelEntity.setApplyType(modelInfo.getApplyType());
		ifcModelEntity.setName(modelInfo.getName());

		Map<Long, IdEObject> geometries = new HashMap<>();

		for (IfcProduct ifcProduct : model.getAllWithSubTypes(IfcProduct.class)) {
			GeometryInfo geometryInfo = ifcProduct.getGeometry();
			if (geometryInfo != null && !geometries.containsKey(geometryInfo.getOid())) {
				geometries.put(geometryInfo.getOid(), geometryInfo);
				GeometryData geometryData = geometryInfo.getData();
				if (geometryData != null && !geometries.containsKey(geometryData.getOid())) {
					geometries.put(geometryData.getOid(), geometryData);
				}
			}
		}

		Iterator<IdEObject> iterator = Iterators.concat(model.getObjects().values().iterator(),
				geometries.values().iterator());

		List<IdEObjectEntity> idEObjectEntityList = new ArrayList<>();

		while (iterator.hasNext()) {
			IdEObject object = iterator.next();
			ByteBuffer valueBuffer = ByteBuffer.allocate(16);
			valueBuffer = convertObjectToByteArray(object, valueBuffer,
					metaDataManager.getPackageMetaData(object.eClass().getEPackage().getName()));
			IdEObjectEntity idEObjectEntity = new IdEObjectEntity();
			idEObjectEntity.setOid(object.getOid());
			idEObjectEntity.setRid(revisionId);
			idEObjectEntity.setObjectBytes(valueBuffer.array());
			idEObjectEntityList.add(idEObjectEntity);
		}

		ifcModelDao.insertIfcModelEntity(ifcModelEntity);
		ifcModelDao.insertAllIdEObjectEntity(idEObjectEntityList);

		ifcDataBase.updateDataBase();
		modelCacheManager.cacheModel(revisionId, model);
	}

	public IfcModelInterface get(PackageMetaData packageMetaData, int rid, QueryInterface query)
			throws IfcModelDbException, IfcModelInterfaceException {
		if (modelCacheManager.contains(rid)) {
			return modelCacheManager.getIfcModel(rid);
		}
		IfcModelInterface model = new BasicIfcModel(packageMetaData);
		TodoList todoList = new TodoList();

		progressReporterTitle("Querying ifcmodel ...");

		IfcModelEntity modelEntity = ifcModelDao.queryIfcModelEntityByRid(rid);

		if (modelEntity == null) {
			return null;
		}

		List<IdEObjectEntity> idEObjectEntitys = ifcModelDao.queryIdEObjectEntityByRid(rid);
		Long total = Long.valueOf(idEObjectEntitys.size());
		progressReporterTitle("Reading objects.");
		progressReporterUpdate(0l, total);

		Long doneObjectCount = 0l;
		for (IdEObjectEntity objectEntity : idEObjectEntitys) {
			get(objectEntity, model, query, todoList);
			progressReporterUpdate(++doneObjectCount, total);
		}
		model.setModelMetaDataValue(modelEntity.getModelMetaData());
		
		modelCacheManager.cacheModel(rid, model);
		return model;
	}

	@SuppressWarnings("unchecked")
	public <T extends IdEObject> T get(IdEObjectEntity objectEntity, IfcModelInterface model, QueryInterface query,
			TodoList todoList) throws IfcModelDbException {
		IdEObjectImpl cachedObject = (IdEObjectImpl) objectCache.get(objectEntity.getOid());
		if (cachedObject != null) {
			if (cachedObject.getLoadingState() == State.LOADED && cachedObject.getRid() != Integer.MAX_VALUE) {
				cachedObject.load();
				return (T) cachedObject;
			}
		}
		ByteBuffer valueBuffer = ByteBuffer.wrap(objectEntity.getObjectBytes());
		EClass eClass = getEClassForOid(objectEntity.getOid());
		int rid = model.getModelMetaData().getRevisionId();
		T convertByteArrayToObject = (T) convertByteArrayToObject(eClass, eClass, objectEntity.getOid(), valueBuffer,
				model, rid, query, todoList);
		objectCache.put(convertByteArrayToObject.getOid(), convertByteArrayToObject);
		return convertByteArrayToObject;
	}

	public void progressReporterTitle(String title) {
		if (progressReporter != null) {
			progressReporter.setTitle(title);
		}
	}

	public void progressReporterUpdate(Long progress, Long max) {
		if (progressReporter != null) {
			progressReporter.update(progress, max);
		}
	}

}