package cn.dlb.bim.ifc.deserializers;

import cn.dlb.bim.utils.StringUtils;

public class StepParser {

	private String line;
	private int lastIndex = 0;
	
	public StepParser(String line) {
		this.line = line;
		if (line.startsWith("(") && line.endsWith(")")) {
			this.line = line.substring(1, line.length() - 1);
		}
		lastIndex = StringUtils.nextField(this.line, 0);
	}

	public String readNextString() throws DeserializeException {
		int nextIndex = StringUtils.nextString(line, lastIndex);
		String val = null;
		try {
			val = line.substring(lastIndex, nextIndex - 1).trim();
		} catch (Exception e) {
			throw new DeserializeException(0, "Expected string");
		}
		lastIndex = StringUtils.nextField(this.line, nextIndex);
		
		if (val.equals("$")) {
			return null;
		}
		
		return IfcParserWriterUtils.readString(val, 0);
	}

	private void skipSpaces() {
		while (lastIndex < line.length() - 1 && line.charAt(lastIndex) == ' ') {
			lastIndex++;
		}
	}
	
	public StepParser startList() throws DeserializeException {
		skipSpaces();
		
		int nextIndex = StringUtils.nextString(line, lastIndex);
		String val = line.substring(lastIndex, nextIndex - 1).trim();
		lastIndex = StringUtils.nextField(this.line, nextIndex);
		return new StepParser(val);
	}

	public boolean hasMoreListItems() {
		skipSpaces();
		if (lastIndex >= line.length()) {
			// End reached
			return false;
		}
		String character = line.substring(lastIndex, lastIndex + 1);
		return !character.equals(")");
	}

	public void endList() throws DeserializeException {
		String character = line.substring(lastIndex, lastIndex + 1);
		if (character.equals(")")) {
			lastIndex++;
		} else {
			throw new DeserializeException("Expected ), got " + character);
		}
	}
}
