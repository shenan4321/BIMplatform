package cn.dlb.bim.ifc.stream.serializers;

/******************************************************************************
 * Copyright (C) 2009-2016  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;

import cn.dlb.bim.ifc.database.DatabaseException;
import cn.dlb.bim.ifc.deserializers.IfcParserWriterUtils;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.model.IfcHeader;
import cn.dlb.bim.ifc.serializers.SerializerException;
import cn.dlb.bim.ifc.serializers.SerializerInputstream;
import cn.dlb.bim.ifc.serializers.StreamingReader;
import cn.dlb.bim.ifc.stream.MinimalVirtualObject;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.WrappedVirtualObject;
import cn.dlb.bim.service.PlatformService;
import cn.dlb.bim.utils.StringUtils;
import cn.dlb.bim.utils.UTF8PrintWriter;
import nl.tue.buildingsmart.schema.EntityDefinition;
import nl.tue.buildingsmart.schema.SchemaDefinition;

public abstract class IfcStepStreamingSerializer implements StreamingSerializer, StreamingReader, OidConvertingSerializer {
	private static final EcorePackage ECORE_PACKAGE_INSTANCE = EcorePackage.eINSTANCE;
	private static final String NULL = "NULL";
	private static final String OPEN_CLOSE_PAREN = "()";
	private static final String ASTERISK = "*";
	private static final String PAREN_CLOSE_SEMICOLON = ");";
	private static final String DASH = "#";
	private static final String IFC_LOGICAL = "IfcLogical";
	private static final String IFC_BOOLEAN = "IfcBoolean";
	private static final String DOT = ".";
	private static final String COMMA = ",";
	private static final String OPEN_PAREN = "(";
	private static final String CLOSE_PAREN = ")";
	private static final String BOOLEAN_UNDEFINED = ".U.";
	private static final String DOLLAR = "$";
	private static final String WRAPPED_VALUE = "wrappedValue";
	
	private String headerSchema;
	private ObjectProvider objectProvider;
	
	private Map<Long, Integer> oidToEid = new HashMap<>();
	private int oidCounter = 1;

	protected static enum Mode {
		HEADER, BODY, FOOTER, FINISHED
	}

	private Mode mode = Mode.HEADER;
	private IfcHeader ifcHeader;
	private PackageMetaData packageMetaData;
	private PrintWriter printWriter;
	private PlatformService platformService;
	
	@Override
	public boolean write(OutputStream outputStream) throws SerializerException, DatabaseException {
		if (this.printWriter == null) {
			this.printWriter = new UTF8PrintWriter(outputStream);
		}
		boolean result = false;
		try {
			result = processMode();
		} catch (IOException e) {
			throw new SerializerException(e);
		}
		return result;
	}
	
	public Map<Long, Integer> getOidToEid() {
		return oidToEid;
	}
	
	public Mode getMode() {
		return mode;
	}
	
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	
	@Override
	public InputStream getInputStream() {
		return new SerializerInputstream(this);
	}
	
	public IfcStepStreamingSerializer() {
	}

	protected void setHeaderSchema(String headerSchema) {
		this.headerSchema = headerSchema;
	}

	@Override
	public void init(PlatformService platformService, ObjectProvider objectProvider, IfcHeader ifcHeader, PackageMetaData packageMetaData) throws SerializerException {
		this.platformService = platformService;
		this.objectProvider = objectProvider;
		this.ifcHeader = ifcHeader;
		this.packageMetaData = packageMetaData;
		headerSchema = ifcHeader.getIfcSchemaVersion();
	}
	
	public void writeToOutputStream(OutputStream outputStream) throws SerializerException, DatabaseException {
		this.printWriter = new UTF8PrintWriter(outputStream);
		try {
			while (mode != Mode.FINISHED) {
				processMode();			
			}
		} catch (IOException e) {
			throw new SerializerException(e);
		}
	}

	private boolean processMode() throws IOException, DatabaseException, SerializerException {
		if (getMode() == Mode.HEADER) {
			writeHeader();
			setMode(Mode.BODY);
		} else if (getMode() == Mode.BODY) {
			VirtualObject next = objectProvider.next();
			if (next != null) {
				write(next);
			} else {
				setMode(Mode.FOOTER);
			}
		} else if (getMode() == Mode.FOOTER) {
			writeFooter();
			setMode(Mode.FINISHED);
			if (printWriter != null) {
				printWriter.flush();
			}
		} else if (getMode() == Mode.FINISHED) {
			return false;
		}
		return true;
	}

	private void writeFooter() throws IOException {
		println("ENDSEC;");
		println("END-ISO-10303-21;");
	}

	private void writeHeader() throws IOException {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		println("ISO-10303-21;");
		println("HEADER;");
		if (ifcHeader == null) {
			Date date = new Date();
			println("FILE_DESCRIPTION ((''), '2;1');");
			println("FILE_NAME ('', '" + dateFormatter.format(date) + "', (''), (''), '', 'BIMserver', '');");
			println("FILE_SCHEMA (('" + headerSchema + "'));");
		} else {
			print("FILE_DESCRIPTION ((");
			print(StringUtils.concat(ifcHeader.getDescription(), "'", ", "));
			println("), '" + ifcHeader.getImplementationLevel() + "');");
			println("FILE_NAME ('" + ifcHeader.getFilename() + "', '" + dateFormatter.format(ifcHeader.getTimeStamp()) + "', (" + StringUtils.concat(ifcHeader.getAuthor(), "'", ", ") + "), (" + StringUtils.concat(ifcHeader.getOrganization(), "'", ", ") + "), '" + ifcHeader.getPreProcessorVersion() + "', '" + ifcHeader.getOriginatingSystem() + "', '"	+ ifcHeader.getAuthorization() + "');");

			//	println("FILE_SCHEMA (('" + ifcHeader.getIfcSchemaVersion() + "'));");
			println("FILE_SCHEMA (('" + headerSchema + "'));");
		}
		println("ENDSEC;");
		println("DATA;");
		// println("//This program comes with ABSOLUTELY NO WARRANTY.");
		// println("//This is free software, and you are welcome to redistribute it under certain conditions. See www.bimserver.org <http://www.bimserver.org>");
	}

	private void println(String line) throws IOException {
		printWriter.println(line);
	}

	private void print(String text) throws IOException {
		printWriter.write(text);
	}
	
	private void write(VirtualObject object) throws SerializerException, IOException {
		EClass eClass = platformService.getEClassForCid(object.getEClassId());
		if (eClass.getEAnnotation("hidden") != null) {
			return;
		}
		print(DASH);
		int convertedKey = getExpressId(object);
		if (convertedKey == -1) {
			throw new SerializerException("Going to serialize an object with id -1 (" + eClass.getName() + ")");
		}
		print(String.valueOf(convertedKey));
		print("= ");
		String upperCase = packageMetaData.getUpperCase(eClass);
		if (upperCase == null) {
			throw new SerializerException("Type not found: " + eClass.getName());
		}
		print(upperCase);
		print(OPEN_PAREN);
		boolean isFirst = true;
		
		EntityDefinition entityBN = getSchemaDefinition().getEntityBN(eClass.getName());
		for (EStructuralFeature feature : eClass.getEAllStructuralFeatures()) {
			if (feature.getEAnnotation("hidden") == null && (entityBN != null && (!entityBN.isDerived(feature.getName()) || entityBN.isDerivedOverride(feature.getName())))) {
				EClassifier type = feature.getEType();
				if (type instanceof EEnum) {
					if (!isFirst) {
						print(COMMA);
					}
					writeEnum(object, feature);
					isFirst = false;
				} else if (type instanceof EClass) {
					EReference eReference = (EReference)feature;
					if (!packageMetaData.isInverse(eReference)) {
						if (!isFirst) {
							print(COMMA);
						}
						writeEClass(object, feature);
						isFirst = false;
					}
				} else if (type instanceof EDataType) {
					if (!isFirst) {
						print(COMMA);
					}
					writeEDataType(object, entityBN, feature);
					isFirst = false;
				}
			}
		}
		println(PAREN_CLOSE_SEMICOLON);
	}

	private int getExpressId(VirtualObject object) {
		return getExpressId(object.getOid());
	}

	private int getExpressId(long oid) {
		if (oidToEid.containsKey(oid)) {
			return oidToEid.get(oid);
		} else {
			int eid = oidCounter++;
			oidToEid.put(oid, eid);
			return eid;
		}
	}

	private void writeEDataType(VirtualObject object, EntityDefinition entityBN, EStructuralFeature feature) throws SerializerException, IOException {
		if (entityBN != null && entityBN.isDerived(feature.getName())) {
			print(ASTERISK);
		} else if (feature.isMany()) {
			writeList(object, feature);
		} else {
			writeObject(object, feature);
		}
	}

	private void writeEClass(VirtualObject object, EStructuralFeature feature) throws SerializerException, IOException {
		Object referencedObject = object.eGet(feature);
		if (referencedObject instanceof VirtualObject) {
			EClass referencedObjectEClass = platformService.getEClassForCid(((VirtualObject)referencedObject).getEClassId());
			if (referencedObjectEClass.getEAnnotation("wrapped") != null) {
				writeWrappedValue(object, feature, referencedObjectEClass);
			}
		} else {
			if (referencedObject instanceof Long) {
				print(DASH);
				print(String.valueOf(getExpressId((Long) referencedObject)));
			} else {
				EClass objectEClass = platformService.getEClassForCid(object.getEClassId());
				EntityDefinition entityBN = getSchemaDefinition().getEntityBN(objectEClass.getName());
				if (entityBN != null && entityBN.isDerived(feature.getName())) {
					print(ASTERISK);
				} else if (feature.isMany()) {
					writeList(object, feature);
				} else {
					writeObject(object, feature);
				}
			}
		}
	}

	private void writeObject(VirtualObject object, EStructuralFeature feature) throws SerializerException, IOException {
		Object ref = object.eGet(feature);
		if (ref == null || (feature.isUnsettable() && !object.eIsSet(feature))) {
			EClassifier type = feature.getEType();
			if (type instanceof EClass) {
				EStructuralFeature structuralFeature = ((EClass) type).getEStructuralFeature(WRAPPED_VALUE);
				if (structuralFeature != null) {
					String name = structuralFeature.getEType().getName();
					if (name.equals(IFC_BOOLEAN) || name.equals(IFC_LOGICAL) || structuralFeature.getEType() == EcorePackage.eINSTANCE.getEBoolean()) {
						print(BOOLEAN_UNDEFINED);
					} else {
						print(DOLLAR);
					}
				} else {
					print(DOLLAR);
				}
			} else {
				if (type == EcorePackage.eINSTANCE.getEBoolean()) {
					print(BOOLEAN_UNDEFINED);
				} else if (feature.isMany()) {
					print("()");
				} else {
					print(DOLLAR);
				}
			}
		} else {
			if (ref instanceof WrappedVirtualObject) {
				writeEmbedded((WrappedVirtualObject) ref);
			} else if (feature.getEType() == ECORE_PACKAGE_INSTANCE.getEDouble()) {
				EClass objectEClass = platformService.getEClassForCid(object.getEClassId());
				EStructuralFeature asStringFeature = objectEClass.getEStructuralFeature(feature.getName() + "AsString");
				String asString = (String) object.eGet(asStringFeature);
				writeDoubleValue((Double)ref, asString, feature);
			} else {
				IfcParserWriterUtils.writePrimitive(ref, printWriter);
			}
		}
	}

	private void writeDoubleValue(double value, String asString, EStructuralFeature feature) throws SerializerException, IOException {
		if (asString != null) {
			print((String)asString);
			return;
		}
		IfcParserWriterUtils.writePrimitive(value, printWriter);
	}

	private void writeEmbedded(WrappedVirtualObject eObject) throws SerializerException, IOException {
		EClass class1 = platformService.getEClassForCid(eObject.getEClassId());
		print(packageMetaData.getUpperCase(class1));
		print(OPEN_PAREN);
		EStructuralFeature structuralFeature = class1.getEStructuralFeature(WRAPPED_VALUE);
		if (structuralFeature != null) {
			Object realVal = eObject.eGet(structuralFeature);
			if (structuralFeature.getEType() == ECORE_PACKAGE_INSTANCE.getEDouble()) {
				EStructuralFeature asStringFeature = class1.getEStructuralFeature(structuralFeature.getName() + "AsString");
				String asString = (String) eObject.eGet(asStringFeature);
				writeDoubleValue((Double)realVal, asString, structuralFeature);
			} else {
				IfcParserWriterUtils.writePrimitive(realVal, printWriter);
			}
		}
		print(CLOSE_PAREN);
	}

	private void writeList(MinimalVirtualObject object, EStructuralFeature feature) throws SerializerException, IOException {
		List<?> list = (List<?>) object.eGet(feature);
		if (list == null) {
			if (feature.isUnsettable()) {
				print(DOLLAR);
			} else {
				print(OPEN_CLOSE_PAREN);
			}
			return;
		}
		List<?> doubleStingList = null;
		if (feature.getEType() == EcorePackage.eINSTANCE.getEDouble()) {
			EStructuralFeature doubleStringFeature = feature.getEContainingClass().getEStructuralFeature(feature.getName() + "AsString");
			if (doubleStringFeature == null) {
				throw new SerializerException("Field " + feature.getName() + "AsString" + " not found");
			}
			doubleStingList = (List<?>) object.eGet(doubleStringFeature);
		}
		if (list.isEmpty()) {
			if (!feature.isUnsettable()) {
				print(OPEN_CLOSE_PAREN);
			} else {
				print("$");
			}
		} else {
			print(OPEN_PAREN);
			boolean first = true;
			int index = 0;
			for (Object listObject : list) {
				if (!first) {
					print(COMMA);
				}
				if (feature instanceof EReference && listObject instanceof Long) {
					print(DASH);
					print(String.valueOf(getExpressId((Long)listObject)));
				} else {
					if (listObject == null) {
						print(DOLLAR);
					} else {
						if (listObject instanceof WrappedVirtualObject && feature.getEType().getEAnnotation("wrapped") != null) {
							WrappedVirtualObject eObject = (WrappedVirtualObject) listObject;
							EClass eObjectEClass = platformService.getEClassForCid(eObject.getEClassId());
							Object realVal = eObject.eGet(eObjectEClass.getEStructuralFeature("wrappedValue"));
							if (realVal instanceof Double) {
								Object stringVal = eObject.eGet(eObjectEClass.getEStructuralFeature("wrappedValueAsString"));
								if (stringVal != null) {
									print((String) stringVal);
								} else {
									IfcParserWriterUtils.writePrimitive(realVal, printWriter);
								}
							} else {
								IfcParserWriterUtils.writePrimitive(realVal, printWriter);
							}
						} else if (listObject instanceof WrappedVirtualObject) {
							WrappedVirtualObject eObject = (WrappedVirtualObject) listObject;
							EClass class1 = platformService.getEClassForCid(eObject.getEClassId());
							EStructuralFeature structuralFeature = class1.getEStructuralFeature(WRAPPED_VALUE);
							if (structuralFeature != null) {
								Object realVal = eObject.eGet(structuralFeature);
								print(packageMetaData.getUpperCase(class1));
								print(OPEN_PAREN);
								if (realVal instanceof Double) {
									EStructuralFeature asStringFeature = class1.getEStructuralFeature(structuralFeature.getName() + "AsString");
									String asString = (String) eObject.eGet(asStringFeature);
									writeDoubleValue((Double)realVal, asString, structuralFeature);
								} else {
									IfcParserWriterUtils.writePrimitive(realVal, printWriter);
								}
								print(CLOSE_PAREN);
							} else {
								if (feature.getEAnnotation("twodimensionalarray") != null) {
									EClass eObjectEClass = platformService.getEClassForCid(eObject.getEClassId());
									writeList(eObject, eObjectEClass.getEStructuralFeature("List"));
								} else {
//										LOGGER.info("Unfollowable reference found from " + object + "(" + object.getOid() + ")." + feature.getName() + " to " + eObject + "(" + eObject.getOid() + ")");
								}
							}
						} else {
							if (doubleStingList != null) {
								if (index < doubleStingList.size()) {
									String val = (String)doubleStingList.get(index);
									if (val == null) {
										IfcParserWriterUtils.writePrimitive(listObject, printWriter);
									} else {
										print(val);
									}
								} else {
									IfcParserWriterUtils.writePrimitive(listObject, printWriter);
								}
							} else {
								IfcParserWriterUtils.writePrimitive(listObject, printWriter);
							}
						}
					}
				}
				first = false;
				index++;				
			}
			print(CLOSE_PAREN);
		}
	}

	private void writeWrappedValue(VirtualObject object, EStructuralFeature feature, EClass ec) throws SerializerException, IOException {
		Object get = object.eGet(feature);
		boolean isWrapped = ec.getEAnnotation("wrapped") != null;
		EStructuralFeature structuralFeature = ec.getEStructuralFeature(WRAPPED_VALUE);
		if (get instanceof WrappedVirtualObject) {
			boolean isDefinedWrapped = feature.getEType().getEAnnotation("wrapped") != null;
			WrappedVirtualObject betweenObject = (WrappedVirtualObject) get;
			if (betweenObject != null) {
				if (isWrapped && isDefinedWrapped) {
					Object val = betweenObject.eGet(structuralFeature);
					String name = structuralFeature.getEType().getName();
					if ((name.equals(IFC_BOOLEAN) || name.equals(IFC_LOGICAL)) && val == null) {
						print(BOOLEAN_UNDEFINED);
					} else if (structuralFeature.getEType() == ECORE_PACKAGE_INSTANCE.getEDouble()) {
						EClass objectEClass = platformService.getEClassForCid(object.getEClassId());
						EStructuralFeature asStringFeature = objectEClass.getEStructuralFeature(feature.getName() + "AsString");
						String asString = (String) betweenObject.eGet(asStringFeature);
						writeDoubleValue((Double)val, asString, feature);
					} else {
						IfcParserWriterUtils.writePrimitive(val, printWriter);
					}
				} else {
					writeEmbedded(betweenObject);
				}
			}
		} else if (get instanceof EList<?>) {
			EList<?> list = (EList<?>) get;
			if (list.isEmpty()) {
				if (!feature.isUnsettable()) {
					print(OPEN_CLOSE_PAREN);
				} else {
					print("$");
				}
			} else {
				print(OPEN_PAREN);
				boolean first = true;
				for (Object o : list) {
					if (!first) {
						print(COMMA);
					}
					VirtualObject object2 = (VirtualObject) o;
					Object val = object2.eGet(structuralFeature);
					if (structuralFeature.getEType() == ECORE_PACKAGE_INSTANCE.getEDouble()) {
						EClass object2EClass = platformService.getEClassForCid(object2.getEClassId());
						EStructuralFeature asStringFeature = object2EClass.getEStructuralFeature(feature.getName() + "AsString");
						String asString = (String) object2.eGet(asStringFeature);
						writeDoubleValue((Double)val, asString, structuralFeature);
					} else {
						IfcParserWriterUtils.writePrimitive(val, printWriter);
					}
					first = false;
				}
				print(CLOSE_PAREN);
			}
		} else if (get == null) {
			EClassifier type = structuralFeature.getEType();
			if (type.getName().equals("IfcBoolean") || type.getName().equals("IfcLogical") || type == ECORE_PACKAGE_INSTANCE.getEBoolean()) {
				print(BOOLEAN_UNDEFINED);
			} else {
				EClass objectEClass = platformService.getEClassForCid(object.getEClassId());
				EntityDefinition entityBN = getSchemaDefinition().getEntityBN(objectEClass.getName());
				if (entityBN != null && entityBN.isDerived(feature.getName())) {
					print(ASTERISK);
				} else {
					print(DOLLAR);
				}
			}
		} else {
			System.out.println("Unimplemented?");
		}
	}

	private SchemaDefinition getSchemaDefinition() {
		return packageMetaData.getSchemaDefinition();
	}

	private void writeEnum(VirtualObject object, EStructuralFeature feature) throws SerializerException, IOException {
		Object val = object.eGet(feature);
		if (feature.getEType().getName().equals("Tristate")) {
			if (val == null) {
				print(DOLLAR);
			} else {
				IfcParserWriterUtils.writePrimitive(val, printWriter);
			}
		} else {
			if (val == null) {
				print(DOLLAR);
			} else {
				if (val.equals(NULL)) {
					print(DOLLAR);
				} else {
					print(DOT);
//					if (val instanceof String) {
						print((String)val);
//					} else {
//						print(val.toString().toUpperCase());
//					}
					print(DOT);
				}
			}
		}
	}
}