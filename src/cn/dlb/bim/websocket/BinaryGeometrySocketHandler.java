package cn.dlb.bim.websocket;

import java.io.IOException;
import java.util.ArrayList;

import javax.websocket.OnOpen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import cn.dlb.bim.action.LongGeometryQueryAction;
import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.service.BimService;

public class BinaryGeometrySocketHandler implements WebSocketHandler {
	private static final Logger logger = LoggerFactory.getLogger(BinaryGeometrySocketHandler.class);  
    
    private static final ArrayList<WebSocketSession> users = new ArrayList<WebSocketSession>();  
    private final PlatformServer server;
    private final BimService bimService;
    
    public BinaryGeometrySocketHandler(PlatformServer server, BimService bimService) {
    	this.server = server;
		this.bimService = bimService;
	}
  
    /** 
     * after connection establish 
     */  
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {  
        logger.info("connect success...");  
        users.add(session);  
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
//    	sendMessageToUsers(new TextMessage(webSocketMessage.getPayload() + "hello"));  
    }  
  
    @Override  
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {  
        if(webSocketSession.isOpen()){  
            webSocketSession.close();  
        }  
        logger.info("connenction error,close the connection...");  
        users.remove(webSocketSession);  
    }  
  
    @Override  
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {  
        logger.info("close the connenction..."+closeStatus.toString());  
        users.remove(webSocketSession);  
    }  
  
    @Override  
    public boolean supportsPartialMessages() {  
        return true;  
    }  
    /** 
     * 给所有在线用户发送消息 
     * 
     * @param message 
     */  
    public void sendMessageToUsers(TextMessage message) {  
        for (WebSocketSession user : users) {  
            try {  
                if (user.isOpen()) {  
                    user.sendMessage(message);  
                }  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
    }
}
