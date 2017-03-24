package test.cn.dlb.bim.service;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.dlb.bim.component.PlatformInitDatas;
import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.ifc.database.OldQuery;
import cn.dlb.bim.ifc.database.binary.IfcModelBinary;
import cn.dlb.bim.ifc.database.binary.IfcModelBinaryException;
import cn.dlb.bim.ifc.database.binary.TodoList;
import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.Schema;
import cn.dlb.bim.ifc.model.BasicIfcModel;
import cn.dlb.bim.ifc.model.SplitIfcModel;
import cn.dlb.bim.models.ifc2x3tc1.Ifc2x3tc1Package;
import cn.dlb.bim.models.ifc2x3tc1.IfcProduct;
import cn.dlb.bim.models.ifc2x3tc1.impl.Ifc2x3tc1PackageImpl;
import cn.dlb.bim.service.IIfcStoreService;
import cn.dlb.bim.service.impl.BimServiceImpl;

public class TestBimService {
	
	private IIfcStoreService ifcStoreService;
	private BimServiceImpl bimService;
	private PlatformServer server;
	private PlatformInitDatas datas;
	 
	@Before
	public void before(){                                                                   
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:conf/spring.xml"});
		ifcStoreService = (IIfcStoreService) context.getBean("IfcStoreService");
		bimService = (BimServiceImpl) context.getBean("BimService");
		server = (PlatformServer) context.getBean("PlatformServer");
		datas = (PlatformInitDatas) context.getBean("PlatformInitDatas");
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
    		TodoList todoList = new TodoList();
    		for (IdEObject obj : values) {
    			ByteBuffer byteBuffer = ByteBuffer.allocate(16);
    			model.fixOids(datas);
    			IfcModelBinary ifcModelBinary = new IfcModelBinary(datas);
    			byteBuffer = ifcModelBinary.convertObjectToByteArray(obj, byteBuffer, packageMetaData);
    			byteBuffer.flip();
    			BasicIfcModel newModel = new BasicIfcModel(packageMetaData);
    			
    			OldQuery query = new OldQuery(packageMetaData, true);
    			IdEObject idEObject = ifcModelBinary.convertByteArrayToObject(null, obj.eClass(), -1, byteBuffer, newModel, query, todoList);
    			System.out.println(idEObject.getExpressId());
    		}
    		System.out.println();
    	}
    }
    
    @Test
    public void testSplite() {
    	List<IfcModelInterface> modelList = bimService.queryAllIfcModel();
    	if (modelList.size() > 0) {
    		IfcModelInterface model = modelList.get(0);
    		PackageMetaData packageMetaData = server.getMetaDataManager().getPackageMetaData(Schema.IFC2X3TC1.getEPackageName());
    		SplitIfcModel splitModel = new SplitIfcModel(packageMetaData);
    		EClassifier classifier = Ifc2x3tc1Package.eINSTANCE.getEClassifier("IfcBuildingStorey");
    		List<IdEObject> objectList = model.getAllWithSubTypes(((EClass)classifier));
    		for (IdEObject object : objectList) {
    			Map<Integer, IdEObject> result = new LinkedHashMap<>();
    			splitModel.processObject(object, result);
    			model.getAllWithSubTypes(IfcProduct.class);
    			System.out.println();
    		}
    		
    	}
    }
	    
}
