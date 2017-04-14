package test.cn.dlb.bim.component;

import org.eclipse.emf.ecore.EClass;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.dlb.bim.component.PlatformInitDatas;
import cn.dlb.bim.models.ifc2x3tc1.Ifc2x3tc1Package;

//@Transactional  
//@TransactionConfiguration(transactionManager = "txManager", defaultRollback = true)  
//@RunWith(SpringJUnit4ClassRunner.class)  
//@ContextConfiguration(locations={"classpath:conf/spring.xml","classpath:conf/spring-mvc.xml"})  
public class TestPlatformInitDatas {
	
//	@Resource(name="PlatformInitDatas")  
	private PlatformInitDatas platformInitDatas;
	 
	@Before
	public void before(){                                                                   
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:conf/spring.xml"
	    		,//"classpath:conf/spring-mvc.xml"
	    		});
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
