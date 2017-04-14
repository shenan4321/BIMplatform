package cn.dlb.bim.ifc.serializers;

import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.ProjectInfo;

public class Ifc2x3tc1StepSerializer extends IfcStepSerializer {

	public Ifc2x3tc1StepSerializer() {
		super();
	}
	
	@Override
	public void init(IfcModelInterface model, ProjectInfo projectInfo, boolean normalizeOids) throws SerializerException {
		setHeaderSchema("IFC2X3");
		super.init(model, projectInfo, normalizeOids);
	}
}
