package cn.dlb.bim.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import cn.dlb.bim.service.IBimService;
import cn.dlb.bim.websocket.DlbHandshakeInterceptor;
import cn.dlb.bim.websocket.DlbWebSocketHandler;
import cn.dlb.bim.websocket.GeometryInterceptor;
import cn.dlb.bim.websocket.GeometrySocketHandler;

@Configuration  
@EnableWebMvc
@EnableWebSocket 
public class WebSocketConfig implements WebSocketConfigurer {
	
	@Autowired
	IBimService bimservice;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        /** 
         * 支持websocket 的 connection 
         */  
//        registry.addHandler(new DlbWebSocketHandler(),"/ws").setAllowedOrigins("*").addInterceptors(new DlbHandshakeInterceptor());  
          
        /** 
         * 如不支持websocket的connenction,采用sockjs 
         */  
        registry.addHandler(new GeometrySocketHandler(bimservice),"/ws/geometry").setAllowedOrigins("*").addInterceptors(new GeometryInterceptor());
        //.withSockJS();
	}

}
