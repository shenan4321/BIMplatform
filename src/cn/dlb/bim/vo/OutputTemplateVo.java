package cn.dlb.bim.vo;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import cn.dlb.bim.dao.entity.OutputTemplate;

public class OutputTemplateVo implements ITransformer<OutputTemplate> {
	private Long otid;
	private String name;
	private Map<String, IfcTypeSelectorVo> ifcTypeSelectorMap;
	public OutputTemplateVo() {
		ifcTypeSelectorMap = new LinkedHashMap<>();
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
	public Map<String, IfcTypeSelectorVo> getIfcTypeSelectorMap() {
		return ifcTypeSelectorMap;
	}
	public void setIfcTypeSelectorMap(Map<String, IfcTypeSelectorVo> ifcTypeSelectorMap) {
		this.ifcTypeSelectorMap = ifcTypeSelectorMap;
	}
	@Override
	public void transformFrom(OutputTemplate origin) {
		throw new UnsupportedOperationException();
	}
	@Override
	public OutputTemplate transformTo() {
		OutputTemplate templateEntity = new OutputTemplate();
		templateEntity.setName(this.name);
		for (IfcTypeSelectorVo ifcTypeSelectorVo : ifcTypeSelectorMap.values()) {
			String ifcType = ifcTypeSelectorVo.getIfcType();
			for (NamespaceSelectorVo namespaceSelectorVo : ifcTypeSelectorVo.getNamespaceSelectorMap().values()) {
				String namespace = namespaceSelectorVo.getNamespace();
				for (ObjectTypeContainerVo container : namespaceSelectorVo.getObjectTypeContainerMap().values()) {
					String objectType = container.getObjectType();
					Boolean selected = container.getSelected();
					templateEntity.putIntoMap(ifcType, namespace, objectType, selected);
				}
			}
		}
		return templateEntity;
	}
	
	public void putIntoMap(String ifcType, String namespace, String objectType, Boolean selected, Long oid) {
		if (ifcTypeSelectorMap.containsKey(ifcType)) {
			IfcTypeSelectorVo ifcTypeSelectorVo = ifcTypeSelectorMap.get(ifcType);
			ifcTypeSelectorVo.putIntoMap(namespace, objectType, selected, oid);
		} else {
			IfcTypeSelectorVo ifcTypeSelectorVo = new IfcTypeSelectorVo();
			ifcTypeSelectorVo.setIfcType(ifcType);
			ifcTypeSelectorVo.putIntoMap(namespace, objectType, selected, oid);
			ifcTypeSelectorMap.put(ifcType, ifcTypeSelectorVo);
		}
	}
	
	public Set<Long> allSelectedOids() {
		Set<Long> resultSet = new HashSet<>();
		for (IfcTypeSelectorVo ifcTypeSelectorVo : ifcTypeSelectorMap.values()) {
			ifcTypeSelectorVo.getAllSelectedOids(resultSet);
		}
		return resultSet;
	}
}
