package cn.dlb.bim.dao.entity;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "OutputTemplate")
public class OutputTemplate {
	@Id
	private Long otid;
	private String name;
	private Map<String, NamespaceSelector> namespaceSelectorMap;
	public OutputTemplate() {
		namespaceSelectorMap = new LinkedHashMap<>();
	}
	public Long getOtid() {
		return otid;
	}
	public void setOtid(Long otid) {
		this.otid = otid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, NamespaceSelector> getNamespaceSelectorMap() {
		return namespaceSelectorMap;
	}
	public void setNamespaceSelectorMap(Map<String, NamespaceSelector> namespaceSelectorMap) {
		this.namespaceSelectorMap = namespaceSelectorMap;
	}
	public void putIntoMap(String ifcType, String namespace, String objectType, Boolean selected) {
		String objectTypefiltered = objectType;
		if ("".equals(objectType)) {
			objectTypefiltered = ObjectTypeSelector.UNDEFINED;
		}
		if (namespaceSelectorMap.containsKey(ifcType)) {
			NamespaceSelector selector = namespaceSelectorMap.get(ifcType);
			selector.putIntoMap(namespace, objectTypefiltered, selected);
		} else {
			NamespaceSelector selector = new NamespaceSelector();
			selector.setIfcType(ifcType);
			selector.putIntoMap(namespace, objectTypefiltered, selected);
			namespaceSelectorMap.put(ifcType, selector);
		}
	}
	public Boolean isSelected(String ifcType, String namespace, String objectType) {
		if (namespaceSelectorMap.containsKey(ifcType)) {
			NamespaceSelector namespaceSelector = namespaceSelectorMap.get(ifcType);
			return namespaceSelector.isOjectTypeSelected(namespace, objectType);
		} else {
			return false;
		}
	}
}
