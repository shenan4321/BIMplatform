package cn.dlb.bim.ifc.tree.stream;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.stream.query.JsonQueryObjectModelConverter;
import cn.dlb.bim.ifc.stream.query.Query;
import cn.dlb.bim.ifc.stream.query.QueryException;

public class StreamProjectTreeScript {
	private final Query query;
	public StreamProjectTreeScript(PackageMetaData packageMetaData) throws JsonParseException, JsonMappingException, IOException, QueryException {
		JsonQueryObjectModelConverter converter = new JsonQueryObjectModelConverter(packageMetaData);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		ObjectNode queryJson = mapper.readValue("{\r\n" + 
				"defines: {\r\n" + 
				"Representation: {\r\n" + 
				"type: \"IfcProduct\",\r\n" + 
				"fields: [\"Representation\", \"geometry\"]\r\n" + 
				"},\r\n" + 
				"ContainsElementsDefine: {\r\n" + 
				"type: \"IfcSpatialStructureElement\",\r\n" + 
				"field: \"ContainsElements\",\r\n" + 
				"include: {\r\n" + 
				"type: \"IfcRelContainedInSpatialStructure\",\r\n" + 
				"field: \"RelatedElements\",\r\n" + 
				"includes: [\r\n" + 
				"\"IsDecomposedByDefine\",\r\n" + 
				"\"ContainsElementsDefine\",\r\n" + 
				"\"Representation\"\r\n" + 
				"]\r\n" + 
				"}\r\n" + 
				"},\r\n" + 
				"IsDecomposedByDefine: {\r\n" + 
				"type: \"IfcObjectDefinition\",\r\n" + 
				"field: \"IsDecomposedBy\",\r\n" + 
				"include: {\r\n" + 
				"type: \"IfcRelDecomposes\",\r\n" + 
				"field: \"RelatedObjects\",\r\n" + 
				"includes: [\r\n" + 
				"\"IsDecomposedByDefine\",\r\n" + 
				"\"ContainsElementsDefine\",\r\n" + 
				"\"Representation\"\r\n" + 
				"]\r\n" + 
				"}\r\n" + 
				"}\r\n" + 
				"},\r\n" + 
				"queries: [\r\n" + 
				"{\r\n" + 
				"type: \"IfcProject\",\r\n" + 
				"includes: [\r\n" + 
				"\"IsDecomposedByDefine\",\r\n" + 
				"\"ContainsElementsDefine\"\r\n" + 
				"]\r\n" + 
				"},\r\n" + 
				"{\r\n" + 
				"type: \"IfcRepresentation\",\r\n" + 
				"includeAllSubtypes: true\r\n" + 
				"},\r\n" + 
				"{\r\n" + 
				"type: \"IfcProductRepresentation\"\r\n" + 
				"},\r\n" + 
				"{\r\n" + 
				"type: \"IfcPresentationLayerWithStyle\"\r\n" + 
				"},\r\n" + 
				"{\r\n" + 
				"type: \"IfcProduct\",\r\n" + 
				"includeAllSubtypes: true\r\n" + 
				"},\r\n" + 
				"{\r\n" + 
				"type: \"IfcProductDefinitionShape\"\r\n" + 
				"},\r\n" + 
				"{\r\n" + 
				"type: \"IfcPresentationLayerAssignment\"\r\n" + 
				"},\r\n" + 
				"{\r\n" + 
				"type: \"IfcRelAssociatesClassification\",\r\n" + 
				"includes: [\r\n" + 
				"{\r\n" + 
				"type: \"IfcRelAssociatesClassification\",\r\n" + 
				"field: \"RelatedObjects\"\r\n" + 
				"},\r\n" + 
				"{\r\n" + 
				"type: \"IfcRelAssociatesClassification\",\r\n" + 
				"field: \"RelatingClassification\"\r\n" + 
				"}\r\n" + 
				"]\r\n" + 
				"},\r\n" + 
				"{\r\n" + 
				"type: \"IfcSIUnit\"\r\n" + 
				"},\r\n" + 
				"{\r\n" + 
				"type: \"IfcPresentationLayerAssignment\"\r\n" + 
				"}\r\n" + 
				"]\r\n" + 
				"};", ObjectNode.class);
			
		query = converter.parseJson("test", queryJson);
		
	}
	
	public Query getQuery() {
		return query;
	}
	
}
