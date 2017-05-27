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
		
		Vector3f maxVec = new Vector3f(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		Vector3f minVec = new Vector3f(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		for (GeometryInfoVo geometry : geometryList) {
			maxVec = maxVector(maxVec, geometry.getBound().max);
			minVec = minVector(minVec, geometry.getBound().min);
		}
		Double zoom = maxZoom(minVec, maxVec);
		Vector3f middle = middle(minVec, maxVec);
		sendGeometryParam(zoom, middle);
		sendWebSocketMessage(geometryList, progressReporter);
	}
	
	public Vector3f maxVector(Vector3f a, Vector3f b) {
		double maxX = Math.max(a.x, b.x);
		double maxY = Math.max(a.y, b.y);
		double maxZ = Math.max(a.z, b.z);
		return new Vector3f(maxX, maxY, maxZ);
	}
	
	public Vector3f minVector(Vector3f a, Vector3f b) {
		double minX = Math.min(a.x, b.x);
		double minY = Math.min(a.y, b.y);
		double minZ = Math.min(a.z, b.z);
		return new Vector3f(minX, minY, minZ);
	}
	
	public Double maxZoom(Vector3f min, Vector3f max) {
		double deltX = Math.abs(max.x - min.x);
		double deltY = Math.abs(max.y - min.y);
		double deltZ = Math.abs(max.z - min.z);
		double maxDelt = Math.max(Math.max(deltX, deltY), deltZ);
		return maxDelt;
	}
	
	private Vector3f middle(Vector3f min, Vector3f max) {
		return new Vector3f((min.x + max.x)/2.0, (min.y + max.y)/2.0, (min.z + max.z)/2.0);
	}

	public void sendWebSocketMessage(List<GeometryInfoVo> msg, ProgressReporter progressReporter) {
		if (webSocketSession == null || !webSocketSession.isOpen()) {
			return;
		}
		Gson gson = new Gson();
		try {
			int progress = 0;
			for (GeometryInfoVo info : msg) {
				String jsonStr = gson.toJson(info);
				TextMessage message = new TextMessage(jsonStr);
				progressReporter.setTitle("Transferring");
				progressReporter.update(progress++, msg.size());
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
	
	public void sendGeometryParam(Double zoom, Vector3f middle) {
		if (webSocketSession == null || !webSocketSession.isOpen()) {
			return;
		}
		ResultUtil result = new ResultUtil();
		result.setKeyValue("zoom", zoom);
		result.setKeyValue("middle", middle);
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
