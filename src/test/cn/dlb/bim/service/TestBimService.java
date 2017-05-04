package test.cn.dlb.bim.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.dlb.bim.component.MongoGridFs;
import cn.dlb.bim.component.PlatformInitDatas;
import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.dao.IfcModelDao;
import cn.dlb.bim.ifc.database.IfcModelDbException;
import cn.dlb.bim.ifc.database.IfcModelDbSession;
import cn.dlb.bim.ifc.database.OldQuery;
import cn.dlb.bim.ifc.emf.IfcModelInterfaceException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.Schema;
import cn.dlb.bim.ifc.model.BasicIfcModel;
import cn.dlb.bim.ifc.serializers.IfcStepSerializer;
import cn.dlb.bim.ifc.serializers.SerializerException;
import cn.dlb.bim.service.impl.BimServiceImpl;

public class TestBimService {

	private BimServiceImpl bimService;
	private PlatformServer server;
	private PlatformInitDatas datas;
	private MongoGridFs mongoGridFs;
	private IfcModelDao ifcModelDao;

	@Before
	public void before() {
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "classpath:conf/spring.xml" });
		bimService = (BimServiceImpl) context.getBean("BimService");
		server = (PlatformServer) context.getBean("PlatformServer");
		datas = (PlatformInitDatas) context.getBean("PlatformInitDatas");
		mongoGridFs = (MongoGridFs) context.getBean("MongoGridFs");
	}

	@Test
	public void testIfcModelDbSessionGet() throws IfcModelDbException, IfcModelInterfaceException, FileNotFoundException, SerializerException {
		PackageMetaData packageMetaData = server.getMetaDataManager()
				.getPackageMetaData(Schema.IFC2X3TC1.getEPackageName());
		IfcModelDbSession session = new IfcModelDbSession(ifcModelDao, server.getMetaDataManager(), datas);
		BasicIfcModel newModel = new BasicIfcModel(packageMetaData);
		session.get(3, newModel, new OldQuery(packageMetaData, true));
		IfcStepSerializer serializer = server.getSerializationManager().createIfcStepSerializer(Schema.IFC2X3TC1);
		serializer.init(newModel, null, true);
		FileOutputStream fos = new FileOutputStream(new File("D:\\test.ifc"));
		serializer.writeToOutputStream(fos, null);
		System.out.println();
	}
	
}