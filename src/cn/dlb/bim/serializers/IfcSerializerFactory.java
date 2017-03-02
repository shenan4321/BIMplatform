package cn.dlb.bim.serializers;

import cn.dlb.bim.emf.Schema;

public class IfcSerializerFactory {
	private static IfcSerializerFactory factory = null;
	
	public static IfcSerializerFactory getInstance() {
		if (factory == null) {
			factory = new IfcSerializerFactory();
		} 
		return factory;
	}
	
	public IfcStepSerializer createIfcStepSerializer(Schema schema) {
		if (schema == Schema.IFC4) {
			return new Ifc4StepSerializer();
		} else {
			return new Ifc2x3tc1StepSerializer();
		}
	}
}
