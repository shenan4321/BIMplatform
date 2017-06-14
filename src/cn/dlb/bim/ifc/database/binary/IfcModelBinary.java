package cn.dlb.bim.ifc.database.binary;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Date;
import java.util.List;

import org.eclipse.emf.common.util.AbstractEList;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EEnumImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import cn.dlb.bim.ifc.database.IfcModelDbException;
import cn.dlb.bim.ifc.database.ObjectCache;
import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IdEObjectImpl;
import cn.dlb.bim.ifc.emf.IdEObjectImpl.State;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.IfcModelInterfaceException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.QueryInterface;
import cn.dlb.bim.models.geometry.GeometryPackage;
import cn.dlb.bim.models.ifc2x3tc1.Ifc2x3tc1Package;
import cn.dlb.bim.models.ifc4.Ifc4Package;
import cn.dlb.bim.models.store.StorePackage;
import cn.dlb.bim.utils.BinUtils;

public class IfcModelBinary {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IfcModelBinary.class);
	
	protected IfcDataBase ifcDataBase;
	protected final ObjectCache objectCache = new ObjectCache();
	
	public static final int STORE_PROJECT_ID = 1;
	
	public IfcModelBinary(IfcDataBase ifcDataBase) {
		this.ifcDataBase = ifcDataBase;
	}
	
	public IdEObject convertByteArrayToObject(EClass originalQueryClass, EClass eClass, long oid, ByteBuffer buffer, IfcModelInterface model, Integer rid, QueryInterface query, TodoList todoList) throws IfcModelDbException {
		try {
			IdEObject idEObject = todoList.get(oid);
			todoList.remove(oid);
			
			if (idEObject == null) {
				idEObject = createInternal(eClass, query);
				((IdEObjectImpl) idEObject).setOid(oid);
				((IdEObjectImpl) idEObject).setPid(query.getPid());
			} 
			
			if (idEObject.eClass().getEAnnotation("wrapped") == null 
					&& idEObject.eClass().getEAnnotation("hidden") == null) {
				try {
					model.addAllowMultiModel(oid, idEObject);
				} catch (IfcModelInterfaceException e) {
					throw new IfcModelDbException(e);
				}
			}
			((IdEObjectImpl) idEObject).setRid(rid);
			((IdEObjectImpl) idEObject).useInverses(false);

			if (StorePackage.eINSTANCE == idEObject.eClass().getEPackage()) {
				LOGGER.info("Read: " + idEObject.eClass().getName() + " pid=" + query.getPid() + " oid=" + oid);
			}

			((IdEObjectImpl) idEObject).setLoadingState(State.LOADING);

			objectCache.put(oid, idEObject);
			
			int unsettedLength = model.getPackageMetaData().getUnsettedLength(eClass);
			
			byte[] unsetted = new byte[unsettedLength];
			buffer.get(unsetted);
			int fieldCounter = 0;
			
			for (EStructuralFeature feature : eClass.getEAllStructuralFeatures()) {
				try {
					if (model.getPackageMetaData().useForDatabaseStorage(eClass, feature)) {
						boolean isUnsetted = (unsetted[fieldCounter / 8] & (1 << (fieldCounter % 8))) != 0;
						if (isUnsetted) {
							if (feature.isUnsettable()) {
								idEObject.eUnset(feature);
							} else if (feature.isMany()) {
								// do nothing
							} else if (feature.getDefaultValue() != null) {
								idEObject.eSet(feature, feature.getDefaultValue());
							}
						} else {
							if (!query.shouldFollowReference(originalQueryClass, eClass, feature)) {
								// we have to do some reading to maintain a correct
								// index
								fakeRead(buffer, feature);
							} else {
								Object newValue = null;
								if (feature.isMany()) {
									newValue = readList(idEObject, buffer, model, query, todoList, feature);
								} else {
									if (feature.getEType() instanceof EEnum) {
										int enumOrdinal = buffer.getInt();
										if (enumOrdinal == -1) {
											newValue = null;
										} else {
											EClassifier eType = feature.getEType();
											EEnumLiteral enumLiteral = ((EEnumImpl) eType).getEEnumLiteral(enumOrdinal);
											if (enumLiteral != null) {
												newValue = enumLiteral.getInstance();
											}
										}
									} else if (feature.getEType() instanceof EClass) {
										// EReference eReference = (EReference) feature;
										buffer.order(ByteOrder.LITTLE_ENDIAN);
										short cid = buffer.getShort();
										buffer.order(ByteOrder.BIG_ENDIAN);
										if (cid == -1) {
											// null, do nothing
										} else if (cid < 0) {
											// non minus one and negative cid means value is embedded in record
											EClass referenceClass = ifcDataBase.getEClassForCid((short) (-cid));
											if (feature.getEAnnotation("dbembed") != null) {
												newValue = readEmbeddedValue(feature, buffer, referenceClass, query);
											} else {
												newValue = readWrappedValue(feature, buffer, referenceClass, query);
											}
										} else if (cid > 0) {
											// positive cid means value is reference to other record
											EClass referenceClass = ifcDataBase.getEClassForCid(cid);
											if (referenceClass == null) {
												throw new IfcModelDbException("No eClass found for cid " + cid);
											}
											// readReference is going to read a long, which includes the 2 bytes for the cid
											buffer.position(buffer.position() - 2);
											newValue = readReference(buffer, model, idEObject, feature, referenceClass, query, todoList);
											// if (eReference.getEOpposite() != null &&
											// ((IdEObjectImpl)
											// newValue).isLoadedOrLoading()) {
											// newValue = null;
											// }
										}
									} else if (feature.getEType() instanceof EDataType) {
										newValue = readPrimitiveValue(feature.getEType(), buffer, query);
									}
								}
								if (newValue != null) {
									idEObject.eSet(feature, newValue);
								}
							}
						}
						fieldCounter++;
					}
				} catch (StringIndexOutOfBoundsException e) {
					throw new IfcModelDbException("Reading " + eClass.getName() + "(" + oid + ")." + feature.getName(), e);
				} catch (BufferUnderflowException e) {
					throw new IfcModelDbException("Reading " + eClass.getName() + "(" + oid + ")." + feature.getName(), e);
				} catch (BufferOverflowException e) {
					throw new IfcModelDbException("Reading " + eClass.getName() + "(" + oid + ")." + feature.getName(), e);
				}
			}
			((IdEObjectImpl) idEObject).setLoaded();
			((IdEObjectImpl) idEObject).useInverses(true);
			if (idEObject.getRid() > 100000 || idEObject.getRid() < -100000) {
				LOGGER.debug("Improbable rid " + idEObject.getRid() + " - " + idEObject);
			}
			return idEObject;
		} catch (BufferUnderflowException e) {
			throw new IfcModelDbException("Reading " + eClass.getName(), e);
		} catch (BufferOverflowException e) {
			throw new IfcModelDbException("Reading " + eClass.getName(), e);
		}
	}

	public ByteBuffer convertObjectToByteArray(IdEObject object, ByteBuffer buffer, PackageMetaData packageMetaData) throws IfcModelDbException {
		int bufferSize = getExactSize(object, packageMetaData, true);
		if (bufferSize > buffer.capacity()) {
			LOGGER.debug("Buffer too small (" + bufferSize + ")");
			buffer = ByteBuffer.allocate(bufferSize);
		}
		
		int unsettedLength = packageMetaData.getUnsettedLength(object.eClass());
		
		byte[] unsetted = new byte[unsettedLength];
		int fieldCounter = 0;
		
		for (EStructuralFeature feature : object.eClass().getEAllStructuralFeatures()) {
			if (packageMetaData.useForDatabaseStorage(object.eClass(), feature)) {
				if (useUnsetBit(feature, object)) {
					unsetted[fieldCounter / 8] |= (1 << (fieldCounter % 8));
				}
				fieldCounter++;
			}
		}
		buffer.put(unsetted);
		
		EClass eClass = getEClassForOid(object.getOid());
		if (!eClass.isSuperTypeOf(object.eClass())) {
			throw new IfcModelDbException("Object with oid " + object.getOid() + " is a " + object.eClass().getName() + " but it's cid-part says it's a " + eClass.getName());
		}
		
		for (EStructuralFeature feature : object.eClass().getEAllStructuralFeatures()) {
			if (packageMetaData.useForDatabaseStorage(object.eClass(), feature)) {
				if (!useUnsetBit(feature, object)) {
					if (feature.isMany()) {
						writeList(object, buffer, packageMetaData, feature);
					} else {
						Object value = object.eGet(feature);
						if (feature.getEType() instanceof EEnum) {
							if (value == null) {
								buffer.putInt(-1);
							} else {
								EEnum eEnum = (EEnum) feature.getEType();
								EEnumLiteral eEnumLiteral = eEnum.getEEnumLiteralByLiteral(((Enum<?>) value).toString());
								if (eEnumLiteral != null) {
									buffer.putInt(eEnumLiteral.getValue());
								} else {
									LOGGER.error(((Enum<?>) value).toString() + " not found");
									buffer.putInt(-1);
								}
							}
						} else if (feature.getEType() instanceof EClass) {
							if (value == null) {
								buffer.order(ByteOrder.LITTLE_ENDIAN);
								buffer.putShort((short) -1);
								buffer.order(ByteOrder.BIG_ENDIAN);
							} else {
								IdEObject referencedObject = (IdEObject) value;
								EClass referencedClass = referencedObject.eClass();
								if (feature.getEAnnotation("dbembed") != null) {
									writeEmbeddedValue(object.getPid(), object.getRid(), value, buffer, packageMetaData);
								} else if (referencedClass.getEAnnotation("wrapped") != null) {
									writeWrappedValue(object.getPid(), object.getRid(), value, buffer, packageMetaData);
								} else {
									writeReference(object, value, buffer, feature);
								}
							}
						} else if (feature.getEType() instanceof EDataType) {
							writePrimitiveValue(feature, value, buffer);
						}
					}
				}
			}
		}
		if (buffer.position() != bufferSize) {
			throw new IfcModelDbException("Value buffer sizes do not match for " + object.eClass().getName() + " " + buffer.position() + "/" + bufferSize);
		}
		
		return buffer;
	}
	
	protected IdEObject readReference(ByteBuffer buffer, IfcModelInterface model, IdEObject object, EStructuralFeature feature, EClass eClass,
			QueryInterface query, TodoList todoList) throws IfcModelDbException {
		// TODO next bit seems to make no sense, why detect a deleted record when reading a reference??
		if (buffer.capacity() == 1 && buffer.get(0) == -1) {
			buffer.position(buffer.position() + 1);
			return null;
		}
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		long oid = buffer.getLong();
		buffer.order(ByteOrder.BIG_ENDIAN);
		IdEObject foundInCache = objectCache.get(oid);
		if (foundInCache != null) {
			return foundInCache;
		}
		if (model.contains(oid)) {
			return model.get(oid);
		}
		IdEObjectImpl newObject = createInternal(eClass, query);
		newObject.setOid(oid);
		if (perRecordVersioning(newObject)) {
			newObject.setPid(STORE_PROJECT_ID);
		} else {
			newObject.setPid(query.getPid());
		}
		newObject.setRid(query.getRid());
		try {
			newObject.setModel(model);
		} catch (IfcModelInterfaceException e) {
			LOGGER.error("", e);
		}
		objectCache.put(oid, newObject);
		if (query.isDeep() && object.eClass().getEAnnotation("wrapped") == null) {
//			if (feature.getEAnnotation("nolazyload") == null) {
				todoList.put(oid, newObject);
//			}
		} else {
			if (object.eClass().getEAnnotation("wrapped") == null) {
				try {
					model.addAllowMultiModel(oid, newObject);
				} catch (IfcModelInterfaceException e) {
					throw new IfcModelDbException(e);
				}
			}
		}
		return newObject;
	}
	
	@SuppressWarnings("unchecked")
	protected Object readList(IdEObject idEObject, ByteBuffer buffer, IfcModelInterface model, QueryInterface query, TodoList todoList,
			EStructuralFeature feature) throws IfcModelDbException {
		if (feature.getEType() instanceof EEnum) {
		} else if (feature.getEType() instanceof EClass) {
			if (buffer.capacity() == 1 && buffer.get(0) == -1) {
				buffer.position(buffer.position() + 1);
			} else {
				/*
				 * TODO There still is a problem with this,
				 * when readReference (and all calls beyond
				 * that call) alter (by opposites) this
				 * list, this list can potentially grow too
				 * large
				 * 
				 * Only can happen with non-unique
				 * references
				 */
				int listSize = buffer.getInt();

				AbstractEList<Object> list = (AbstractEList<Object>) idEObject.eGet(feature);
				for (int i = 0; i < listSize; i++) {
					if (feature.getEAnnotation("twodimensionalarray") != null) {
						IdEObjectImpl newObject = createInternal((EClass) feature.getEType(), query);
						Object result = readList(newObject, buffer, model, query, todoList, newObject.eClass().getEStructuralFeature("List"));
						if (result != null) {
							newObject.eSet(newObject.eClass().getEStructuralFeature("List"), result);
						}
						list.addUnique(newObject);
					} else {
						IdEObject referencedObject = null;
						
						buffer.order(ByteOrder.LITTLE_ENDIAN);
						short cid = buffer.getShort();
						buffer.order(ByteOrder.BIG_ENDIAN);
						if (cid == -1) {
							// null, do nothing
						} else if (cid < 0) {
							// negative cid means value is
							// embedded
							// in record
							EClass referenceClass = ifcDataBase.getEClassForCid((short) (-cid));
							if (referenceClass == null) {
								throw new IfcModelDbException("No class found for cid " + (-cid));
							}
							referencedObject = readWrappedValue(feature, buffer, referenceClass, query);
						} else if (cid > 0) {
							// positive cid means value is a
							// reference
							// to another record
							EClass referenceClass = ifcDataBase.getEClassForCid(cid);
							if (referenceClass == null) {
								throw new IfcModelDbException("Cannot find class with cid " + cid);
							}
							buffer.position(buffer.position() - 2);
							referencedObject = readReference(buffer, model, idEObject, feature, referenceClass, query, todoList);
						}
						if (referencedObject != null) {
							if (!feature.getEType().isInstance(referencedObject)) {
								throw new IfcModelDbException(referencedObject.getClass().getSimpleName() + " cannot be stored in list of type "
										+ feature.getEType().getName() + " for feature " + feature.getName());
							}
							if (feature.isUnique()) {
								list.add(referencedObject);
							} else {
								list.addUnique(referencedObject);
							}
						}
					}
				}
			}
		} else if (feature.getEType() instanceof EDataType) {
			int listSize = buffer.getInt();
			BasicEList<Object> list = new BasicEList<Object>(listSize);
			for (int i = 0; i < listSize; i++) {
				Object reference = readPrimitiveValue(feature.getEType(), buffer, query);
				if (reference != null) {
					list.addUnique(reference);
				}
			}
			return list;
		}
		return null;
	}
	
	protected IdEObject readEmbeddedValue(EStructuralFeature feature, ByteBuffer buffer, EClass eClass, QueryInterface query) {
		IdEObject eObject = createInternal(eClass, query);
		for (EStructuralFeature eStructuralFeature : eClass.getEAllStructuralFeatures()) {
			if (eStructuralFeature.isMany()) {
				// Not implemented
			} else {
				Object primitiveValue = readPrimitiveValue(eStructuralFeature.getEType(), buffer, query);
				((IdEObjectImpl) eObject).setLoaded();
				eObject.eSet(eStructuralFeature, primitiveValue);
			}
		}
		return eObject;
	}
	
	protected IdEObject readWrappedValue(EStructuralFeature feature, ByteBuffer buffer, EClass eClass, QueryInterface query) {
		EStructuralFeature eStructuralFeature = eClass.getEStructuralFeature("wrappedValue");
		Object primitiveValue = readPrimitiveValue(eStructuralFeature.getEType(), buffer, query);
		IdEObject eObject = createInternal(eClass, query);
		((IdEObjectImpl) eObject).setLoaded(); // We don't want to go lazy load
												// this
		eObject.eSet(eStructuralFeature, primitiveValue);
		if (eStructuralFeature.getEType() == EcorePackage.eINSTANCE.getEDouble() || eStructuralFeature.getEType() == EcorePackage.eINSTANCE.getEDoubleObject()) {
			EStructuralFeature strFeature = eClass.getEStructuralFeature("wrappedValueAsString");
			Object stringVal = readPrimitiveValue(EcorePackage.eINSTANCE.getEString(), buffer, query);
			eObject.eSet(strFeature, stringVal);
		}
		return eObject;
	}
	
	public void fakeRead(ByteBuffer buffer, EStructuralFeature feature) throws IfcModelDbException {
		boolean wrappedValue = feature.getEType().getEAnnotation("wrapped") != null;
		if (feature.isMany()) {
			if (feature.getEType() instanceof EEnum) {
			} else if (feature.getEType() instanceof EClass) {
				if (buffer.capacity() == 1 && buffer.get(0) == -1) {
					buffer.position(buffer.position() + 1);
				} else {
					int listSize = buffer.getInt();
					for (int i = 0; i < listSize; i++) {
						buffer.order(ByteOrder.LITTLE_ENDIAN);
						short cid = buffer.getShort();
						buffer.order(ByteOrder.BIG_ENDIAN);
						if (cid != -1) {
							if (wrappedValue) {
								EClass eClass = (EClass) feature.getEType();
								fakePrimitiveRead(eClass.getEStructuralFeature("wrappedValue").getEType(), buffer);
							} else {
								buffer.position(buffer.position() + 6);
							}
						}
					}
				}
			} else if (feature.getEType() instanceof EDataType) {
				int listSize = buffer.getInt();
				for (int i = 0; i < listSize; i++) {
					fakePrimitiveRead(feature.getEType(), buffer);
				}
			}
		} else {
			if (feature.getEType() instanceof EEnum) {
				buffer.position(buffer.position() + 4);
			} else if (feature.getEType() instanceof EClass) {
				if (buffer.capacity() == 1 && buffer.get(0) == -1) {
					buffer.position(buffer.position() + 1);
				} else {
					buffer.order(ByteOrder.LITTLE_ENDIAN);
					short cid = buffer.getShort();
					buffer.order(ByteOrder.BIG_ENDIAN);
					if (wrappedValue) {
						fakePrimitiveRead(feature.getEType(), buffer);
					} else {
						if (cid != -1) {
							buffer.position(buffer.position() + 6);
						}
					}
				}
			} else if (feature.getEType() instanceof EDataType) {
				fakePrimitiveRead(feature.getEType(), buffer);
			}
		}
	}
	
	protected void fakePrimitiveRead(EClassifier classifier, ByteBuffer buffer) throws IfcModelDbException {
		if (classifier == EcorePackage.eINSTANCE.getEString()) {
			int length = buffer.getInt();
			if (length != -1) {
				buffer.position(buffer.position() + length);
			}
		} else if (classifier == EcorePackage.eINSTANCE.getEInt() || classifier == EcorePackage.eINSTANCE.getEIntegerObject()) {
			buffer.position(buffer.position() + 4);
		} else if (classifier == EcorePackage.eINSTANCE.getELong() || classifier == EcorePackage.eINSTANCE.getELongObject()) {
			buffer.position(buffer.position() + 8);
		} else if (classifier == EcorePackage.eINSTANCE.getEFloat() || classifier == EcorePackage.eINSTANCE.getEFloatObject()) {
			buffer.position(buffer.position() + 4);
		} else if (classifier == EcorePackage.eINSTANCE.getEDouble() || classifier == EcorePackage.eINSTANCE.getEDoubleObject()) {
			buffer.position(buffer.position() + 8);
		} else if (classifier == EcorePackage.eINSTANCE.getEBoolean() || classifier == EcorePackage.eINSTANCE.getEBooleanObject()) {
			buffer.position(buffer.position() + 1);
		} else if (classifier == EcorePackage.eINSTANCE.getEDate()) {
			buffer.position(buffer.position() + 8);
		} else if (classifier == EcorePackage.eINSTANCE.getEByteArray()) {
			int length = buffer.getInt();
			if (length != -1) {
				buffer.position(buffer.position() + length);
			}
		} else {
			throw new IfcModelDbException("Unimplemented " + classifier);
		}
	}
	
	public Object readPrimitiveValue(EClassifier classifier, ByteBuffer buffer, QueryInterface query) {
		if (classifier == EcorePackage.eINSTANCE.getEString()) {
			int length = buffer.getInt();
			if (length != -1) {
				return BinUtils.readString(buffer, length);
			} else {
				return null;
			}
		} else if (classifier == EcorePackage.eINSTANCE.getEInt() || classifier == EcorePackage.eINSTANCE.getEIntegerObject()) {
			return buffer.getInt();
		} else if (classifier == EcorePackage.eINSTANCE.getELong() || classifier == EcorePackage.eINSTANCE.getELongObject()) {
			return buffer.getLong();
		} else if (classifier == EcorePackage.eINSTANCE.getEFloat() || classifier == EcorePackage.eINSTANCE.getEFloatObject()) {
			return buffer.getFloat();
		} else if (classifier == EcorePackage.eINSTANCE.getEDouble() || classifier == EcorePackage.eINSTANCE.getEDoubleObject()) {
			return buffer.getDouble();
		} else if (classifier == EcorePackage.eINSTANCE.getEBoolean() || classifier == EcorePackage.eINSTANCE.getEBooleanObject()) {
			return buffer.get() == 1;
		} else if (classifier == EcorePackage.eINSTANCE.getEDate()) {
			long val = buffer.getLong();
			if (val == -1L) {
				return null;
			}
			return new Date(val);
		} else if (classifier == EcorePackage.eINSTANCE.getEByteArray()) {
			int size = buffer.getInt();
			byte[] result = new byte[size];
			buffer.get(result);
			return result;
		} else if (classifier.getName().equals("Tristate")) {
			int ordinal = buffer.getInt();
			EEnum tristateEnum = query.getPackageMetaData().getEEnum("Tristate");
			return tristateEnum.getEEnumLiteral(ordinal).getInstance();
		} else if (classifier instanceof EEnum) {
			int ordinal = buffer.getInt();
			EEnum eEnum = (EEnum) classifier;
			return eEnum.getEEnumLiteral(ordinal).getInstance();
		} else {
			throw new RuntimeException("Unsupported type " + classifier.getName());
		}
	}
	
	public byte[] readPrimitiveBytes(EClassifier classifier, ByteBuffer buffer, QueryInterface query) {
		if (classifier == EcorePackage.eINSTANCE.getEString()) {
			int length = buffer.getInt();
			if (length != -1) {
				byte[] result = new byte[length];
				buffer.get(result, 0, length);
				return result;
			} else {
				return null;
			}
		} else if (classifier == EcorePackage.eINSTANCE.getEInt() || classifier == EcorePackage.eINSTANCE.getEIntegerObject()) {
			byte[] result = new byte[4];
			buffer.get(result, 0, 4);
			return result;
		} else if (classifier == EcorePackage.eINSTANCE.getELong() || classifier == EcorePackage.eINSTANCE.getELongObject()) {
			byte[] result = new byte[8];
			buffer.get(result, 0, 8);
			return result;
		} else if (classifier == EcorePackage.eINSTANCE.getEFloat() || classifier == EcorePackage.eINSTANCE.getEFloatObject()) {
			byte[] result = new byte[4];
			buffer.get(result, 0, 4);
			return result;
		} else if (classifier == EcorePackage.eINSTANCE.getEDouble() || classifier == EcorePackage.eINSTANCE.getEDoubleObject()) {
			byte[] result = new byte[8];
			buffer.get(result, 0, 8);
			return result;
		} else if (classifier == EcorePackage.eINSTANCE.getEBoolean() || classifier == EcorePackage.eINSTANCE.getEBooleanObject()) {
			byte[] result = new byte[1];
			buffer.get(result, 0, 1);
			return result;
		} else if (classifier == EcorePackage.eINSTANCE.getEDate()) {
			byte[] result = new byte[8];
			buffer.get(result, 0, 8);
			return result;
		} else if (classifier == EcorePackage.eINSTANCE.getEByteArray()) {
			int size = buffer.getInt();
			byte[] result = new byte[size];
			buffer.get(result);
			return result;
		} else {
			throw new RuntimeException("Unsupported type " + classifier.getName());
		}
	}
	
	protected void writeEmbeddedValue(int pid, int rid, Object value, ByteBuffer buffer, PackageMetaData packageMetaData) throws IfcModelDbException {
		IdEObject wrappedValue = (IdEObject) value;

		Short cid = ifcDataBase.getCidOfEClass(wrappedValue.eClass());
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putShort((short) -cid);
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		for (EStructuralFeature eStructuralFeature : wrappedValue.eClass().getEAllStructuralFeatures()) {
			if (eStructuralFeature.isMany()) {
				writeList(wrappedValue, buffer, packageMetaData, eStructuralFeature);
			} else {
				writePrimitiveValue(eStructuralFeature, wrappedValue.eGet(eStructuralFeature), buffer);
			}
		}
	}
	
	protected void writeList(IdEObject object, ByteBuffer buffer, PackageMetaData packageMetaData, EStructuralFeature feature) throws IfcModelDbException {
		if (feature.getEType() instanceof EEnum) {
			// Aggregate relations to enums never occur... at this
			// moment
		} else if (feature.getEType() instanceof EClass) {
			EList<?> list = (EList<?>) object.eGet(feature);
			buffer.putInt(list.size());
			for (Object o : list) {
				if (o == null) {
					buffer.order(ByteOrder.LITTLE_ENDIAN);
					buffer.putShort((short) -1);
					buffer.order(ByteOrder.BIG_ENDIAN);
				} else {
					IdEObject listObject = (IdEObject) o;
					if (listObject.eClass().getEAnnotation("wrapped") != null || listObject.eClass().getEStructuralFeature("wrappedValue") != null) {
						writeWrappedValue(object.getPid(), object.getRid(), listObject, buffer, packageMetaData);
					} else if (feature.getEAnnotation("twodimensionalarray") != null) {
						EStructuralFeature lf = listObject.eClass().getEStructuralFeature("List");
						writeList(listObject, buffer, packageMetaData, lf);
					} else {
						writeReference(object, listObject, buffer, feature);
					}
				}
			}
		} else if (feature.getEType() instanceof EDataType) {
			EList<?> list = (EList<?>) object.eGet(feature);
			buffer.putInt(list.size());
			for (Object o : list) {
				writePrimitiveValue(feature, o, buffer);
			}
		}
	}
	
	protected void writePrimitiveValue(EStructuralFeature feature, Object value, ByteBuffer buffer) throws IfcModelDbException {
		EClassifier type = feature.getEType();
		if (type == EcorePackage.eINSTANCE.getEString()) {
			if (value == null) {
				buffer.putInt(-1);
			} else {
				String stringValue = (String) value;
				byte[] bytes = stringValue.getBytes(Charsets.UTF_8);
				if (bytes.length > Integer.MAX_VALUE) {
					throw new IfcModelDbException("String value too long (max length is " + Integer.MAX_VALUE + ")");
				}
				buffer.putInt(bytes.length);
				buffer.put(bytes);
			}
		} else if (type == EcorePackage.eINSTANCE.getEInt() || type == EcorePackage.eINSTANCE.getEIntegerObject()) {
			if (value == null) {
				buffer.putInt(0);
			} else {
				buffer.putInt((Integer) value);
			}
		} else if (type == EcorePackage.eINSTANCE.getEDouble() || type == EcorePackage.eINSTANCE.getEDoubleObject()) {
			if (value == null) {
				buffer.putDouble(0D);
			} else {
				buffer.putDouble((Double) value);
			}
		} else if (type == EcorePackage.eINSTANCE.getEFloat() || type == EcorePackage.eINSTANCE.getEFloatObject()) {
			if (value == null) {
				buffer.putFloat(0F);
			} else {
				buffer.putFloat((Float) value);
			}
		} else if (type == EcorePackage.eINSTANCE.getELong() || type == EcorePackage.eINSTANCE.getELongObject()) {
			if (value == null) {
				buffer.putLong(0L);
			} else {
				buffer.putLong((Long) value);
			}
		} else if (type == EcorePackage.eINSTANCE.getEBoolean() || type == EcorePackage.eINSTANCE.getEBooleanObject()) {
			if (value == null) {
				buffer.put((byte) 0);
			} else {
				buffer.put(((Boolean) value) ? (byte) 1 : (byte) 0);
			}
		} else if (type == EcorePackage.eINSTANCE.getEDate()) {
			if (value == null) {
				buffer.putLong(-1L);
			} else {
				buffer.putLong(((Date) value).getTime());
			}
		} else if (type.getName().equals("Tristate")) {
			Enumerator eEnumLiteral = (Enumerator) value;
			buffer.putInt(eEnumLiteral.getValue());
		} else if (value instanceof Enumerator) {
			Enumerator eEnumLiteral = (Enumerator) value;
			buffer.putInt(eEnumLiteral.getValue());
		} else if (type == EcorePackage.eINSTANCE.getEByteArray()) {
			if (value == null) {
				buffer.putInt(0);
			} else {
				byte[] bytes = (byte[]) value;
				buffer.putInt(bytes.length);
				buffer.put(bytes);
			}
		} else {
			throw new RuntimeException("Unsupported type " + type.getName());
		}
	}
	
	protected void writeReference(IdEObject object, Object value, ByteBuffer buffer, EStructuralFeature feature) throws IfcModelDbException {
		IdEObject idEObject = (IdEObject) value;
		if (idEObject.getOid() < 0) {
			throw new IfcModelDbException("Writing a reference with oid " + idEObject.getOid() + ", this is not supposed to happen, referenced: " + idEObject.getOid() + " " + value + " from " + object.getOid() + " " + object);
		}
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putLong(idEObject.getOid());
		buffer.order(ByteOrder.BIG_ENDIAN);
	}
	
	protected void writeWrappedValue(int pid, int rid, Object value, ByteBuffer buffer, PackageMetaData packageMetaData) throws IfcModelDbException {
		IdEObject wrappedValue = (IdEObject) value;
		EStructuralFeature eStructuralFeature = wrappedValue.eClass().getEStructuralFeature("wrappedValue");
		Short cid = ifcDataBase.getCidOfEClass(wrappedValue.eClass());
		
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.putShort((short) -cid);
		buffer.order(ByteOrder.BIG_ENDIAN);
		
		writePrimitiveValue(eStructuralFeature, wrappedValue.eGet(eStructuralFeature), buffer);
		if (eStructuralFeature.getEType() == EcorePackage.eINSTANCE.getEDouble() || eStructuralFeature.getEType() == EcorePackage.eINSTANCE.getEDoubleObject()) {
			EStructuralFeature fe = wrappedValue.eClass().getEStructuralFeature("wrappedValueAsString");
			writePrimitiveValue(fe, wrappedValue.eGet(fe), buffer);
		}
		if (wrappedValue.eClass().getName().equals("IfcGloballyUniqueId")) {
			EClass eClass = packageMetaData.getEClass("IfcGloballyUniqueId");
			if (wrappedValue.getOid() == -1) {//TODO
				((IdEObjectImpl) wrappedValue).setOid(ifcDataBase.newOid(eClass));
			}
			ByteBuffer valueBuffer = convertObjectToByteArray(wrappedValue, ByteBuffer.allocate(getExactSize(wrappedValue, packageMetaData, true)), packageMetaData);
			ByteBuffer keyBuffer = createKeyBuffer(pid, wrappedValue.getOid(), rid);
//			try {
//				database.getKeyValueStore().storeNoOverwrite(eClass.getEPackage().getName() + "_" + eClass.getName(),
//						keyBuffer.array(), valueBuffer.array(), this);
//				database.incrementCommittedWrites(1);
//			} catch (BimserverLockConflictException e) {
//				LOGGER.error("", e);
//			}
		}
	}
	
	protected int getExactSize(IdEObject idEObject, PackageMetaData packageMetaData, boolean useUnsetBits) {
		int size = 0;
		int bits = 0;
		
		if (idEObject.getExpressId() == 15723) {
			System.out.println();
		}
		
		for (EStructuralFeature eStructuralFeature : idEObject.eClass().getEAllStructuralFeatures()) {
			if (packageMetaData.useForDatabaseStorage(idEObject.eClass(), eStructuralFeature)) {
				bits++;
				if (!useUnsetBits || !useUnsetBit(eStructuralFeature, idEObject)) {
					Object val = idEObject.eGet(eStructuralFeature);
					if (eStructuralFeature instanceof EAttribute) {
						EAttribute eAttribute = (EAttribute) eStructuralFeature;
						if (eAttribute.isMany()) {
							size += 4;
							for (Object v : ((List<?>) val)) {
								size += getPrimitiveSize(eAttribute.getEAttributeType(), v);
							}
						} else {
							size += getPrimitiveSize(eAttribute.getEAttributeType(), val);
						}
					} else if (eStructuralFeature instanceof EReference) {
						EReference eReference = (EReference) eStructuralFeature;
						if (eReference.isMany()) {
							size += 4;
							for (Object v : ((List<?>) val)) {
								size += getWrappedValueSize(v, eReference, packageMetaData);
							}
						} else {
							size += getWrappedValueSize(val, eReference, packageMetaData);
						}
					}
				}
			}
		}

		if (useUnsetBits) {
			size += (int) Math.ceil(bits / 8.0);
		}
		return size;
	}
	
	protected boolean useUnsetBit(EStructuralFeature feature, IdEObject object) {
		// TODO non-unsettable boolean values can also be stored in these bits
		Object value = object.eGet(feature);
		if (feature.isUnsettable()) {
			if (!object.eIsSet(feature)) {
				return true;
			}
			if (feature.isMany() && ((List<?>)value).isEmpty()) {
				return true;
			}
		} else {
			if (feature.isMany() && ((List<?>)value).isEmpty()) {
				return true;
			}
			if (feature.getDefaultValue() == value || (feature.getDefaultValue() != null && feature.getDefaultValue().equals(value))) {
				return true;
			}
		}
		return false;
	}
	
	protected int getPrimitiveSize(EDataType eDataType, Object val) {
		if (eDataType == EcorePackage.eINSTANCE.getEInt() || eDataType == EcorePackage.eINSTANCE.getEIntegerObject()) {
			return 4;
		} else if (eDataType == EcorePackage.eINSTANCE.getEFloat() || eDataType == EcorePackage.eINSTANCE.getEFloatObject()) {
			return 4;
		} else if (eDataType == EcorePackage.eINSTANCE.getEBoolean() || eDataType == EcorePackage.eINSTANCE.getEBooleanObject()) {
			return 1;
		} else if (eDataType == EcorePackage.eINSTANCE.getEDate()) {
			return 8;
		} else if (eDataType == EcorePackage.eINSTANCE.getELong() || eDataType == EcorePackage.eINSTANCE.getELongObject()) {
			return 8;
		} else if (eDataType == EcorePackage.eINSTANCE.getEDouble() || eDataType == EcorePackage.eINSTANCE.getEDoubleObject()) {
			return 8;
		} else if (eDataType == EcorePackage.eINSTANCE.getEString()) {
			if (val != null) {
				return 4 + ((String) val).getBytes(Charsets.UTF_8).length;
			}
			return 4;
		} else if (eDataType == EcorePackage.eINSTANCE.getEByteArray()) {
			if (val != null) {
				return 4 + ((byte[]) val).length;
			}
			return 4;
		} else if (eDataType instanceof EEnum) {
			return 4;
		}
		throw new RuntimeException("Unimplemented: " + eDataType);
	}
	
	protected int getWrappedValueSize(Object val, EReference eReference, PackageMetaData packageMetaData) {
		if (val == null) {
			return 2;
		}
		if (val instanceof EObject) {
			IdEObject eObject = (IdEObject) val;
			if (eReference.getEAnnotation("twodimensionalarray") != null) {
				int refSize = 4;
				EStructuralFeature eStructuralFeature = eObject.eClass().getEStructuralFeature("List");
				List<?> l = (List<?>)eObject.eGet(eStructuralFeature);
				for (Object o : l) {
					if (o instanceof EObject) {//linfujun: i changed here
						IdEObject subEObject = (IdEObject) o;
						if (subEObject.eClass().getEAnnotation("wrapped") != null) {
							IdEObject wrappedValue = (IdEObject) subEObject;
							EStructuralFeature wrappedValueFeature = wrappedValue.eClass().getEStructuralFeature("wrappedValue");
							Object wrappedVal = subEObject.eGet(wrappedValueFeature);
							refSize += 2 + getPrimitiveSize((EDataType) wrappedValueFeature.getEType(), wrappedVal);
							if (wrappedValueFeature.getEType() == EcorePackage.eINSTANCE.getEDouble() || wrappedValueFeature.getEType() == EcorePackage.eINSTANCE.getEDoubleObject()) {
								EStructuralFeature wrappedStringFeature = wrappedValue.eClass().getEStructuralFeature("wrappedValueAsString");
								String str = (String) subEObject.eGet(wrappedStringFeature);
								refSize += getPrimitiveSize(EcorePackage.eINSTANCE.getEString(), str);
							}
						} else {
							refSize += 8;
						}
//						refSize += 8;
					} else {
						refSize += getPrimitiveSize((EDataType) eStructuralFeature.getEType(), o);
					}
				}
				return refSize;
			} else if (eReference.getEAnnotation("dbembed") != null) {
				int refSize = 2;
				refSize += getExactSize(eObject, packageMetaData, false);
				return refSize;
			} else if (eObject.eClass().getEAnnotation("wrapped") != null) {
				IdEObject wrappedValue = (IdEObject) val;
				EStructuralFeature wrappedValueFeature = wrappedValue.eClass().getEStructuralFeature("wrappedValue");
				Object wrappedVal = eObject.eGet(wrappedValueFeature);
				int refSize = 2 + getPrimitiveSize((EDataType) wrappedValueFeature.getEType(), wrappedVal);
				if (wrappedValueFeature.getEType() == EcorePackage.eINSTANCE.getEDouble() || wrappedValueFeature.getEType() == EcorePackage.eINSTANCE.getEDoubleObject()) {
					EStructuralFeature wrappedStringFeature = wrappedValue.eClass().getEStructuralFeature("wrappedValueAsString");
					String str = (String) eObject.eGet(wrappedStringFeature);
					refSize += getPrimitiveSize(EcorePackage.eINSTANCE.getEString(), str);
				}
				return refSize;
			} else {
				return 8;
			}
		} else {
			throw new RuntimeException("Programming error, cannot happen");
		}
	}
	
	protected IdEObjectImpl createInternal(EClass eClass, QueryInterface queryInterface) {
		IdEObjectImpl object = (IdEObjectImpl) eClass.getEPackage().getEFactoryInstance().create(eClass);
		object.setQueryInterface(queryInterface);
		return object;
	}
	
	public EClass getEClassForOid(long oid) throws IfcModelDbException {
		return ifcDataBase.getEClassForOid(oid);
	}
	
	@SuppressWarnings("unused")
	protected ByteBuffer fillKeyBuffer(ByteBuffer buffer, IdEObject object) {
		if (object.getRid() > 100000 || object.getRid() < -100000) {
			LOGGER.debug("Improbable rid: " + object.getRid() + " - " + object);
		}
		return fillKeyBuffer(buffer, object.getPid(), object.getOid(), object.getRid());
	}

	protected ByteBuffer fillKeyBuffer(ByteBuffer buffer, int pid, long oid, int rid) {
		buffer.position(0);
		buffer.putInt(pid);
		buffer.putLong(oid);
		buffer.putInt(-rid);
		return buffer;
	}

	protected ByteBuffer createKeyBuffer(int pid, long oid, int rid) {
		ByteBuffer keyBuffer = ByteBuffer.allocate(16);
		fillKeyBuffer(keyBuffer, pid, oid, rid);
		return keyBuffer;
	}

	protected ByteBuffer createKeyBuffer(int pid, long oid) {
		ByteBuffer keyBuffer = ByteBuffer.allocate(12);
		fillKeyBuffer(keyBuffer, pid, oid);
		return keyBuffer;
	}

	protected ByteBuffer fillKeyBuffer(ByteBuffer buffer, int pid, long oid) {
		buffer.position(0);
		buffer.putInt(pid);
		buffer.putLong(oid);
		return buffer;
	}
	
	public boolean perRecordVersioning(IdEObject idEObject) {
		return perRecordVersioning(idEObject.eClass());
	}

	public static boolean perRecordVersioning(EClass eClass) {
		return eClass.getEPackage() != Ifc2x3tc1Package.eINSTANCE && eClass.getEPackage() != Ifc4Package.eINSTANCE && eClass.getEPackage() != GeometryPackage.eINSTANCE;
	}
	
}
