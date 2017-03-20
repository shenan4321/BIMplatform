package cn.dlb.bim.ifc.idm;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

public interface ObjectIDM {
	boolean shouldFollowReference(EClass originalClass, EClass eClass, EStructuralFeature eStructuralFeature);
	boolean shouldIncludeClass(EClass originalClass, EClass eClass);
}