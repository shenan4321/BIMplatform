package cn.dlb.bim.dao.entity;

import java.util.LinkedHashMap;
import java.util.Map;

public class NamespaceSelector {
	public static String UNDEFINED = "undefined";
	private Map<String, ObjectTypeSelector> namespaceMap;
	public NamespaceSelector() {
		namespaceMap = new LinkedHashMap<>();
	}
	public Map<String, ObjectTypeSelector> getNamespaceMap() {
		return namespaceMap;
	}
	public void setNamespaceMap(Map<String, ObjectTypeSelector> namespaceMap) {
		this.namespaceMap = namespaceMap;
	}
	public void putIntoMap(String namespace, String objectType, Boolean selected) {
		String objectTypefiltered = objectType;
		if ("".equals(objectType)) {
			objectTypefiltered = ObjectTypeSelector.UNDEFINED;
		}
		if (namespaceMap.containsKey(namespace)) {
			ObjectTypeSelector selector = namespaceMap.get(namespace);
			selector.getObjectTypeMap().put(objectTypefiltered, selected);
		} else {
			ObjectTypeSelector selector = new ObjectTypeSelector();
			selector.getObjectTypeMap().put(objectTypefiltered, selected);
			namespaceMap.put(namespace, selector);
		}
	}
	public boolean isOjectTypeSelected(String namespace, String objectType) {
		if (namespaceMap.containsKey(namespace)) {
			ObjectTypeSelector selector = namespaceMap.get(namespace);
			return selector.isOjectTypeSelected(objectType);
		} else {
			return false;
		}
	}
}
