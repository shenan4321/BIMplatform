package cn.dlb.bim.ifc.serializers;

import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.ProjectInfo;

public class Ifc4StepSerializer extends IfcStepSerializer {

	public Ifc4StepSerializer() {
		super();
	}
	
	@Override
	public void init(IfcModelInterface model, ProjectInfo projectInfo, boolean normalizeOids) throws SerializerException {
		setHeaderSchema("IFC4");
		super.init(model, projectInfo, normalizeOids);
	}
}