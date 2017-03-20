package test.cn.dlb.bim.service;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.dlb.bim.component.PlatformInitDatas;
import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.ifc.binary.IfcModelBinary;
import cn.dlb.bim.ifc.binary.IfcModelBinaryException;
import cn.dlb.bim.ifc.binary.OldQuery;
import cn.dlb.bim.ifc.binary.TodoList;
import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.Schema;
import cn.dlb.bim.ifc.model.BasicIfcModel;
import cn.dlb.bim.service.IIfcStoreService;
import cn.dlb.bim.service.impl.BimServiceImpl;

public class TestBimService {
	
	private IIfcStoreService ifcStoreService;
	private BimServiceImpl bimService;
	private PlatformServer server;
	private PlatformInitDatas datas;
	 
	@Before
	public void before(){                                                                   
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:conf/spring.xml"
	    		,"classpath:conf/spring-mybatis.xml"});
		ifcStoreService = (IIfcStoreService) context.getBean("IfcStoreService");
		bimService = (BimServiceImpl) context.getBean("BimService");
		server = (PlatformServer) context.getBean("PlatformServer");
	}
	 
	@Test
	public void insert(){
		List<IfcModelInterface> modelList = bimService.queryAllIfcModel();
		if (modelList.size() > 0) {
			ifcStoreService.insert(modelList.get(0));
		}
		
		System.out.println();
		
	}
	
    @Test
    public void testConvert() throws IfcModelBinaryException {
    	List<IfcModelInterface> modelList = bimService.queryAllIfcModel();
    	if (modelList.size() > 0) {
    		IfcModelInterface model = modelList.get(0);
    		Collection<IdEObject> values = model.getValues();
    		PackageMetaData packageMetaData = server.getMetaDataManager().getPackageMetaData(Schema.IFC2X3TC1.getEPackageName());
    		for (IdEObject obj : values) {
    			ByteBuffer byteBuffer = ByteBuffer.allocate(16);
    			IfcModelBinary ifcModelBinary = new IfcModelBinary(datas);
    			byteBuffer = ifcModelBinary.convertObjectToByteArray(obj, byteBuffer, packageMetaData);
    			byteBuffer.flip();
    			BasicIfcModel newModel = new BasicIfcModel(packageMetaData);
    			TodoList todoList = new TodoList();
    			OldQuery query = new OldQuery(packageMetaData, true);
    			IdEObject idEObject = ifcModelBinary.convertByteArrayToObject(null, obj.eClass(), -1, byteBuffer, newModel, query, todoList);
    			String byStr = byteBuffer.toString();
    		}
    	}
    }
	    
}
