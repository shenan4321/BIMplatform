package test.cn.dlb.bim.dao;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.DB;
import com.mongodb.Mongo;

import cn.dlb.bim.dao.IIfcObjectDao;
import cn.dlb.bim.dao.entity.IfcStoreModel;

public class TestIfcObjectDaoImpl {
	
	private IIfcObjectDao dao;
	
	@Before
	public void before(){                                                                   
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:conf/spring.xml"});
		dao = (IIfcObjectDao) context.getBean("IfcObjectDaoImpl");
	}
	 
	@Test
	public void testNewOid(){
		IfcStoreModel ifcStoreModel = new IfcStoreModel();
		ifcStoreModel.setGid(1l);
		ifcStoreModel.setIfcObjectBytes(new String("sadjkahsjk").getBytes());
		dao.insertIfcStoreModel(ifcStoreModel);
	}
	
	
}
