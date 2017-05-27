package cn.dlb.bim.ifc.emf;

import org.eclipse.emf.ecore.EClass;

public class OfflineOidProvider implements OidProvider {
	private Long oidCounter = 1l;

	@Override
	public long newOid(EClass eClass) {
		return oidCounter++;
	}
	
}
