package cn.dlb.bim.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import cn.dlb.bim.service.BimService;
import cn.dlb.bim.websocket.BinaryGeometryInterceptor;
import cn.dlb.bim.websocket.BinaryGeometrySocketHandler;
import cn.dlb.bim.websocket.BinaryGeometryTemplateInterceptor;
import cn.dlb.bim.websocket.BinaryGeometryTemplateSocketHandler;
import cn.dlb.bim.websocket.GeometryInterceptor;
import cn.dlb.bim.websocket.GeometrySocketHandler;

@Configuration  
@EnableWebMvc
@EnableWebSocket 
public class WebSocketConfig implements WebSocketConfigurer {
	
	@Autowired
	private PlatformServer server;
	@Autowired
	private BimService bimService;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        /** 
         * 支持websocket 的 connection 
         */  
        registry.addHandler(new BinaryGeometrySocketHandler(server, bimService),"/ws/binarygeometry").setAllowedOrigins("*").addInterceptors(new BinaryGeometryInterceptor());  
        
        /** 
         * 支持websocket 的 connection 
         */  
        registry.addHandler(new BinaryGeometryTemplateSocketHandler(server, bimService),"/ws/binarygeometry/template").setAllowedOrigins("*").addInterceptors(new BinaryGeometryTemplateInterceptor()); 
        
        /** 
         * 如不支持websocket的connenction,采用sockjs 
         */  
        registry.addHandler(new GeometrySocketHandler(server, bimService),"/ws/geometry").setAllowedOrigins("*").addInterceptors(new GeometryInterceptor());
        //.withSockJS();
	}

}
