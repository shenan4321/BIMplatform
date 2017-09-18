package cn.dlb.bim.ifc.stream.deserializers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EClassImpl;
import org.eclipse.emf.ecore.impl.EEnumImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.deserializers.DeserializeException;
import cn.dlb.bim.ifc.deserializers.IfcHeaderParser;
import cn.dlb.bim.ifc.deserializers.IfcParserWriterUtils;
import cn.dlb.bim.ifc.emf.MetaDataException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.Schema;
import cn.dlb.bim.ifc.model.IfcHeader;
import cn.dlb.bim.ifc.shared.ByteProgressReporter;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.WrappedVirtualObject;
import cn.dlb.bim.service.CatalogService;
import cn.dlb.bim.service.VirtualObjectService;
import cn.dlb.bim.utils.FakeClosingInputStream;
import cn.dlb.bim.utils.StringUtils;
import nl.tue.buildingsmart.schema.Attribute;
import nl.tue.buildingsmart.schema.EntityDefinition;
import nl.tue.buildingsmart.schema.ExplicitAttribute;

public class IfcStepStreamingDeserializer implements StreamingDeserializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(IfcStepStreamingDeserializer.class);
	
	private ByteProgressReporter byteProgressReporter;
	private PackageMetaData packageMetaData;
	private static final String WRAPPED_VALUE = "wrappedValue";
	private WaitingListVirtualObject<Integer> waitingList;
	private Mode mode = Mode.HEADER;
	private int lineNumber;
	private Schema schema;
	
	// ExpressID -> ObjectID
	// TODO find more efficient implementation
	private final Map<Integer, Long> mappedObjects = new HashMap<>();
	private IfcHeader ifcHeader;
	private CatalogService catalogService;
	private VirtualObjectService virtualObjectService;
	private Integer rid;
	private Collection<VirtualObject> virtualObjectsToSave;
	private static final Integer batchSaveSize = 1000;
	
//	private static MetricCollector metricCollector = new MetricCollector();
	
	// Use String instead of EClass, compare takes 1.7%
	private final Map<String, AtomicInteger> summaryMap = new TreeMap<>();

	@Override
	public void init(PackageMetaData packageMetaData, CatalogService catalogService, VirtualObjectService virtualObjectService) {
		this.packageMetaData = packageMetaData;
		this.catalogService = catalogService;
		this.virtualObjectService = virtualObjectService;
		waitingList = new WaitingListVirtualObject<Integer>(this);
		this.schema = packageMetaData.getSchema();
		this.rid = catalogService.newRevisionId();
		virtualObjectsToSave = Collections.synchronizedList(new ArrayList<>());
	}
	
	@Override
	public Map<EClass, Integer> getSummaryMap() {
		Map<EClass, Integer> newMap = new TreeMap<>(new Comparator<EClass>(){
			@Override
			public int compare(EClass o1, EClass o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (String key : this.summaryMap.keySet()) {
			EClass eClass = packageMetaData.getEClass(key);
			newMap.put(eClass, this.summaryMap.get(key).get());
		}
		return newMap;
	}
	
	@Override
	public void setProgressReporter(ByteProgressReporter byteProgressReporter) {
		this.byteProgressReporter = byteProgressReporter;
	}
	
	public enum Mode {
		HEADER, DATA, FOOTER, DONE
	}

	public PackageMetaData getPackageMetaData() {
		return packageMetaData;
	}
	
	@Override
	public long read(File sourceFile) throws DeserializeException {
		try {
			FileInputStream in = new FileInputStream(sourceFile);
			long size = read(in, sourceFile.length());
			in.close();
			return size;
		} catch (FileNotFoundException e) {
			throw new DeserializeException(lineNumber, e);
		} catch (IOException e) {
			throw new DeserializeException(lineNumber, e);
		}
	}
	
	@Override
	public long read(InputStream in, String filename, long fileSize) throws DeserializeException {
		mode = Mode.HEADER;
		if (filename != null && (filename.toUpperCase().endsWith(".ZIP") || filename.toUpperCase().endsWith(".IFCZIP"))) {
			ZipInputStream zipInputStream = new ZipInputStream(in);
			try {
				ZipEntry nextEntry = zipInputStream.getNextEntry();
				if (nextEntry == null) {
					throw new DeserializeException("Zip files must contain exactly one IFC-file, this zip-file looks empty");
				}
				if (nextEntry.getName().toUpperCase().endsWith(".IFC")) {
					FakeClosingInputStream fakeClosingInputStream = new FakeClosingInputStream(zipInputStream);
					long size = read(fakeClosingInputStream, fileSize);
					if (size == 0) {
						throw new DeserializeException("Uploaded file does not seem to be a correct IFC file");
					}
					if (zipInputStream.getNextEntry() != null) {
						zipInputStream.close();
						throw new DeserializeException("Zip files may only contain one IFC-file, this zip-file contains more files");
					} else {
						zipInputStream.close();
					}
					return size;
				} else {
					throw new DeserializeException("Zip files must contain exactly one IFC-file, this zip-file seems to have one or more non-IFC files");
				}
			} catch (IOException e) {
				throw new DeserializeException(e);
			}
		} else {
			return read(in, fileSize);
		}
	}

	private long read(InputStream inputStream, long fileSize) throws DeserializeException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charsets.UTF_8));
		long bytesRead = 0;
		lineNumber = 0;
		try {
			String line = reader.readLine();
			if (line == null) {
				throw new DeserializeException("Unexpected end of stream reading first line");
			}
			MessageDigest md = MessageDigest.getInstance("MD5");
			while (line != null) {
				byte[] bytes = line.getBytes(Charsets.UTF_8);
				md.update(bytes, 0, bytes.length);
				try {
					while (!processLine(line.trim())) {
						String readLine = reader.readLine();
						if (readLine == null) {
							break;
						}
						line += readLine;
						lineNumber++;
					}
				} catch (Exception e) {
					if (e instanceof DeserializeException) {
						throw (DeserializeException)e;
					} else {
						throw new DeserializeException(lineNumber, " (" + e.getMessage() + ") " + line, e);
					}
				}
				bytesRead += bytes.length;
				if (byteProgressReporter != null) {
					byteProgressReporter.progress(bytesRead);
				}

				line = reader.readLine();
				lineNumber++;
			}
//			model.getModelMetaData().setChecksum(md.digest());
			if (mode == Mode.HEADER) {
				throw new DeserializeException(lineNumber, "No valid IFC header found");
			}
			return lineNumber;
		} catch (FileNotFoundException e) {
			throw new DeserializeException(lineNumber, e);
		} catch (IOException e) {
			throw new DeserializeException(lineNumber, e);
		} catch (NoSuchAlgorithmException e) {
			throw new DeserializeException(lineNumber, e);
		}
	}

	private boolean processLine(String line) throws DeserializeException, MetaDataException, DatabaseException {
		switch (mode) {
		case HEADER:
			if (line.length() > 0) {
				if (line.endsWith(";")) {
					processHeader(line);
				} else {
					return false;
				}
			}
			if (line.equals("DATA;")) {
				mode = Mode.DATA;
			}
			break;
		case DATA:
			if (line.equals("ENDSEC;")) {
				mode = Mode.FOOTER;
				virtualObjectService.saveAll(virtualObjectsToSave);
				virtualObjectsToSave.clear();
				try {
					waitingList.dumpIfNotEmpty();
				} catch (WaitingListException e) {
					e.printStackTrace();
				}
			} else {
				if (line.length() > 0 && line.charAt(0) == '#') {
					while (line.endsWith("*/")) {
						line = line.substring(0, line.lastIndexOf("/*")).trim();
					}
					if (line.endsWith(";")) {
						processRecord(line);
					} else {
						return false;
					}
				}
			}
			break;
		case FOOTER:
			if (line.equals("ENDSEC;")) {
				mode = Mode.DONE;
			}
			break;
		case DONE:
		}
		return true;
	}

	public IfcHeader getIfcHeader() {
		return ifcHeader;
	}
	
	private void processHeader(String line) throws DeserializeException {
		try {
			if (ifcHeader == null) {
				ifcHeader = new IfcHeader();
				ifcHeader.setRid(rid);
			}
			if (line.startsWith("FILE_DESCRIPTION")) {
				String filedescription = line.substring("FILE_DESCRIPTION".length()).trim();
				new IfcHeaderParser().parseDescription(filedescription.substring(1, filedescription.length() - 2), ifcHeader);
			} else if (line.startsWith("FILE_NAME")) {
				String filename = line.substring("FILE_NAME".length()).trim();
				new IfcHeaderParser().parseFileName(filename.substring(1, filename.length() - 2), ifcHeader);
			} else if (line.startsWith("FILE_SCHEMA")) {
				String fileschema = line.substring("FILE_SCHEMA".length()).trim();
				new IfcHeaderParser().parseFileSchema(fileschema.substring(1, fileschema.length() - 2), ifcHeader);

				String ifcSchemaVersion = ifcHeader.getIfcSchemaVersion();
				if (!ifcSchemaVersion.toLowerCase().equalsIgnoreCase(schema.getHeaderName().toLowerCase())) {
					throw new DeserializeException(lineNumber, ifcSchemaVersion + " is not supported by this deserializer (" + schema.getHeaderName() + " is)");
				}
				ifcHeader.setIfcSchemaVersion(ifcSchemaVersion);
			} else if (line.startsWith("ENDSEC;")) {
				// Do nothing
			}
		} catch (ParseException e) {
			throw new DeserializeException(lineNumber, e);
		}
	}

	private VirtualObject newVirtualObject(EClass eClass) {
		return new VirtualObject(rid, catalogService.getCidOfEClass(eClass), catalogService.newOid(eClass), eClass);
	}

	private WrappedVirtualObject newWrappedVirtualObject(EClass eClass) {
		return new WrappedVirtualObject(catalogService.getCidOfEClass(eClass), eClass);
	}
	
	public void processRecord(String line) throws DeserializeException, MetaDataException, DatabaseException {
		int equalSignLocation = line.indexOf("=");
		int lastIndexOfSemiColon = line.lastIndexOf(";");
		if (lastIndexOfSemiColon == -1) {
			throw new DeserializeException(lineNumber, "No semicolon found in line");
		}
		int indexOfFirstParen = line.indexOf("(", equalSignLocation);
		if (indexOfFirstParen == -1) {
			throw new DeserializeException(lineNumber, "No left parenthesis found in line");
		}
		int indexOfLastParen = line.lastIndexOf(")", lastIndexOfSemiColon);
		if (indexOfLastParen == -1) {
			throw new DeserializeException(lineNumber, "No right parenthesis found in line");
		}
		int recordNumber = Integer.parseInt(line.substring(1, equalSignLocation).trim());
		String name = line.substring(equalSignLocation + 1, indexOfFirstParen).trim();
		EClass eClass = (EClass) getPackageMetaData().getEClassifierCaseInsensitive(name);

		if (eClass == null) {
			throw new DeserializeException(lineNumber, name + " is not a known entity");
		}
		
		VirtualObject object = newVirtualObject(eClass);
		
		AtomicInteger atomicInteger = summaryMap.get(eClass.getName());
		if (atomicInteger == null) {
			summaryMap.put(eClass.getName(), new AtomicInteger(1));
		} else {
			atomicInteger.incrementAndGet();
		}
		mappedObjects.put(recordNumber, object.getOid());

		boolean openReferences = false;

		if (eClass != null) {
			String realData = line.substring(indexOfFirstParen + 1, indexOfLastParen);
			int lastIndex = 0;
			EntityDefinition entityBN = getPackageMetaData().getSchemaDefinition().getEntityBN(name);
			if (entityBN == null) {
				throw new DeserializeException(lineNumber, "Unknown entity " + name);
			}
			for (EStructuralFeature eStructuralFeature : eClass.getEAllStructuralFeatures()) {
				if (getPackageMetaData().useForSerialization(eClass, eStructuralFeature)) {
					if (getPackageMetaData().useForDatabaseStorage(eClass, eStructuralFeature)) {
						int nextIndex = StringUtils.nextString(realData, lastIndex);
						String val = null;
						try {
							val = realData.substring(lastIndex, nextIndex - 1).trim();
						} catch (Exception e) {
							int expected = 0;
							for (Attribute attribute2 : entityBN.getAttributesCached(true)) {
								if (attribute2 instanceof ExplicitAttribute) {
									expected++;
								}
							}
							throw new DeserializeException(lineNumber, eClass.getName() + " expects " + expected + " fields, but less found (" + e.getMessage() + ")");
						}
						lastIndex = nextIndex;
						char firstChar = val.charAt(0);
						if (firstChar == '$') {
							object.eUnset(eStructuralFeature);
							if (eStructuralFeature.getEType() == EcorePackage.eINSTANCE.getEDouble()) {
								EStructuralFeature doubleStringFeature = eClass.getEStructuralFeature(eStructuralFeature.getName() + "AsString");
								object.eUnset(doubleStringFeature);
							}
						} else if (firstChar == '#') {
							if (!readReference(val, object, eStructuralFeature)) {
								openReferences = true;
							}
						} else if (firstChar == '.') {
							readEnum(val, object, eStructuralFeature);
						} else if (firstChar == '(') {
							if (!readList(val, object, eStructuralFeature)) {
								openReferences = true;
							}
						} else if (firstChar == '*') {
							object.eUnset(eStructuralFeature);
						} else {
							if (!eStructuralFeature.isMany()) {
								object.setAttribute(eStructuralFeature, convert(eStructuralFeature.getEType(), val));
								if (eStructuralFeature.getEType() == EcorePackage.eINSTANCE.getEDouble()) {
									EStructuralFeature doubleStringFeature = eClass.getEStructuralFeature(eStructuralFeature.getName() + "AsString");
									object.setAttribute(doubleStringFeature, val);
								}
							} else {
								// It's not a list in the file, but it is in the
								// schema??
							}
						}
					} else {
						int nextIndex = StringUtils.nextString(realData, lastIndex);
						lastIndex = nextIndex;
					}
				} else {
					if (getPackageMetaData().useForDatabaseStorage(eClass, eStructuralFeature)) {
						if (eStructuralFeature instanceof EReference && getPackageMetaData().isInverse((EReference) eStructuralFeature)) {
							object.eUnset(eStructuralFeature);
						} else {
							if (eStructuralFeature.getEAnnotation("asstring") == null) {
								object.eUnset(eStructuralFeature);
							}
						}
					}
				}
			}
			
			// Other objects waiting for me?
			if (waitingList.containsKey(recordNumber)) {
				waitingList.updateNode(recordNumber, eClass, object);
			}

			if (!openReferences) {
				addVirtualObjectToSave(object);
//				metricCollector.collect(line.length(), nrBytes);
			}
		}
	}
	
	private boolean readList(String val, VirtualObject object, EStructuralFeature structuralFeature) throws DeserializeException, MetaDataException, DatabaseException {
		int index = 0;
		if (!structuralFeature.isMany()) {
			throw new DeserializeException(lineNumber, "Field " + structuralFeature.getName() + " of " + structuralFeature.getEContainingClass().getName() + " is no aggregation");
		}
		boolean isDouble = structuralFeature.getEType() == EcorePackage.eINSTANCE.getEDouble();
		EStructuralFeature doubleStringFeature = null;
		if (isDouble) {
			doubleStringFeature = structuralFeature.getEContainingClass().getEStructuralFeature(structuralFeature.getName() + "AsString");
			if (doubleStringFeature == null) {//linfujun, i change the code here to fix the bug
				doubleStringFeature = structuralFeature.getEContainingClass().getEStructuralFeature(structuralFeature.getFeatureID());
			}
			if (doubleStringFeature == null) {
				throw new DeserializeException(lineNumber, "Field not found: " + structuralFeature.getName() + "AsString");
			}
		}
		String realData = val.substring(1, val.length() - 1);
		int lastIndex = 0;
//		object.startList(structuralFeature);
		// TODO not always instantiate
		List<String> doubles = new ArrayList<>();
		boolean complete = true;
		while (lastIndex != realData.length() + 1) {
			int nextIndex = StringUtils.nextString(realData, lastIndex);
			String stringValue = realData.substring(lastIndex, nextIndex - 1).trim();
			lastIndex = nextIndex;
			if (stringValue.length() > 0) {
				if (stringValue.charAt(0) == '#') {
					Integer referenceId = Integer.parseInt(stringValue.substring(1));
					if (mappedObjects.containsKey(referenceId)) {
						Long referencedOid = mappedObjects.get(referenceId);
						if (referencedOid != null) {
							EClass referenceEClass = catalogService.getEClassForOid(referencedOid);
							if (((EClass) structuralFeature.getEType()).isSuperTypeOf(referenceEClass)) {
								// TODO unique checking?
								object.setListItemReference(structuralFeature, index, referencedOid);
							} else {
								throw new DeserializeException(lineNumber, referenceEClass.getName() + " cannot be stored in " + structuralFeature.getName());
							}
						}
					} else {
//						int pos = object.reserveSpaceForListReference();
						waitingList.add(referenceId, new ListWaitingVirtualObject(lineNumber, object, structuralFeature, index));
						complete = false;
					}
				} else if (stringValue.charAt(0) == '(') {
					// Two dimensional list
					EClass newObjectEClass = (EClass) structuralFeature.getEType();
					VirtualObject newObject = newVirtualObject((EClass) structuralFeature.getEType());
					readList(stringValue, newObject, newObjectEClass.getEStructuralFeature("List"));
					// TODO unique?
					object.setListItemReference(structuralFeature, index, newObject.getOid());
				} else {
					Object convert = convert(structuralFeature.getEType(), stringValue);
					if (convert != null) {
						object.setListItem(structuralFeature, index, convert);
						if (isDouble) {
							doubles.add(stringValue);
						}
					}
				}
			}
			index++;
		}
//		object.endList();
		// TODO make more efficient
		if (isDouble) {
//			object.startList(doubleStringFeature);
			int i=0;
			for (String d : doubles) {
				if (doubleStringFeature.getEType() == EcorePackage.eINSTANCE.getEDouble()) {//linfujun, i change the code here to fix the bug
					object.setListItem(doubleStringFeature, i++, Double.parseDouble(d));
				} else {
					object.setListItem(doubleStringFeature, i++, d);
				}
			}
//			object.endList();
		}
		return complete;
	}

	private Object convert(EClassifier classifier, String value) throws DeserializeException, MetaDataException, DatabaseException {
		if (classifier != null) {
			if (classifier instanceof EClassImpl) {
				if (null != ((EClassImpl) classifier).getEStructuralFeature(WRAPPED_VALUE)) {
					EClass newObjectEClass = (EClass) classifier;
					WrappedVirtualObject newObject = newWrappedVirtualObject(newObjectEClass);
					Class<?> instanceClass = newObjectEClass.getEStructuralFeature(WRAPPED_VALUE).getEType().getInstanceClass();
					if (value.equals("")) {

					} else {
						if (instanceClass == Integer.class || instanceClass == int.class) {
							try {
								newObject.setAttribute(newObjectEClass.getEStructuralFeature(WRAPPED_VALUE), Integer.parseInt(value));
							} catch (NumberFormatException e) {
								throw new DeserializeException(lineNumber, value + " is not a valid integer value");
							}
						} else if (instanceClass == Long.class || instanceClass == long.class) {
							newObject.setAttribute(newObjectEClass.getEStructuralFeature(WRAPPED_VALUE), Long.parseLong(value));
						} else if (instanceClass == Boolean.class || instanceClass == boolean.class) {
							newObject.setAttribute(newObjectEClass.getEStructuralFeature(WRAPPED_VALUE), value.equals(".T."));
						} else if (instanceClass == Double.class || instanceClass == double.class) {
							try {
								newObject.setAttribute(newObjectEClass.getEStructuralFeature(WRAPPED_VALUE), Double.parseDouble(value));
							} catch (NumberFormatException e) {
								throw new DeserializeException(lineNumber, value + " is not a valid double floating point number");
							}
							newObject.setAttribute(newObjectEClass.getEStructuralFeature(WRAPPED_VALUE + "AsString"), value);
						} else if (instanceClass == String.class) {
							newObject.setAttribute(newObjectEClass.getEStructuralFeature(WRAPPED_VALUE), IfcParserWriterUtils.readString(value, lineNumber));
						} else if (instanceClass.getSimpleName().equals("Tristate")) {
							Object tristate = null;
							if (value.equals(".T.")) {
								tristate = Boolean.TRUE;
							} else if (value.equals(".F.")) {
								tristate = Boolean.FALSE;
							} else if (value.equals(".U.")) {
								tristate = null;
							}
							if (tristate != null) {
								newObject.setAttribute(newObjectEClass.getEStructuralFeature(WRAPPED_VALUE), tristate);
							}
						}
					}
					return newObject;
				} else {
					return processInline(classifier, value);
				}
			} else if (classifier instanceof EDataType) {
				return IfcParserWriterUtils.streamConvertSimpleValue(getPackageMetaData(), classifier.getInstanceClass(), value, lineNumber);
			}
		}
		return null;
	}

	private Object processInline(EClassifier classifier, String value) throws DeserializeException, MetaDataException, DatabaseException {
		if (value.indexOf("(") != -1) {
			String typeName = value.substring(0, value.indexOf("(")).trim();
			String v = value.substring(value.indexOf("(") + 1, value.length() - 1);
			EClassifier eClassifier = getPackageMetaData().getEClassifierCaseInsensitive(typeName);
			if (eClassifier instanceof EClass) {
				return convert(eClassifier, v);
			} else {
				throw new DeserializeException(lineNumber, typeName + " is not an existing IFC entity");
			}
		} else {
			return IfcParserWriterUtils.convertSimpleValue(getPackageMetaData(), classifier.getInstanceClass(), value, lineNumber);
		}
	}

	private void readEnum(String val, VirtualObject object, EStructuralFeature structuralFeature) throws DeserializeException, MetaDataException, DatabaseException {
		if (val.equals(".T.")) {
			object.setAttribute(structuralFeature, Boolean.TRUE);
		} else if (val.equals(".F.")) {
			object.setAttribute(structuralFeature, Boolean.FALSE);
		} else if (val.equals(".U.")) {
			object.eUnset(structuralFeature);
		} else {
			if (structuralFeature.getEType() instanceof EEnumImpl) {
				String realEnumValue = val.substring(1, val.length() - 1);
				EEnumLiteral enumValue = (((EEnumImpl) structuralFeature.getEType()).getEEnumLiteral(realEnumValue));
				if (enumValue == null) {
					throw new DeserializeException(lineNumber, "Enum type " + structuralFeature.getEType().getName() + " has no literal value '" + realEnumValue + "'");
				}
				object.setAttribute(structuralFeature, enumValue.getLiteral());
			} else {
				throw new DeserializeException(lineNumber, "Value " + val + " indicates enum type but " + structuralFeature.getEType().getName() + " expected");
			}
		}
	}

	private boolean readReference(String val, VirtualObject object, EStructuralFeature structuralFeature) throws DeserializeException, DatabaseException {
		try {
			int referenceId = Integer.parseInt(val.substring(1));
			if (mappedObjects.containsKey(referenceId)) {
				object.setReference(structuralFeature, mappedObjects.get(referenceId));
				return true;
			} else {
//				int pos = object.reserveSpaceForReference(structuralFeature);
				waitingList.add(referenceId, new WaitingVirtualObject(lineNumber, object, structuralFeature));
				return false;
			}
		} catch (NumberFormatException e) {
			throw new DeserializeException(lineNumber, "'" + val + "' is not a valid reference");
		}
	}

	public Integer getRid() {
		return rid;
	}
	
	public void addVirtualObjectToSave(VirtualObject object) {
		virtualObjectsToSave.add(object);
		if (virtualObjectsToSave.size() >= batchSaveSize) {
			virtualObjectService.saveAll(virtualObjectsToSave);
			virtualObjectsToSave.clear();
		}
	}

}