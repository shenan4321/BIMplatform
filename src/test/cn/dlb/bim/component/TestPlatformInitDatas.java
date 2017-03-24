package test.cn.dlb.bim.component;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
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
import cn.dlb.bim.models.ifc2x3tc1.Ifc2x3tc1Package;
import cn.dlb.bim.service.IIfcStoreService;
import cn.dlb.bim.service.impl.BimServiceImpl;
import cn.dlb.bim.utils.IdentifyUtil;

public class TestPlatformInitDatas {
	
	private PlatformInitDatas platformInitDatas;
	 
	@Before
	public void before(){                                                                   
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:conf/spring.xml"
	    		,"classpath:conf/spring-mybatis.xml"});
		platformInitDatas = (PlatformInitDatas) context.getBean("PlatformInitDatas");
	}
	 
	@Test
	public void testNewOid(){
		
		EClass[] eClassList = new EClass[]{
				Ifc2x3tc1Package.eINSTANCE.getIfcAnnotationSurface(),
				Ifc2x3tc1Package.eINSTANCE.getIfc2DCompositeCurve(),
				Ifc2x3tc1Package.eINSTANCE.getIfcBoilerType(),
				Ifc2x3tc1Package.eINSTANCE.getIfcAbsorbedDoseMeasure(),
				Ifc2x3tc1Package.eINSTANCE.getIfcAngularDimension(),
				Ifc2x3tc1Package.eINSTANCE.getIfcServiceLife()
		};
		
		for (EClass eclass : eClassList) {
			long oid = platformInitDatas.newOid(eclass);
			System.out.println(oid);
		}
	}
	    
}
