package cn.dlb.bim.ifc;

import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.ifc.deserializers.Ifc2x3tc1StepDeserializer;
import cn.dlb.bim.ifc.deserializers.Ifc4StepDeserializer;
import cn.dlb.bim.ifc.deserializers.IfcStepDeserializer;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.Schema;
import cn.dlb.bim.ifc.serializers.Ifc2x3tc1StepSerializer;
import cn.dlb.bim.ifc.serializers.Ifc4StepSerializer;
import cn.dlb.bim.ifc.serializers.IfcStepSerializer;

public class SerializationManager {
	
	private PlatformServer server;
	
	public SerializationManager(PlatformServer server) {
		this.server = server;
	}
	
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
		PackageMetaData packageMetaData = server.getMetaDataManager().getPackageMetaData(schema.getEPackageName());
		if (schema == Schema.IFC4) {
			deserializer = new Ifc4StepDeserializer(schema);
			
		} else {
			deserializer = new Ifc2x3tc1StepDeserializer(schema);
		}
		deserializer.init(packageMetaData);
		return deserializer;
	}
	
	
	
}
