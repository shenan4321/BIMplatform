package cn.dlb.bim.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.springframework.data.util.CloseableIterator;
import org.springframework.web.socket.WebSocketSession;
import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.ifc.database.DatabaseException;
import cn.dlb.bim.ifc.deserializers.DeserializeException;
import cn.dlb.bim.ifc.deserializers.StepParser;
import cn.dlb.bim.ifc.emf.IfcModelInterfaceException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.Schema;
import cn.dlb.bim.ifc.stream.GeometryGeneratingException;
import cn.dlb.bim.ifc.stream.StreamingGeometryGenerator;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.deserializers.IfcStepStreamingDeserializer;
import cn.dlb.bim.ifc.stream.query.QueryContext;
import cn.dlb.bim.service.PlatformService;

public class StreamingCheckinAction extends LongAction {

	private static String IFC2X3_SCHEMA_SHORT = "IFC2X3";
	private static String IFC4_SCHEMA_SHORT = "IFC4";

	private final File ifcFile;
	private final PlatformServer server;
	private final PlatformService platformService;

	public StreamingCheckinAction(WebSocketSession webSocketSession, File ifcFile, PlatformServer server,
			PlatformService platformService) {
		super(webSocketSession);
		this.ifcFile = ifcFile;
		this.server = server;
		this.platformService = platformService;
	}

	@Override
	public void execute() throws DatabaseException, IfcModelInterfaceException {
		IfcStepStreamingDeserializer deserializer = new IfcStepStreamingDeserializer() {
		};
		Schema schema = null;
		try {
			schema = preReadSchema(ifcFile);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DeserializeException e) {
			e.printStackTrace();
		}

		PackageMetaData packageMetaData = server.getMetaDataManager().getPackageMetaData(schema.getEPackageName());
		deserializer.init(packageMetaData, platformService);
		try {
			deserializer.read(ifcFile);
		} catch (DeserializeException e) {
			e.printStackTrace();
		}
		Integer rid = deserializer.getRid();
		fixInverses(packageMetaData, rid);
		
		StreamingGeometryGenerator generator = new StreamingGeometryGenerator(server, platformService, rid);
		QueryContext queryContext = new QueryContext(platformService, packageMetaData, rid);
		try {
			generator.generateGeometry(queryContext);
		} catch (GeometryGeneratingException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private void fixInverses(PackageMetaData packageMetaData, Integer rid)
			throws DatabaseException {
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
			platformService.update(referencedObject);
		}
		
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
			if (eReference.getName().equals("RelatedElements")
					&& referencedObjectEclass.getName().equals("IfcSpace")) {
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

}
