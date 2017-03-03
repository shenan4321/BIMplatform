package cn.dlb.bim.common;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class PlatformServer {
	public static void main(String[] args) {  
        String filePath = System.getProperty("user.dir");
        System.out.println(System.getProperty("user.dir"));  
  
        try {  
            Server server = new Server(8080);  
  
            WebAppContext context = new WebAppContext();  
            context.setContextPath("/BIMplatform");  
            context.setDescriptor(filePath + "/WebContent/WEB-INF/web.xml"); // 指定web.xml配置文件  
            context.setResourceBase(filePath + "/WebContent/");// 指定webapp目录  
            context.setParentLoaderPriority(true);  
  
            server.setHandler(context);  
            server.start();  
            server.join();  
  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
}
