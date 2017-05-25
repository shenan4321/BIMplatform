package cn.dlb.bim.action;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.Gson;

import cn.dlb.bim.ifc.database.IfcModelDbException;
import cn.dlb.bim.ifc.emf.IfcModelInterfaceException;
import cn.dlb.bim.ifc.shared.ProgressReporter;
import cn.dlb.bim.service.BimService;
import cn.dlb.bim.vo.GeometryInfoVo;
import cn.dlb.bim.vo.ProgressVo;
import cn.dlb.bim.vo.Vector3f;
import cn.dlb.bim.web.ResultUtil;

public class LongGeometryQueryAction extends LongAction {
	private static String IFC2X3_SCHEMA_SHORT = "IFC2X3";

//	private static final Logger LOGGER = LoggerFactory.getLogger(LongGeometryQueryAction.class);

	private final BimService bimService;
	private final Integer rid;
	private int lastPercentProcess = 0;

	public LongGeometryQueryAction(BimService bimService, Integer rid, WebSocketSession webSocketSession) {
		super(webSocketSession);
		this.bimService = bimService;
		this.rid = rid;
	}

	@Override
	public void execute() throws IfcModelDbException, IfcModelInterfaceException {

		@SuppressWarnings("all")
		ProgressReporter progressReporter = new ProgressReporter() {

			private String title = "";
			private long progress;
			private long max;

			@Override
			public void update(long progress, long max) {
				this.progress = progress;
				this.max = max;
				ProgressVo msg = new ProgressVo();
				msg.setTitle(title);
				msg.setProgress(progress);
				msg.setMax(max);
				sendWebSocketMessage(msg);
			}

			@Override
			public void setTitle(String title) {
				this.title = title;
				ProgressVo msg = new ProgressVo();
				msg.setTitle(title);
				msg.setProgress(progress);
				msg.setMax(max);
				sendWebSocketMessage(msg);
			}
		};
		
		List<GeometryInfoVo> geometryList = bimService.queryGeometryInfo(rid, progressReporter);
		
		if (geometryList == null) {
			sendErrorWebSocketClose(rid);
		}
		
		
		double maxZoom = 0;
		for (GeometryInfoVo geometry : geometryList) {
			Double zoom = maxZoom(geometry.getBound().min, geometry.getBound().max);
			maxZoom = Math.max(zoom, maxZoom);
		}

		sendGeometryZoom(40000.0);
		sendWebSocketMessage(geometryList);
	}
	
	public Double maxZoom(Vector3f min, Vector3f max) {
		double minX = min.x;
		double minY = min.y;
		double maxX = max.x;
		double maxY = max.y;
		double deltX = Math.abs(maxX - minX);
		double deltY = Math.abs(maxY - minY);
		double maxDelt = Math.max(deltX, deltY);
		return maxDelt;
	}

	@SuppressWarnings("resource")
	public void sendWebSocketMessage(List<GeometryInfoVo> msg) {
		if (webSocketSession == null || !webSocketSession.isOpen()) {
			return;
		}
		Gson gson = new Gson();
		try {
			for (GeometryInfoVo info : msg) {
				String jsonStr = gson.toJson(info);
				TextMessage message = new TextMessage(jsonStr);
				webSocketSession.sendMessage(message);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				webSocketSession.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendWebSocketMessage(ProgressVo msg) {
		if (webSocketSession == null || !webSocketSession.isOpen()) {
			return;
		}
		int curPercent = 0;
		if (msg.getMax() != 0) {
			curPercent = (int) Math.floor(Double.valueOf(msg.getProgress()) / msg.getMax() * 100);
		}
		if (lastPercentProcess != curPercent) {
			lastPercentProcess = curPercent;
			Gson gson = new Gson();
			String jsonStr = gson.toJson(msg);
			TextMessage message = new TextMessage(jsonStr);
			try {
				webSocketSession.sendMessage(message);
			} catch (Exception e) {
				try {
					webSocketSession.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}
	
	public void sendErrorWebSocketClose(Integer rid) {
		if (webSocketSession == null || !webSocketSession.isOpen()) {
			return;
		}
		ResultUtil result = new ResultUtil();
		result.setSuccess(false);
		result.setMsg("no model with rid : " + rid);
		Gson gson = new Gson();
		String jsonStr = gson.toJson(result.getResult());
		TextMessage message = new TextMessage(jsonStr);
		try {
			webSocketSession.sendMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				webSocketSession.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendGeometryZoom(Double zoom) {
		if (webSocketSession == null || !webSocketSession.isOpen()) {
			return;
		}
		ResultUtil result = new ResultUtil();
		result.setKeyValue("zoom", zoom);
		result.setSuccess(true);
		Gson gson = new Gson();
		String jsonStr = gson.toJson(result.getResult());
		TextMessage message = new TextMessage(jsonStr);
		try {
			webSocketSession.sendMessage(message);
		} catch (Exception e) {
			try {
				webSocketSession.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		} 
	}
}
