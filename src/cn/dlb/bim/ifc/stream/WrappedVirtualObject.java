package cn.dlb.bim.ifc.stream;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "WrappedVirtualObject")
public class WrappedVirtualObject implements MinimalVirtualObject {
	private final Short eClassId;
	private final Map<String, Object> features = new LinkedHashMap<>();
	
	public WrappedVirtualObject(Short eClassId) {
		this.eClassId = eClassId;
	}
	
	public Short getEClassId() {
		return eClassId;
	}
	public Map<String, Object> getFeatures() {
		return features;
	}
	public Object eGet(EStructuralFeature feature) {
		return features.get(feature.getName());
	}
	public void eUnset(EStructuralFeature eStructuralFeature) {
		features.remove(eStructuralFeature.getName());
	}
	public boolean eIsSet(EStructuralFeature feature) {
		return features.containsKey(feature);
	}
	public void setAttribute(EStructuralFeature eStructuralFeature, Object value) {
		if (value != null) {
			features.put(eStructuralFeature.getName(), value);
		}
	}
}
