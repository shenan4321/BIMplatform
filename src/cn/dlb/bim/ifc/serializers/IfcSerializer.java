package cn.dlb.bim.ifc.serializers;

import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.ProjectInfo;
import nl.tue.buildingsmart.schema.SchemaDefinition;

public abstract class IfcSerializer extends EmfSerializer {

	private SchemaDefinition schemaDefinition;

	@Override
	public void init(IfcModelInterface model, ProjectInfo projectInfo, boolean normalizeOids) throws SerializerException {
		super.init(model, projectInfo, normalizeOids);
	}
	
	protected void setSchema(SchemaDefinition schemaDefinition) {
		this.schemaDefinition = schemaDefinition;
	}
	
	protected SchemaDefinition getSchemaDefinition() {
		return schemaDefinition;
	}
}