package cn.dlb.bim.ifc.database;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.mongodb.gridfs.GridFSDBFile;

import cn.dlb.bim.component.MongoGridFs;
import cn.dlb.bim.dao.entity.IdEObjectEntity;
import cn.dlb.bim.dao.entity.IfcModelEntity;
import cn.dlb.bim.ifc.database.binary.IfcDataBase;
import cn.dlb.bim.ifc.database.binary.IfcModelBinary;
import cn.dlb.bim.ifc.database.binary.TodoList;
import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IdEObjectImpl;
import cn.dlb.bim.ifc.emf.IdEObjectImpl.State;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.IfcModelInterfaceException;
import cn.dlb.bim.ifc.emf.MetaDataManager;
import cn.dlb.bim.ifc.emf.QueryInterface;
import cn.dlb.bim.models.geometry.GeometryData;
import cn.dlb.bim.models.geometry.GeometryInfo;
import cn.dlb.bim.models.ifc2x3tc1.IfcProduct;

public class IfcModelDbSession extends IfcModelBinary {

	private static final Logger LOGGER = LoggerFactory.getLogger(IfcModelDbSession.class);

//	private IfcModelDao ifcModelDao;
	private MongoGridFs mongoGridFs;
	private MetaDataManager metaDataManager;

	public IfcModelDbSession(MongoGridFs mongoGridFs, MetaDataManager metaDataManager, IfcDataBase ifcDataBase) {
		super(ifcDataBase);
		this.mongoGridFs = mongoGridFs;
		this.metaDataManager = metaDataManager;
	}
	
	public void saveIfcModel(IfcModelInterface model) throws IfcModelDbException {
		IfcModelEntity ifcModelEntity = new IfcModelEntity();
		final Integer revisionId = ifcDataBase.newRevisionId();
		model.getModelMetaData().setRevisionId(revisionId);
		model.fixOids(ifcDataBase);
		ifcModelEntity.setModelMetaData(model.getModelMetaData());
		ifcModelEntity.setRid(revisionId);
		BiMap<Long, IdEObject> objectBiMap = model.getObjects();
		List<Long> objectOids = new ArrayList<>();
		objectOids.addAll(objectBiMap.keySet());
		
		for (IdEObject object : objectBiMap.values()) {
			addObjectsToModelEntity(object, ifcModelEntity, false);
		}
		for (IfcProduct ifcProduct : model.getAllWithSubTypes(IfcProduct.class)) {
			GeometryInfo geometryInfo = ifcProduct.getGeometry();
			if (geometryInfo != null) {
				addObjectsToModelEntity(geometryInfo, ifcModelEntity, true);
				GeometryData geometryData = geometryInfo.getData();
				if (geometryData != null) {
					addObjectsToModelEntity(geometryData, ifcModelEntity, true);
				}
			}
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.flush();
			oos.writeObject(ifcModelEntity);
			InputStream inputStream = new ByteArrayInputStream(bos.toByteArray());
			String filename = ifcModelEntity.getModelMetaData().getIfcHeader().getFilename();
			mongoGridFs.saveIfcModel(inputStream, filename, ifcModelEntity.getRid());
			ifcDataBase.updateDataBase();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
//		ifcModelDao.insertIfcModelEntity(ifcModelEntity);
		
	}

	public void addObjectsToModelEntity(IdEObject idEObject, IfcModelEntity ifcModelEntity, boolean unidentified) throws IfcModelDbException {
		if (idEObject.getOid() == -1) {
			throw new IfcModelDbException("Cannot store object with oid -1");
		}
		ByteBuffer valueBuffer = ByteBuffer.allocate(16);
		valueBuffer = convertObjectToByteArray(idEObject, valueBuffer,
				metaDataManager.getPackageMetaData(idEObject.eClass().getEPackage().getName()));
		IdEObjectEntity idEObjectEntity = new IdEObjectEntity();
		idEObjectEntity.setOid(idEObject.getOid());
		idEObjectEntity.setObjectBytes(valueBuffer.array());
		if (unidentified) {
			ifcModelEntity.getUnidentifiedObjectEntities().add(idEObjectEntity);//geometry in it
		} else {
			ifcModelEntity.getObjectEntities().add(idEObjectEntity);
		}
		
	}
	
//	public void commit() throws IfcModelDbException, ServiceException, IfcModelDbException {
//		// This buffer is reused for the values, it's position must be reset at
//		// the end of the loop, and the convertObjectToByteArray function is
//		// responsible for setting the buffer's position to the end of the (used
//		// part of the) buffer
//		
//		for (IdEObject object : objectsToSave) {
//			if (object.getOid() == -1) {
//				throw new IfcModelDbException("Cannot store object with oid -1");
//			}
//			ByteBuffer valueBuffer = ByteBuffer.allocate(16);
//			valueBuffer = convertObjectToByteArray(object, valueBuffer,
//					metaDataManager.getPackageMetaData(object.eClass().getEPackage().getName()));
//			IdEObjectEntity idEObjectEntity = new IdEObjectEntity();
//			idEObjectEntity.setOid(object.getOid());
//			idEObjectEntity.setRid(object.getRid());
//			idEObjectEntity.setObjectBytes(valueBuffer.array());
//			ifcModelDao.insertIdEObjectEntity(idEObjectEntity);
//		}
//	}
	
	public boolean get(int rid, IfcModelInterface model, QueryInterface query) throws IfcModelDbException, IfcModelInterfaceException {
		TodoList todoList = new TodoList();
//		IfcModelEntity modelEntity = ifcModelDao.queryIfcModelEntityByRid(rid);
		GridFSDBFile file = mongoGridFs.findIfcModel(rid);
		if (file == null) {
			return false;
		}
		
		InputStream inputStream = file.getInputStream();
		ObjectInputStream ois = null;
		IfcModelEntity modelEntity = null;
		try {
			ois = new ObjectInputStream(inputStream);
			modelEntity = (IfcModelEntity) ois.readObject();
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		if (modelEntity == null) {
			return false;
		}
		for (IdEObjectEntity objectEntity : modelEntity.getObjectEntities()) {
			get(objectEntity, model, query, todoList);
		}
		for (IdEObjectEntity unidentifiedObjectEntity : modelEntity.getUnidentifiedObjectEntities()) {
			get(unidentifiedObjectEntity, model, query, todoList);
		}
//		processTodoList(model, todoList, query, 2);//read into 2 deep level, i put geometryinfo and its child geometrydata in it;
		model.setModelMetaDataValue(modelEntity.getModelMetaData());
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends IdEObject> T get(IdEObjectEntity objectEntity, IfcModelInterface model, QueryInterface query, TodoList todoList) throws IfcModelDbException {
//		if (objectsToCommit.containsOid(oid)) {
//			return (T) objectsToCommit.getByOid(oid);
//		}
		IdEObjectImpl cachedObject = (IdEObjectImpl) objectCache.get(objectEntity.getOid());
		if (cachedObject != null) {
			if (cachedObject.getLoadingState() == State.LOADED && cachedObject.getRid() != Integer.MAX_VALUE) {
				cachedObject.load();
				return (T) cachedObject;
			}
		}
//		IdEObjectEntity objectEntity = ifcModelDao.queryIdEObjectEntityByOid(oid);
//		if (objectEntity == null) {
//			return null;
//		}
		ByteBuffer valueBuffer = ByteBuffer.wrap(objectEntity.getObjectBytes());
		EClass eClass = getEClassForOid(objectEntity.getOid());
		int rid = model.getModelMetaData().getRevisionId();
		T convertByteArrayToObject = (T) convertByteArrayToObject(eClass, eClass, objectEntity.getOid(), valueBuffer, model, rid, query, todoList);
		objectCache.put(convertByteArrayToObject.getOid(), convertByteArrayToObject);
		return convertByteArrayToObject;
	}
	
//	public void processTodoList(IfcModelEntity modelEntity, IfcModelInterface model, TodoList todoList, QueryInterface query, int deepLevel) throws IfcModelDbException {
//		for (int i = deepLevel; i >= 0; i--) {
//			List<Long> remainOids = new ArrayList<>();
//			remainOids.addAll(todoList.keySet());
//			for (Long oid : remainOids) {
//				IdEObject idEObject = todoList.get(oid);
//				get(oid, model, query, todoList);
//			}
//		}
//	}

}
