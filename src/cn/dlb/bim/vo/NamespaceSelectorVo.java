package cn.dlb.bim.vo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class NamespaceSelectorVo {
	private String namespace;
	private Map<String, ObjectTypeContainerVo> objectTypeContainerMap;
	public NamespaceSelectorVo() {
		objectTypeContainerMap = new LinkedHashMap<>();
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public Map<String, ObjectTypeContainerVo> getObjectTypeContainerMap() {
		return objectTypeContainerMap;
	}
	public void setObjectTypeContainerMap(Map<String, ObjectTypeContainerVo> objectTypeContainerMap) {
		this.objectTypeContainerMap = objectTypeContainerMap;
	}
	public void putIntoMap(String objectType, Boolean selected, Long oid) {
		if (objectTypeContainerMap.containsKey(objectType)) {
			ObjectTypeContainerVo objectTypeContainerVo = objectTypeContainerMap.get(objectType);
			objectTypeContainerVo.getOids().add(oid);
		} else {
			ObjectTypeContainerVo objectTypeContainerVo = new ObjectTypeContainerVo();
			objectTypeContainerVo.setObjectType(objectType);
			objectTypeContainerVo.setSelected(selected);
			objectTypeContainerVo.getOids().add(oid);
			objectTypeContainerMap.put(objectType, objectTypeContainerVo);
		}
	}
	public void getAllSelectedOids(Set<Long> resultSet) {
		for (ObjectTypeContainerVo objectTypeContainerVo :objectTypeContainerMap.values()) {
			if (objectTypeContainerVo.getSelected() == true) {
				resultSet.addAll(objectTypeContainerVo.getOids());
			}
		}
	}
}
