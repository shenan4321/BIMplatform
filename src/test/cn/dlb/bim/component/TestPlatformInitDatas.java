package test.cn.dlb.bim.component;

import org.eclipse.emf.ecore.EClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cn.dlb.bim.dao.BaseMongoDao;
import cn.dlb.bim.dao.entity.ConcreteRevision;
import cn.dlb.bim.models.ifc2x3tc1.Ifc2x3tc1Package;
import cn.dlb.bim.service.impl.CatalogServiceImpl;

@RunWith(SpringJUnit4ClassRunner.class)  
@ContextConfiguration(locations={"classpath:conf/spring.xml"})  
public class TestPlatformInitDatas {
	
	@Autowired
	@Qualifier("ConcreteRevisionDaoImpl")
	private BaseMongoDao<ConcreteRevision> dao;
	 
//	@Before
//	public void before(){                                                                   
//		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:conf/spring.xml"
//	    		,//"classpath:conf/spring-mvc.xml"
//	    		});
//		platformInitDatas = (PlatformServiceImpl) context.getBean("PlatformInitDatas");
//	}
	 
	@Test
	public void testNewOid(){
		
	}
	    
}
