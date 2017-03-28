package cn.dlb.bim.ifc.deserializers;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import cn.dlb.bim.ifc.model.IfcHeader;

public class IfcHeaderParser {

	public IfcHeader parseFileName(String line) throws DeserializeException, ParseException {
		IfcHeader ifcHeader = new IfcHeader();
		parseFileName(line, ifcHeader);
		return ifcHeader;
	}
	
	public IfcHeader parseDescription(String line) throws DeserializeException, ParseException {
		IfcHeader ifcHeader = new IfcHeader();
		parseDescription(line, ifcHeader);
		return ifcHeader;
	}
	
	public void parseDescription(String line, IfcHeader ifcHeader) throws DeserializeException, ParseException {
		line = line.replace("\r\n", "");

		StepParser stepParser = new StepParser(line);
		StepParser startList = stepParser.startList();
		while (startList.hasMoreListItems()) {
			ifcHeader.getDescription().add(startList.readNextString());
		}
		ifcHeader.setImplementationLevel(stepParser.readNextString());
	}
	
	public void parseFileName(String line, IfcHeader ifcHeader) throws DeserializeException, ParseException {
		line = line.replace("\r\n", "");
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");

		StepParser stepParser = new StepParser(line);
		ifcHeader.setFilename(stepParser.readNextString());
		ifcHeader.setTimeStamp(dateFormatter.parse(stepParser.readNextString()));
		StepParser startList = stepParser.startList();
		while (startList.hasMoreListItems()) {
			ifcHeader.getAuthor().add(startList.readNextString());
		}
		startList = stepParser.startList();
		while (startList.hasMoreListItems()) {
			ifcHeader.getOrganization().add(startList.readNextString());
		}
		ifcHeader.setPreProcessorVersion(stepParser.readNextString());
		ifcHeader.setOriginatingSystem(stepParser.readNextString());
		ifcHeader.setAuthorization(stepParser.readNextString());
	}

	public void parseFileSchema(String line, IfcHeader ifcHeader) throws DeserializeException {
		line = line.replace("\r\n", "");
		StepParser stepParser = new StepParser(line);
		ifcHeader.setIfcSchemaVersion(stepParser.readNextString());
	}
}