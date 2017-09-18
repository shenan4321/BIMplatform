package cn.dlb.bim.ifc.stream;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.primitives.Bytes;
import com.sleepycat.persist.model.Entity;

@Entity
@Document(collection = "VirtualObject")
public class VirtualObject extends ReadWriteVirtualObject implements Externalizable {
	
	@Indexed
	private Long oid;
	
	@Indexed
	private Integer rid;
	
	@Indexed
	private Short eClassId;
	
	private final Map<Integer, Object> features;

	/**
	 * 用于标注是否序列化输出，但不影响数据库存储
	 */
	@Transient
	private final Map<EStructuralFeature, Object> useForSerializationFeatures;
	@Transient
	private EClass eClass;
	
	public VirtualObject() {
		useForSerializationFeatures = new LinkedHashMap<>();
		features = new LinkedHashMap<>();
	}

	public VirtualObject(Integer rid, Short eClassId, Long oid, EClass eClass) {
		this.rid = rid;
		this.eClassId = eClassId;
		this.oid = oid;
		features = new LinkedHashMap<>();
		useForSerializationFeatures = new LinkedHashMap<>();
		this.eClass = eClass;
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
		return features.get(feature.getFeatureID());
	}

	public void eUnset(EStructuralFeature eStructuralFeature) {
		features.remove(eStructuralFeature.getFeatureID());
	}

	public boolean eIsSet(EStructuralFeature feature) {
		return features.containsKey(feature.getFeatureID());
	}

	public void setReference(EStructuralFeature eStructuralFeature, Long oid) {
		features.put(eStructuralFeature.getFeatureID(), oid);
	}

	public void setAttribute(EStructuralFeature eStructuralFeature, Object value) {
		if (value != null) {
			features.put(eStructuralFeature.getFeatureID(), value);
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
		List list = (List<?>) features.get(structuralFeature.getFeatureID());
		if (list == null) {
			list = new ArrayList(minSize == -1 ? 0 : minSize);
			features.put(structuralFeature.getFeatureID(), list);
		}
		while (list.size() < minSize) {
			list.add(null);
		}
		return list;
	}

	public Map<Integer, Object> getFeatures() {
		return features;
	}

	public Boolean has(String featureName) {
		EStructuralFeature feature = eClass.getEStructuralFeature(featureName);
		if (feature != null) {
			return features.containsKey(feature.getFeatureID());
		} else {
			return false;
		}
	}

	public Object get(String featureName) {
		EStructuralFeature feature = eClass.getEStructuralFeature(featureName);
		if (feature != null) {
			return features.get(feature.getFeatureID());
		} else {
			return null;
		}
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
	
	public void setEClass(EClass eClass) {
		this.eClass = eClass;
	}

	public ByteBuffer write(ByteBuffer buffer) {
		buffer = ensureCapacity(buffer, 18);
		buffer.putShort(eClassId);
		buffer.putLong(oid);
		buffer.putInt(rid);
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
		oid = buffer.getLong();
		rid = buffer.getInt();
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

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		ByteBuffer buffer = ByteBuffer.allocate(256);
		buffer = write(buffer);
		out.write(buffer.array(), 0, buffer.position());
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		if (in.available() <= 0) {
			return;
		}
		int available = in.available();
		byte[] bytesTotal = new byte[available];
		in.readFully(bytesTotal);
		available = in.available();
		while (available > 0) {
			byte[] section = new byte[available];
			in.readFully(section);
			bytesTotal = Bytes.concat(bytesTotal, section);
			available = in.available();
		}
		ByteBuffer buffer = ByteBuffer.wrap(bytesTotal);
		read(buffer);
	}
	
}
