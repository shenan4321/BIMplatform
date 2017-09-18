package cn.dlb.bim.ifc.stream;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.base.Charsets;

import cn.dlb.bim.database.DatabaseException;

@Document(collection = "WrappedVirtualObject")
public class WrappedVirtualObject extends ReadWriteVirtualObject {
	private Short eClassId;
	private final Map<Integer, Object> features = new LinkedHashMap<>();
	
	/**
	 * 用于标注是否序列化输出，但不影响数据库存储
	 */
	@Transient
	private final Map<EStructuralFeature, Object> useForSerializationFeatures;
	@Transient
	private EClass eClass;
	
	public WrappedVirtualObject() {
		useForSerializationFeatures = new LinkedHashMap<>();
	}
	
	public WrappedVirtualObject(Short eClassId, EClass eClass) {
		this.eClassId = eClassId;
		this.eClass = eClass;
		useForSerializationFeatures = new LinkedHashMap<>();
	}
	
	public Short getEClassId() {
		return eClassId;
	}
	public Map<Integer, Object> getFeatures() {
		return features;
	}
	public Object eGet(EStructuralFeature feature) {
		return features.get(feature.getFeatureID());
	}
	public void eUnset(EStructuralFeature eStructuralFeature) {
		features.remove(eStructuralFeature.getFeatureID());
	}
	public boolean eIsSet(EStructuralFeature feature) {
		return features.containsKey(feature.getFeatureID());
	}
	public void setAttribute(EStructuralFeature eStructuralFeature, Object value) {
		if (value != null) {
			features.put(eStructuralFeature.getFeatureID(), value);
		}
	}
	public void set(String featureName, Object value) {
		if (value != null) {
			EStructuralFeature feature = eClass.getEStructuralFeature(featureName);
			features.put(feature.getFeatureID(), value);
		}
	}
	public Object eGet(String featureName) {
		EStructuralFeature feature = eClass.getEStructuralFeature(featureName);
		return features.get(feature.getFeatureID());
	}

	@SuppressWarnings("unchecked")
	public boolean useFeatureForSerialization(EStructuralFeature feature, int index) {
		if (feature instanceof EAttribute) {
			return true;
		}
		if (useForSerializationFeatures.containsKey(feature)) {
			Object object = useForSerializationFeatures.get(feature);
			if (object instanceof Set) {
				Set<Integer> set = (Set<Integer>) object;
				if (set.contains(index)) {
					return true;
				}
			} else {
				return object == Boolean.TRUE;
			}
		}
		return false;
	}
	
	public boolean useFeatureForSerialization(EStructuralFeature feature) {
		if (feature instanceof EAttribute) {
			return true;
		}
		return useForSerializationFeatures.containsKey(feature);
	}

	public void addUseForSerialization(EStructuralFeature eStructuralFeature) {
		if (eStructuralFeature.getEContainingClass().isSuperTypeOf(eClass)) {
			useForSerializationFeatures.put(eStructuralFeature, true);
		} else {
			throw new IllegalArgumentException(eStructuralFeature.getName() + " does not exist in " + eClass.getName());
		}
	}
	
	@SuppressWarnings("unchecked")
	public void addUseForSerialization(EStructuralFeature eStructuralFeature, int index) {
		if (eStructuralFeature.getEContainingClass().isSuperTypeOf(eClass)) {
			Set<Integer> set = (Set<Integer>) useForSerializationFeatures.get(eStructuralFeature);
			if (set == null) {
				set = new HashSet<>();
				useForSerializationFeatures.put(eStructuralFeature, set);
			}
			set.add(index);
		} else {
			throw new IllegalArgumentException(eStructuralFeature.getName() + " does not exist in " + eClass.getName());
		}
	}

	public EClass eClass() {
		return eClass;
	}
	
	public ByteBuffer write(ByteBuffer buffer) {
		buffer = ensureCapacity(buffer, 6);
		buffer.putShort(eClassId);
		buffer.putInt(features.size());
		for (Entry<Integer, Object> entry : features.entrySet()) {
			Integer featureId = entry.getKey();
			Object value = entry.getValue();
			buffer = ensureCapacity(buffer, 4);
			buffer.putInt(featureId);
			buffer = writeFeature(buffer, value);
		}
		return buffer;
	}
	
	@Override
	public void read(ByteBuffer buffer) {
		eClassId = buffer.getShort();
		int size = buffer.getInt();
		for (int i = 0; i < size; i++) {
			Integer featureId = buffer.getInt();
			Object feature = readFeature(buffer);
			features.put(featureId, feature);
		}
	}

	@Override
	public ReadWriteVirtualObject createReadWriteVirtualObject() {
		return new WrappedVirtualObject();
	}
	
}
