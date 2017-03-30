package cn.dlb.bim.websocket;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.Gson;

import cn.dlb.bim.service.IBimService;
import cn.dlb.bim.vo.GeometryInfoVo;

public class GeometrySocketHandler implements WebSocketHandler {
	private static final Logger logger = LoggerFactory.getLogger(GeometrySocketHandler.class);  
    
//    private static final ArrayList<WebSocketSession> users = new ArrayList<WebSocketSession>();
	private final IBimService bimService;
	
	public GeometrySocketHandler(IBimService bimService) {
		this.bimService = bimService;
	}
  
    /** 
     * after connection establish 
     */  
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {  
        logger.info("connect success...");  
        String rid = session.getAttributes().get("rid").toString();
        Integer ridInt = Integer.valueOf(rid);
        List<GeometryInfoVo> geometies = bimService.queryDbGeometryInfo(ridInt);
        Gson gson = new Gson();
        String jsonString = gson.toJson(geometies);
        TextMessage textMessage = new TextMessage(jsonString);
        session.sendMessage(textMessage);
    }  
  
    /** 
     * process the received message  
     */  
    @Override  
    public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {  
    	
    }  
  
    @Override  
    public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {  
        if(webSocketSession.isOpen()){  
            webSocketSession.close();  
        }  
        logger.info("connenction error,close the connection...");  
    }  
  
    @Override  
    public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {  
        logger.info("close the connenction..."+closeStatus.toString());  
    }  
  
    @Override  
    public boolean supportsPartialMessages() {  
        return false;  
    }  
}
