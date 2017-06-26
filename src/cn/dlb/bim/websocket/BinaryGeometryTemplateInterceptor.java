package cn.dlb.bim.websocket;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.google.common.base.Strings;

public class BinaryGeometryTemplateInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Map<String, Object> attributes) throws Exception {
		String rid = ((ServletServerHttpRequest) request).getServletRequest().getParameter("rid");
		String otid = ((ServletServerHttpRequest) request).getServletRequest().getParameter("otid");
		if (Strings.isNullOrEmpty(rid) || Strings.isNullOrEmpty(otid)) {
			return false;
		} else {
			attributes.put("rid", rid);
			attributes.put("otid", otid);
			return true;
		}
	}

	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
			Exception exception) {
	}

}
