package cn.dlb.bim;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class PlatformServerStarter {
	
	public static void main(String[] args) {  
        try {  
            Server jettyServer = new Server(PlatformContext.getServerPort());  
            WebAppContext context = new WebAppContext();  
            context.setContextPath(PlatformContext.getContextPath());
            context.setDescriptor(PlatformContext.getDescriptor()); // 指定web.xml配置文件 
            context.setResourceBase(PlatformContext.getResourceBase());// 指定webapp目录  
            context.setParentLoaderPriority(true);  
            
            jettyServer.setHandler(context);  
            jettyServer.start();  
            jettyServer.join();  
  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
}
