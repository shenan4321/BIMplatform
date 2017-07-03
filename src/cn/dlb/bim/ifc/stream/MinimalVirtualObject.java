package cn.dlb.bim.ifc.stream;

import org.eclipse.emf.ecore.EStructuralFeature;
import cn.dlb.bim.ifc.database.DatabaseException;

public interface MinimalVirtualObject {
	void setAttribute(EStructuralFeature eStructuralFeature, Object value) throws DatabaseException;
	Object eGet(EStructuralFeature feature);
}
