package cn.dlb.bim.ifc.stream;

import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import cn.dlb.bim.ifc.database.DatabaseException;

public interface MinimalVirtualObject {
	void setAttribute(EStructuralFeature eStructuralFeature, Object value) throws DatabaseException;
	Object eGet(EStructuralFeature feature);
	boolean useFeatureForSerialization(EStructuralFeature feature);
	boolean useFeatureForSerialization(EStructuralFeature feature, int index);
	public void addUseForSerialization(EStructuralFeature eStructuralFeature);
	public void addUseForSerialization(EStructuralFeature eStructuralFeature, int index);
	public EClass eClass();
	public Map<String, Object> getFeatures();
}
