package test.cn.dlb.bim.dao;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TestIfcObjectDaoImpl {
	
	@Before
	public void before(){                                                                   
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:conf/spring.xml"});
	}
	 
	@Test
	public void testNewOid(){
//		ifcStoreModel.setGid(1l);
//		ifcStoreModel.setIfcObjectBytes(new String("sadjkahsjk").getBytes());
//		dao.insertIfcStoreModel(ifcStoreModel);
	}
	
	
}
