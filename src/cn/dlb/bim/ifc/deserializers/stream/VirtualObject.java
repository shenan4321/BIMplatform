package cn.dlb.bim.ifc.deserializers.stream;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "VirtualObject")
public class VirtualObject {
	@Id
	private Long oid;
	@Indexed
	private Integer rid;
	@Indexed
	private final Short eClassId;
	private final Map<String, Object> features = new LinkedHashMap<>();
	public VirtualObject(Short eClassId, Long oid) {
		this.eClassId = eClassId;
		this.oid = oid;
	}
	public Long getOid() {
		return oid;
	}
	public void setOid(Long oid) {
		this.oid = oid;
	}
	public Integer getRid() {
		return rid;
	}
	public void setRid(Integer rid) {
		this.rid = rid;
	}
	public Short getEClassId() {
		return eClassId;
	}
	public Object eGet(EStructuralFeature feature) {
		return features.get(feature.getName());
	}
	public void eUnset(EStructuralFeature eStructuralFeature) {
//		features.put(eStructuralFeature.getName(), null);
		features.remove(eStructuralFeature.getName());
	}
	public boolean eIsSet(EStructuralFeature feature) {
		return features.containsKey(feature);
	}
	public void setReference(EStructuralFeature eStructuralFeature, Long oid) {
		features.put(eStructuralFeature.getName(), oid);
	}
	public void setAttribute(EStructuralFeature eStructuralFeature, Object value) {
		if (value != null) {
			features.put(eStructuralFeature.getName(), value);
		}
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setListItem(EStructuralFeature structuralFeature, int index, Object value) {
		List list = getOrCreateList(structuralFeature, index + 1);
		list.set(index, value);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setListItemReference(EStructuralFeature structuralFeature, int index, Long referencedOid) {
		List list = getOrCreateList(structuralFeature, index + 1);
		list.set(index, referencedOid);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List getOrCreateList(EStructuralFeature structuralFeature, int minSize) {
		List list = (List<?>) features.get(structuralFeature.getName());
		if (list == null) {
			list = new ArrayList(minSize == -1 ? 0 : minSize);
			features.put(structuralFeature.getName(), list);
		}
		while (list.size() < minSize) {
			list.add(null);
		}
		return list;
	}
	public Map<String, Object> getFeatures() {
		return features;
	}
}
