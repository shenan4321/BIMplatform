package cn.dlb.bim.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.dlb.bim.PlatformContext;
import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.ifc.GeometryGenerator;
import cn.dlb.bim.ifc.deserializers.DeserializeException;
import cn.dlb.bim.ifc.deserializers.IfcStepDeserializer;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.Schema;
import cn.dlb.bim.ifc.engine.IRenderEngine;
import cn.dlb.bim.ifc.engine.IRenderEngineFactory;
import cn.dlb.bim.ifc.engine.RenderEngineException;
import cn.dlb.bim.ifc.serializers.IfcStepSerializer;
import cn.dlb.bim.models.ifc2x3tc1.IfcProduct;
import cn.dlb.bim.service.IBimService;
import cn.dlb.bim.vo.GeometryInfoVo;

@Service("BimService")
public class BimServiceImpl implements IBimService {
	
	@Autowired
	@Qualifier("PlatformServer")
	private PlatformServer server;
	
	@Override
	public List<IfcModelInterface> queryAllIfcModel() {

		Schema schema = Schema.IFC2X3TC1;
		File[] ifcFiles = getIfcFileList();
		
		List<IfcModelInterface> modelList = new ArrayList<>();
		
		for (File file : ifcFiles) {
			
			IfcStepDeserializer deserializer = server.getSerializationManager().createIfcStepDeserializer(schema);
			IfcStepSerializer serializer = server.getSerializationManager().createIfcStepSerializer(schema);
			
			try {
				deserializer.read(file);
				IfcModelInterface model = deserializer.getModel();
				
				IRenderEngine renderEngine = server.getRenderEngineFactory().createRenderEngine(schema.getEPackageName());
				
				GeometryGenerator generator = new GeometryGenerator(model, serializer, renderEngine);
				generator.generateForAllElements();
				
				modelList.add(model);
			} catch (DeserializeException e) {
				e.printStackTrace();
			} catch (RenderEngineException e) {
				e.printStackTrace();
			}
			
		}
		return modelList;
	}
	
	@Override
	public List<GeometryInfoVo> queryGeometryInfo() {
		
		Schema schema = Schema.IFC2X3TC1;
		File[] ifcFiles = getIfcFileList();
		
		List<GeometryInfoVo> geometryList = new ArrayList<>();
		
		if (ifcFiles == null) {
			return geometryList;
		}
		
		IfcStepDeserializer deserializer = server.getSerializationManager().createIfcStepDeserializer(schema);
		IfcStepSerializer serializer = server.getSerializationManager().createIfcStepSerializer(schema);
		
		try {
			deserializer.read(ifcFiles[0]);
			IfcModelInterface model = deserializer.getModel();
			
			IRenderEngine renderEngine = server.getRenderEngineFactory().createRenderEngine(schema.getEPackageName());
			
			GeometryGenerator generator = new GeometryGenerator(model, serializer, renderEngine);
			generator.generateForAllElements();
		
			for (IfcProduct ifcProduct : model.getAllWithSubTypes(IfcProduct.class)) {
				if (ifcProduct.getRepresentation() != null && ifcProduct.getRepresentation().getRepresentations().size() != 0) {
					
//					GeometryInfo info = ifcProduct.getGeometry();
					GeometryInfoVo adaptor = new GeometryInfoVo();
					adaptor.adapt(ifcProduct);
					geometryList.add(adaptor);
				}
			}
		} catch (DeserializeException e) {
			e.printStackTrace();
		} catch (RenderEngineException e) {
			e.printStackTrace();
		}
		
		return geometryList;
	}

	public File[] getIfcFileList() {
		File dir = PlatformContext.getClassRootPath().resolve("file/").toAbsolutePath().toFile(); 
		if (dir.isDirectory()) {
			return dir.listFiles();
		} 
		return null;
	}
	
}
