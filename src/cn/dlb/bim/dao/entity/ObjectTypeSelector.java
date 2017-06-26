package cn.dlb.bim.dao.entity;

import java.util.LinkedHashMap;
import java.util.Map;

public class ObjectTypeSelector {
	public static String UNDEFINED = "undefined";
	private String namespace;
	private Map<String, Boolean> objectTypeMap;
	public ObjectTypeSelector() {
		objectTypeMap = new LinkedHashMap<String, Boolean>();
	}
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public Map<String, Boolean> getObjectTypeMap() {
		return objectTypeMap;
	}
	public void setObjectTypeMap(Map<String, Boolean> objectTypeMap) {
		this.objectTypeMap = objectTypeMap;
	}
	public boolean isOjectTypeSelected(String objectType) {
		if ("".equals(objectType)) {
			return objectTypeMap.get(UNDEFINED);
		}else if (objectTypeMap.containsKey(objectType)) {
			return objectTypeMap.get(objectType);
		}
		return false;
	}
}
