package test.cn.dlb.bim.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
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
import cn.dlb.bim.ifc.database.binary.IfcModelBinary;
import cn.dlb.bim.ifc.database.binary.TodoList;
import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.IfcModelInterfaceException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.Schema;
import cn.dlb.bim.ifc.model.BasicIfcModel;
import cn.dlb.bim.ifc.model.SplitIfcModel;
import cn.dlb.bim.ifc.serializers.IfcStepSerializer;
import cn.dlb.bim.ifc.serializers.SerializerException;
import cn.dlb.bim.models.geometry.GeometryInfo;
import cn.dlb.bim.models.ifc2x3tc1.Ifc2x3tc1Package;
import cn.dlb.bim.models.ifc2x3tc1.IfcProduct;
import cn.dlb.bim.service.IIfcStoreService;
import cn.dlb.bim.service.impl.BimServiceImpl;

public class TestBimService {

	private IIfcStoreService ifcStoreService;
	private BimServiceImpl bimService;
	private PlatformServer server;
	private PlatformInitDatas datas;
	private MongoGridFs mongoGridFs;

	@Before
	public void before() {
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "classpath:conf/spring.xml" });
		ifcStoreService = (IIfcStoreService) context.getBean("IfcStoreService");
		bimService = (BimServiceImpl) context.getBean("BimService");
		server = (PlatformServer) context.getBean("PlatformServer");
		datas = (PlatformInitDatas) context.getBean("PlatformInitDatas");
		mongoGridFs = (MongoGridFs) context.getBean("MongoGridFs");
	}

	@Test
	public void insert() {
		List<IfcModelInterface> modelList = bimService.queryAllIfcModel();
		if (modelList.size() > 0) {
			ifcStoreService.insert(modelList.get(0));
		}

		System.out.println();

	}

	@Test
	public void testConvert() throws IfcModelDbException {
		List<IfcModelInterface> modelList = bimService.queryAllIfcModel();
		if (modelList.size() > 0) {
			IfcModelInterface model = modelList.get(0);
			Collection<IdEObject> values = model.getValues();
			PackageMetaData packageMetaData = server.getMetaDataManager()
					.getPackageMetaData(Schema.IFC2X3TC1.getEPackageName());
			TodoList todoList = new TodoList();
			for (IdEObject obj : values) {
				ByteBuffer byteBuffer = ByteBuffer.allocate(16);
				model.fixOids(datas);
				IfcModelBinary ifcModelBinary = new IfcModelBinary(datas);
				byteBuffer = ifcModelBinary.convertObjectToByteArray(obj, byteBuffer, packageMetaData);
				byteBuffer.flip();
				BasicIfcModel newModel = new BasicIfcModel(packageMetaData);

				OldQuery query = new OldQuery(packageMetaData, true);
				IdEObject idEObject = ifcModelBinary.convertByteArrayToObject(null, obj.eClass(), -1, byteBuffer,
						newModel, -1, query, todoList);
				System.out.println(idEObject.getExpressId());
			}
			System.out.println();
		}
	}
	
	@Test
	public void testConvertGeometryInfo() throws IfcModelDbException {
		List<IfcModelInterface> modelList = bimService.queryAllIfcModel();
		if (modelList.size() > 0) {
			IfcModelInterface model = modelList.get(0);
			Collection<IdEObject> values = model.getValues();
			PackageMetaData packageMetaData = server.getMetaDataManager()
					.getPackageMetaData(Schema.IFC2X3TC1.getEPackageName());
			TodoList todoList = new TodoList();
			model.fixOids(datas);
			for (IfcProduct ifcProduct : model.getAllWithSubTypes(IfcProduct.class)) {
				if (ifcProduct.getRepresentation() != null && ifcProduct.getRepresentation().getRepresentations().size() != 0) {
					GeometryInfo geometryInfo = ifcProduct.getGeometry();
					ByteBuffer byteBuffer = ByteBuffer.allocate(16);
					IfcModelBinary ifcModelBinary = new IfcModelBinary(datas);
					byteBuffer = ifcModelBinary.convertObjectToByteArray(geometryInfo, byteBuffer, packageMetaData);
					byteBuffer.flip();
					BasicIfcModel newModel = new BasicIfcModel(packageMetaData);

					OldQuery query = new OldQuery(packageMetaData, true);
					IdEObject idEObject = ifcModelBinary.convertByteArrayToObject(null, ifcProduct.eClass(), -1, byteBuffer,
							newModel, -1, query, todoList);
					System.out.println(idEObject.getExpressId());
				}
			}
		}
	}

	@Test
	public void testIfcModelDbSessionSave() throws IfcModelDbException, IfcModelInterfaceException {
		List<IfcModelInterface> modelList = bimService.queryAllIfcModel();
		if (modelList.size() > 0) {
			IfcModelInterface model = modelList.get(0);
			model.fixOids(datas);
			IfcModelDbSession session = new IfcModelDbSession(mongoGridFs, server.getMetaDataManager(), datas);
			session.saveIfcModel(model);
			System.out.println();
		}
	}

	@Test
	public void testIfcModelDbSessionGet() throws IfcModelDbException, IfcModelInterfaceException, FileNotFoundException, SerializerException {
		PackageMetaData packageMetaData = server.getMetaDataManager()
				.getPackageMetaData(Schema.IFC2X3TC1.getEPackageName());
		IfcModelDbSession session = new IfcModelDbSession(mongoGridFs, server.getMetaDataManager(), datas);
		BasicIfcModel newModel = new BasicIfcModel(packageMetaData);
		session.get(3, newModel, new OldQuery(packageMetaData, true));
		IfcStepSerializer serializer = server.getSerializationManager().createIfcStepSerializer(Schema.IFC2X3TC1);
		serializer.init(newModel, null, true);
		FileOutputStream fos = new FileOutputStream(new File("D:\\test.ifc"));
		serializer.writeToOutputStream(fos, null);
		System.out.println();
	}
	
	@Test
	public void testSplite() {
		List<IfcModelInterface> modelList = bimService.queryAllIfcModel();
		if (modelList.size() > 0) {
			IfcModelInterface model = modelList.get(0);
			PackageMetaData packageMetaData = server.getMetaDataManager()
					.getPackageMetaData(Schema.IFC2X3TC1.getEPackageName());
			SplitIfcModel splitModel = new SplitIfcModel(packageMetaData);
			EClassifier classifier = Ifc2x3tc1Package.eINSTANCE.getEClassifier("IfcBuildingStorey");
			List<IdEObject> objectList = model.getAllWithSubTypes(((EClass) classifier));
			for (IdEObject object : objectList) {
				Map<Integer, IdEObject> result = new LinkedHashMap<>();
				splitModel.processObject(object, result);
				model.getAllWithSubTypes(IfcProduct.class);
				System.out.println();
			}

		}
	}

}
