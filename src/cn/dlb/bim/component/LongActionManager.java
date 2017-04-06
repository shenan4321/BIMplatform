package cn.dlb.bim.component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import cn.dlb.bim.action.ActionStateListener;
import cn.dlb.bim.action.LongAction;

@Component("LongActionManager")
@SuppressWarnings("rawtypes")
public class LongActionManager implements ActionStateListener {
	
	@Autowired
	@Qualifier("taskExecutor")
	private ThreadPoolTaskExecutor taskExecutor;
	
	private Map<LongAction, Future> actions = Collections.synchronizedMap(new HashMap<>());
//	private Map<WebSocketSession, LongAction> websocketSessionHolder = Collections.synchronizedMap(new HashMap<>());
	
	public void startLongAction(LongAction longAction) {
		
		Future future = taskExecutor.submit(longAction);
		regist(longAction, future);
		
	}

	@Override
	public void onStarted(LongAction longAction) {
		
	}

	@Override
	public void onError(LongAction longAction) {
		remove(longAction);
	}

	@Override
	public void onFinished(LongAction longAction) {
		remove(longAction);
	}
	
	@Override
	public void onUpdate(LongAction longAction) {
		
	}
	
	public void remove(LongAction longAction) {
		actions.remove(longAction);
//		WebSocketSession socketSession = longAction.relatedWebSocketSession();
//		if (socketSession != null) {
//			websocketSessionHolder.remove(socketSession);
//		}
	}
	
//	public void cancelLongActionByWebSocketSessionRelated(WebSocketSession webSocketSession) {
//		LongAction longAction = websocketSessionHolder.get(webSocketSession);
//		cancelLongAction(longAction);
//	}
	
	private void regist(LongAction longAction, Future future) {
		longAction.addStateListener(this);
//		WebSocketSession socketSession = longAction.relatedWebSocketSession();
//		if (socketSession != null) {
//			websocketSessionHolder.put(socketSession, longAction);
//		}
	}
	
	private void cancelLongAction(LongAction longAction) {
		Future future = actions.get(longAction);
		if (future != null) {
//			future.cancel(true);
//			remove(longAction);
		}
	}
}
