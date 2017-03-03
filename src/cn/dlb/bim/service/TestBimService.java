package cn.dlb.bim.service;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.dlb.bim.emf.IfcModelInterface;
import cn.dlb.bim.models.geometry.GeometryInfo;

public class TestBimService {
	
		private IBimService service;
	     
	    @Before
	    public void before(){                                                                   
	        ApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"classpath:conf/spring.xml"});
	        service = (IBimService) context.getBean("BimService");
	    }
	     
	    @Test
	    public void queryModel(){
	    	List<IfcModelInterface> models = service.queryAllIfcModel();
	    	System.out.println();
	    	
	    }
	    
	    @Test
	    public void queryGeometryInfo(){
	    	List<GeometryInfo> geos = service.queryGeometryInfo();
	    	System.out.println();
	    	
	    }
}
