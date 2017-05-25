package cn.dlb.bim.websocket;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import cn.dlb.bim.action.LongGeometryQueryAction;
import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.service.BimService;

public class GeometrySocketHandler implements WebSocketHandler {
	private static final Logger logger = LoggerFactory.getLogger(GeometrySocketHandler.class);  
    
    private static final List<WebSocketSession> users = new ArrayList<WebSocketSession>();
	private final BimService bimService;
	private final PlatformServer server;
	
	public GeometrySocketHandler(PlatformServer server, BimService bimService) {
		this.server = server;
		this.bimService = bimService;
	}
  
    /** 
     * after connection establish 
     */  
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {  
    	users.add(session);
        logger.info("connect success...");  
        String rid = session.getAttributes().get("rid").toString();
        Integer ridInt = Integer.valueOf(rid);
        LongGeometryQueryAction longAction = new LongGeometryQueryAction(bimService, ridInt, session);
        server.getLongActionManager().startLongAction(longAction);
    }  
  
    /** 
     * process the received message  
     */  
    @Override  
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {  
    	return;
    }  
  
    @Override  
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception { 
    	users.remove(webSocketSession);
//        server.getLongActionManager().cancelLongActionByWebSocketSessionRelated(webSocketSession);
        if(webSocketSession.isOpen()){  
        	
            webSocketSession.close();  
        }  
        logger.info("connenction error,close the connection...");  
    }  
  
    @Override  
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {  
    	users.remove(webSocketSession);
//    	server.getLongActionManager().cancelLongActionByWebSocketSessionRelated(webSocketSession);
        logger.info("close the connenction..."+closeStatus.toString());  
    }  
  
    @Override  
    public boolean supportsPartialMessages() {  
        return true;  
    }  
}
