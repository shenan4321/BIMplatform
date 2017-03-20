package cn.dlb.bim.ifc.binary;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;

public interface IdClassMapper {
	public EClassifier getEClassifier(String packageName, String classifierName);
	public Short getCidOfEClass(EClass eClass);
	public EClass getEClassForCid(short cid);
}
