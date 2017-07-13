package cn.dlb.bim;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import cn.dlb.bim.component.PlatformServerConfig;

public class PlatformServerStarter {
	
	public static void main(String[] args) {  
        try {  
        	Server jettyServer = createDevServer();
            jettyServer.start();  
            jettyServer.join();  
  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }  
	
    public static Server createDevServer() {
    	PlatformServerConfig.DEV_MODE = true;
    	Path projectPath = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
    	Server jettyServer = new Server(8080);  
        WebAppContext context = new WebAppContext();  
        context.setContextPath("/BIMplatform");
        context.setDescriptor(projectPath.resolve("WebContent/WEB-INF/web.xml").toAbsolutePath().toString()); // 指定web.xml配置文件 
        context.setResourceBase(projectPath.resolve("WebContent/").toAbsolutePath().toString());// 指定webapp目录  
        context.setParentLoaderPriority(true); 
        jettyServer.setHandler(context);  

        return jettyServer;
    }
}
