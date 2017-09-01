package cn.dlb.bim.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import cn.dlb.bim.websocket.StreamGeometryInterceptor;
import cn.dlb.bim.websocket.StreamGeometrySocketHandler;

@Configuration  
@EnableWebMvc
@EnableWebSocket 
public class WebSocketConfig implements WebSocketConfigurer {
	
	@Autowired
	private BinaryGeometrySocketHandler binaryGeometrySocketHandler;
	
	@Autowired
	private BinaryGeometryTemplateSocketHandler binaryGeometryTemplateSocketHandler;
	
	@Autowired
	private GeometrySocketHandler geometrySocketHandler;
	
	@Autowired
	private StreamGeometrySocketHandler streamGeometrySocketHandler;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        /** 
         * 支持websocket 的 connection 
         */  
        registry.addHandler(binaryGeometrySocketHandler,"/ws/binarygeometry").setAllowedOrigins("*").addInterceptors(new BinaryGeometryInterceptor());  
        
        /** 
         * 支持websocket 的 connection 
         */  
        registry.addHandler(binaryGeometryTemplateSocketHandler,"/ws/binarygeometry/template").setAllowedOrigins("*").addInterceptors(new BinaryGeometryTemplateInterceptor()); 
        
        /** 
         * 如不支持websocket的connenction,采用sockjs 
         */  
        registry.addHandler(geometrySocketHandler,"/ws/geometry").setAllowedOrigins("*").addInterceptors(new GeometryInterceptor());
        //.withSockJS();
        
      	registry.addHandler(streamGeometrySocketHandler,"/ws/streamgeometry").setAllowedOrigins("*").addInterceptors(new StreamGeometryInterceptor());
	}

}
