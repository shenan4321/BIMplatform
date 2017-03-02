package cn.dlb.bim.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import cn.dlb.bim.deserializers.Ifc2x3tc1StepDeserializer;
import cn.dlb.bim.deserializers.Ifc4StepDeserializer;
import cn.dlb.bim.deserializers.IfcStepDeserializer;
import cn.dlb.bim.emf.PackageMetaData;
import cn.dlb.bim.emf.Schema;
import cn.dlb.bim.serializers.Ifc2x3tc1StepSerializer;
import cn.dlb.bim.serializers.Ifc4StepSerializer;
import cn.dlb.bim.serializers.IfcStepSerializer;

@Component("BimFactory")
public class BimFactory {
	
	@Autowired
	@Qualifier("CommonContext")
	private CommonContext commonContext;
	
	public IfcStepSerializer createIfcStepSerializer(Schema schema) {
		IfcStepSerializer serializer = null;
		if (schema == Schema.IFC4) {
			serializer = new Ifc4StepSerializer();
		} else {
			serializer = new Ifc2x3tc1StepSerializer();
		}
		return serializer;
	}
	
	public IfcStepDeserializer createIfcStepDeserializer(Schema schema) {
		IfcStepDeserializer deserializer = null;
		PackageMetaData packageMetaData = commonContext.getMetaDataManager().getPackageMetaData(schema.getEPackageName());
		if (schema == Schema.IFC4) {
			deserializer = new Ifc4StepDeserializer(schema);
			
		} else {
			deserializer = new Ifc2x3tc1StepDeserializer(schema);
		}
		deserializer.init(packageMetaData);
		return deserializer;
	}
	
	
	
}
