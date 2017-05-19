package cn.dlb.bim.action;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.Gson;

import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.dao.entity.IfcModelEntity;
import cn.dlb.bim.ifc.database.IfcModelDbException;
import cn.dlb.bim.ifc.database.IfcModelDbSession;
import cn.dlb.bim.ifc.database.OldQuery;
import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.IfcModelInterfaceException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.emf.Schema;
import cn.dlb.bim.ifc.shared.ProgressReporter;
import cn.dlb.bim.ifc.tree.Material;
import cn.dlb.bim.ifc.tree.MaterialGenerator;
import cn.dlb.bim.models.geometry.GeometryInfo;
import cn.dlb.bim.models.geometry.Vector3f;
import cn.dlb.bim.vo.GeometryInfoVo;
import cn.dlb.bim.vo.ProgressVo;
import cn.dlb.bim.web.ResultUtil;

public class LongGeometryQueryAction extends LongAction {
	private static String IFC2X3_SCHEMA_SHORT = "IFC2X3";

//	private static final Logger LOGGER = LoggerFactory.getLogger(LongGeometryQueryAction.class);

	private final PlatformServer server;
	private final Integer rid;
	private int lastPercentProcess = 0;

	public LongGeometryQueryAction(PlatformServer server, Integer rid, WebSocketSession webSocketSession) {
		super(webSocketSession);
		this.server = server;
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
		IfcModelEntity ifcModelEntity = server.getIfcModelDao().queryIfcModelEntityByRid(rid);
		String ifcSchemaVersion = ifcModelEntity.getModelMetaData().getIfcHeader().getIfcSchemaVersion();
		PackageMetaData packageMetaData = null;
		if (ifcSchemaVersion.startsWith(IFC2X3_SCHEMA_SHORT)) {
			packageMetaData = server.getMetaDataManager().getPackageMetaData(Schema.IFC2X3TC1.getEPackageName());
		} else {
			packageMetaData = server.getMetaDataManager().getPackageMetaData(Schema.IFC4.getEPackageName());
		}
		
		IfcModelDbSession session = new IfcModelDbSession(server.getIfcModelDao(), server.getMetaDataManager(),
				server.getPlatformInitDatas(), progressReporter, server.getModelCacheManager());
		IfcModelInterface model = session.get(packageMetaData, rid, new OldQuery(packageMetaData, true));
		
		if (model == null) {
			sendErrorWebSocketClose(rid);
		}
		
		List<GeometryInfoVo> geometryList = new ArrayList<>();
		EClass productClass = (EClass) model.getPackageMetaData().getEClassifierCaseInsensitive("IfcProduct");
		List<IdEObject> projectList = model.getAllWithSubTypes(productClass);//耗时
		
		double maxZoom = 0;
		for (IdEObject ifcProduct : projectList) {
			GeometryInfoVo adaptor = new GeometryInfoVo();
			GeometryInfo geometryInfo = (GeometryInfo) ifcProduct.eGet(ifcProduct.eClass().getEStructuralFeature("geometry"));
			if (geometryInfo != null) {
				Boolean defualtVisiable = !packageMetaData.getEClass("IfcSpace").isSuperTypeOf(ifcProduct.eClass()) 
						&& !packageMetaData.getEClass("IfcFeatureElementSubtraction").isSuperTypeOf(ifcProduct.eClass());//IfcFeatureElementSubtraction
				if (!defualtVisiable) {//TODO
					continue;
				}
				Double zoom = maxZoom(geometryInfo.getMinBounds(), geometryInfo.getMaxBounds());
				maxZoom = Math.max(zoom, maxZoom);
				MaterialGenerator materialGetter = new MaterialGenerator(model);
				Material material = materialGetter.getMaterial(ifcProduct);
				adaptor.transform(geometryInfo, ifcProduct.getOid(), ifcProduct.eClass().getName(), defualtVisiable, material == null ? null : material.getAmbient());
				geometryList.add(adaptor);
			}
		}

		sendGeometryZoom(maxZoom);
		sendWebSocketMessage(geometryList);
	}
	
	public Double maxZoom(Vector3f min, Vector3f max) {
//		Vector3f min = geometryInfo.getMinBounds();
//		Vector3f max = geometryInfo.getMaxBounds();
		double minX = min.getX();
		double minY = min.getY();
		double maxX = max.getX();
		double maxY = max.getY();
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
