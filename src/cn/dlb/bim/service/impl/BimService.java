package cn.dlb.bim.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import cn.dlb.bim.common.BimFactory;
import cn.dlb.bim.common.CommonContext;
import cn.dlb.bim.common.GeometryGenerator;
import cn.dlb.bim.deserializers.DeserializeException;
import cn.dlb.bim.deserializers.IfcStepDeserializer;
import cn.dlb.bim.emf.IfcModelInterface;
import cn.dlb.bim.emf.Schema;
import cn.dlb.bim.engine.IRenderEngine;
import cn.dlb.bim.engine.IRenderEngineFactory;
import cn.dlb.bim.engine.RenderEngineException;
import cn.dlb.bim.serializers.IfcStepSerializer;
import cn.dlb.bim.service.IBimService;

@Service("BimService")
public class BimService implements IBimService {

	@Autowired
	@Qualifier("CommonContext")
	private CommonContext commonContext;
	
	@Autowired
	@Qualifier("BimFactory")
	private BimFactory bimFactory;
	
	@Autowired
	@Qualifier("RenderEngineFactory")
	private IRenderEngineFactory renderEngineFactory;
	
	@Override
	public List<IfcModelInterface> queryAllIfcModel() {

		Schema schema = Schema.IFC2X3TC1;
		File[] ifcFiles = getIfcFileList();
		
		List<IfcModelInterface> modelList = new ArrayList<>();
		
		for (File file : ifcFiles) {
			
			IfcStepDeserializer deserializer = bimFactory.createIfcStepDeserializer(schema);
			IfcStepSerializer serializer = bimFactory.createIfcStepSerializer(schema);
			
			try {
				deserializer.read(file);
				IfcModelInterface model = deserializer.getModel();
				
				IRenderEngine renderEngine = renderEngineFactory.createRenderEngine(schema.getEPackageName());
				
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

	public File[] getIfcFileList() {
		File dir = commonContext.getRootPath().resolve("file/").toAbsolutePath().toFile(); 
		if (dir.isDirectory()) {
			return dir.listFiles();
		} 
		return null;
	}
	
}
