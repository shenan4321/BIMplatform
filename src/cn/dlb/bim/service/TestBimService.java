package cn.dlb.bim.service;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.dlb.bim.emf.IfcModelInterface;
import cn.dlb.bim.service.impl.BimService;

public class TestBimService {
	
		private IIfcStoreService ifcStoreService;
		
		private BimService bimService;
	     
	    @Before
	    public void before(){                                                                   
	    	ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:conf/spring.xml"
	        		,"classpath:conf/spring-mybatis.xml"});
	    	ifcStoreService = (IIfcStoreService) context.getBean("IfcStoreService");
	    	bimService = (BimService) context.getBean("BimService");
	    }
	     
	    @Test
	    public void insert(){
	    	List<IfcModelInterface> modelList = bimService.queryAllIfcModel();
	    	if (modelList.size() > 0) {
	    		ifcStoreService.insert(modelList.get(0));
	    	}
	    	
	    	System.out.println();
	    	
	    }
	    
}
