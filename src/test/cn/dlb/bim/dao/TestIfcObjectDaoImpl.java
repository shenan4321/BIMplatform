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

import cn.dlb.bim.dao.IfcModelDao;
import cn.dlb.bim.dao.entity.IdEObjectEntity;

public class TestIfcObjectDaoImpl {
	
	private IfcModelDao dao;
	
	@Before
	public void before(){                                                                   
		ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:conf/spring.xml"});
		dao = (IfcModelDao) context.getBean("IdEObjectDaoImpl");
	}
	 
	@Test
	public void testNewOid(){
		IdEObjectEntity ifcStoreModel = new IdEObjectEntity();
//		ifcStoreModel.setGid(1l);
//		ifcStoreModel.setIfcObjectBytes(new String("sadjkahsjk").getBytes());
//		dao.insertIfcStoreModel(ifcStoreModel);
	}
	
	
}
