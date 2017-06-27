package cn.dlb.bim.vo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class IfcTypeSelectorVo {
	private String ifcType;
	private Map<String, NamespaceSelectorVo> namespaceSelectorMap;
	public IfcTypeSelectorVo() {
		namespaceSelectorMap = new LinkedHashMap<>();
	}
	public String getIfcType() {
		return ifcType;
	}
	public void setIfcType(String ifcType) {
		this.ifcType = ifcType;
	}
	public Map<String, NamespaceSelectorVo> getNamespaceSelectorMap() {
		return namespaceSelectorMap;
	}
	public void setNamespaceSelectorMap(Map<String, NamespaceSelectorVo> namespaceSelectorMap) {
		this.namespaceSelectorMap = namespaceSelectorMap;
	}
	public void putIntoMap(String namespace, String objectType, Boolean selected, Long oid) {
		if (namespaceSelectorMap.containsKey(namespace)) {
			NamespaceSelectorVo namespaceSelectorVo = namespaceSelectorMap.get(namespace);
			namespaceSelectorVo.putIntoMap(objectType, selected, oid);
		} else {
			NamespaceSelectorVo namespaceSelectorVo = new NamespaceSelectorVo();
			namespaceSelectorVo.setNamespace(namespace);
			namespaceSelectorVo.putIntoMap(objectType, selected, oid);
			namespaceSelectorMap.put(namespace, namespaceSelectorVo);
		}
	}
	public void getAllSelectedOids(Set<Long> resultSet) {
		for (NamespaceSelectorVo namespaceSelectorVo : namespaceSelectorMap.values()) {
			namespaceSelectorVo.getAllSelectedOids(resultSet);
		}
	}
}
