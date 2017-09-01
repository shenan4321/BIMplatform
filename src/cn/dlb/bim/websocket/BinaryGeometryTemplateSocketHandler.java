package cn.dlb.bim.websocket;

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import cn.dlb.bim.action.BinaryGeometryOutputTemplateAction;
import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.service.BimService;

@Component("BinaryGeometryTemplateSocketHandler")
public class BinaryGeometryTemplateSocketHandler implements WebSocketHandler {
	private static final Logger logger = LoggerFactory.getLogger(BinaryGeometryTemplateSocketHandler.class);  
    
    private static final ArrayList<WebSocketSession> users = new ArrayList<WebSocketSession>();  
    @Autowired
    private PlatformServer server;
    @Autowired
    private BimService bimService;
  
    /** 
     * after connection establish 
     */  
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {  
        logger.info("connect success...");  
        users.add(session);  
        String rid = session.getAttributes().get("rid").toString();
        String otid = session.getAttributes().get("otid").toString();
        BinaryGeometryOutputTemplateAction action = new BinaryGeometryOutputTemplateAction(bimService, Integer.valueOf(rid), Long.valueOf(otid), session);
        server.getLongActionManager().startLongAction(action);
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
