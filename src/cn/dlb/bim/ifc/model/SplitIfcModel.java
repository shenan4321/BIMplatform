package cn.dlb.bim.ifc.model;

import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.PackageMetaData;

public class SplitIfcModel extends BasicIfcModel {

	public SplitIfcModel(PackageMetaData packageMetaData) {
		super(packageMetaData);
	}
	
	public void processObject(IdEObject originObject, Map<Integer, IdEObject> result) {
		if (result.containsValue(originObject)) {
			return;
		}
		result.put(originObject.getExpressId(), originObject);
		for (EStructuralFeature feature : originObject.eClass().getEAllStructuralFeatures()) {
			if (getPackageMetaData().useForDatabaseStorage(originObject.eClass(), feature)) {
				if (feature.isMany()) {
					processList(originObject, feature, result);
				} else {
					Object value = originObject.eGet(feature);
					if (feature.getEType() instanceof EClass) {
						if (value != null) {
							IdEObject referencedObject = (IdEObject) value;
							processObject(referencedObject, result);
						}
					} 
				}
			}
		}
	}
	
	public void processList(IdEObject originObject, EStructuralFeature feature, Map<Integer, IdEObject> result) {
		if (result.containsValue(feature)) {
			return;
		}
		if (feature.getEType() instanceof EClass) {
			EList<?> list = (EList<?>) originObject.eGet(feature);
			for (Object o : list) {
				if (o != null) {
					IdEObject listObject = (IdEObject) o;
					if (feature.getEAnnotation("twodimensionalarray") != null) {
						processList(listObject, feature, result);
					} else {
						processObject(listObject, result);
					}
					
				}
			}
		}
	}

}
