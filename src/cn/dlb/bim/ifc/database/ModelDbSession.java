package cn.dlb.bim.ifc.database;

public class ModelDbSession {
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(ModelDbSession.class);
//	
//	private final ObjectsToSave objectsToSave = new ObjectsToSave();
//	
//
//	public void addToObjectsToCommit(IdEObject idEObject) throws ModelDbException {
//		if (idEObject.getOid() == -1) {
//			throw new ModelDbException("Cannot store object with oid -1");
//		}
//		objectsToSave.put(idEObject);
//	}
//	
//	public void commit() throws ModelDbException, ServiceException {
//		try {
//			// This buffer is reused for the values, it's position must be reset at the end of the loop, and the convertObjectToByteArray function is responsible for setting the buffer's position to the end of the (used part of the) buffer
//			ByteBuffer reusableBuffer = ByteBuffer.allocate(32768);
//			for (IdEObject object : objectsToSave) {
//				if (object.getOid() == -1) {
//					throw new ModelDbException("Cannot store object with oid -1");
//				}
//				fillKeyBuffer(keyBuffer, object);
//				LOGGER.info("Write: " + object.eClass().getName() + " " + "pid=" + object.getPid() + " oid=" + object.getOid() + " rid=" + object.getRid());
//				ByteBuffer valueBuffer = convertObjectToByteArray(object, reusableBuffer, getMetaDataManager().getPackageMetaData(object.eClass().getEPackage().getName()));
//				int valueBufferPosition = valueBuffer.position();
////				processPossibleIndices(keyBuffer, object.getPid(), object.getRid(), object.getOid(), object.eClass(), valueBuffer);
//				if (object.eClass().getEAnnotation("nolazyload") == null && !overwriteEnabled) {
//					database.getKeyValueStore().storeNoOverwrite(object.eClass().getEPackage().getName() + "_" + object.eClass().getName(), keyBuffer.array(), valueBuffer.array(), 0, valueBufferPosition, this);
//				} else {
//					database.getKeyValueStore().store(object.eClass().getEPackage().getName() + "_" + object.eClass().getName(), keyBuffer.array(),
//							valueBuffer.array(), 0, valueBuffer.position(), this);
//				}
//				if (progressHandler != null) {
//					progressHandler.progress(++current, objectsToCommit.size());
//				}
//				writes++;
//				reusableBuffer = valueBuffer; // bimServerClient may have increased the size of the buffer by creating a new one, we keep using it for other objects
//				reusableBuffer.position(0);
//			}
//			if (bimTransaction != null) {
//				bimTransaction.commit();
//				database.getKeyValueStore().sync();
//			}
//			database.incrementCommittedWrites(writes);
//			close();
//			for (PostCommitAction postCommitAction : postCommitActions) {
//				postCommitAction.execute();
//			}
//		} catch (BimserverDatabaseException e) {
//			throw e;
//		} catch (ServiceException e) {
//			throw e;
//		}
//	}
//	

}
