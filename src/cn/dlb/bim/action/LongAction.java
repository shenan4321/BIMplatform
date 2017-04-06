package cn.dlb.bim.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import cn.dlb.bim.ifc.database.IfcModelDbException;
import cn.dlb.bim.ifc.emf.IfcModelInterfaceException;

public abstract class LongAction implements Runnable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LongAction.class);
	
	protected ActionStateListener actionStateListener;
	protected LongActionState state;
	protected WebSocketSession webSocketSession;
	
	public LongAction(WebSocketSession webSocketSession) {
		this.webSocketSession = webSocketSession;
		this.state = LongActionState.Unknow;
	}
	
	@Override
	public void run() {
		switchState(LongActionState.Started);
		try {
			execute();
		} catch (Exception e) {
			switchState(LongActionState.Error);
			LOGGER.error("", e);
		}
		switchState(LongActionState.Finished);
	}
	
	public WebSocketSession relatedWebSocketSession() {
		return webSocketSession;
	}
	public void setNullRelatedWebSocketSession() {
		webSocketSession = null;
	}
	public void addStateListener(ActionStateListener actionStateListener) {
		this.actionStateListener = actionStateListener;
	}
	public void switchState(LongActionState state) {
		this.state = state;
		if (actionStateListener != null) {
			switch (state) {
			case Started:
				actionStateListener.onStarted(this);
				break;
			case Error:
				actionStateListener.onError(this);
				break;
			case Finished:
				actionStateListener.onFinished(this);
				break;
			default:
				break;
			}
		}
	}
	public LongActionState getLongActionState() {
		return state;
	}
	
	public abstract void execute() throws IfcModelDbException, IfcModelInterfaceException;
}
