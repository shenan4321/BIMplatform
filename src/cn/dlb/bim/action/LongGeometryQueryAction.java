package cn.dlb.bim.action;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.Gson;

import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.ifc.database.IfcModelDbException;
import cn.dlb.bim.ifc.database.IfcModelDbSession;
import cn.dlb.bim.ifc.database.OldQuery;
import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.IfcModelInterfaceException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.engine.MaterialGetter;
import cn.dlb.bim.ifc.engine.cells.Material;
import cn.dlb.bim.ifc.shared.ProgressReporter;
import cn.dlb.bim.models.geometry.GeometryInfo;
import cn.dlb.bim.vo.GeometryInfoVo;
import cn.dlb.bim.vo.ProgressVo;
import cn.dlb.bim.web.ResultUtil;

public class LongGeometryQueryAction extends LongAction {

//	private static final Logger LOGGER = LoggerFactory.getLogger(LongGeometryQueryAction.class);

	private final PlatformServer server;
	private final Integer rid;
	private final String schemaName;
	private int lastPercentProcess = 0;

	public LongGeometryQueryAction(PlatformServer server, Integer rid, String schemaName,
			WebSocketSession webSocketSession) {
		super(webSocketSession);
		this.server = server;
		this.rid = rid;
		this.schemaName = schemaName;
	}

	@Override
	public void execute() throws IfcModelDbException, IfcModelInterfaceException {

		PackageMetaData packageMetaData = server.getMetaDataManager().getPackageMetaData(schemaName);

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

		IfcModelDbSession session = new IfcModelDbSession(server.getIfcModelDao(), server.getMetaDataManager(),
				server.getPlatformInitDatas(), progressReporter, server.getModelCacheManager());
		IfcModelInterface model = session.get(packageMetaData, rid, new OldQuery(packageMetaData, true));
		
		if (model == null) {
			sendErrorWebSocketClose(rid);
		}
		
		List<GeometryInfoVo> geometryList = new ArrayList<>();
		EClass productClass = (EClass) model.getPackageMetaData().getEClassifierCaseInsensitive("IfcProduct");
		List<IdEObject> projectList = model.getAllWithSubTypes(productClass);

		for (IdEObject ifcProduct : projectList) {
			GeometryInfoVo adaptor = new GeometryInfoVo();
			GeometryInfo geometryInfo = (GeometryInfo) ifcProduct.eGet(ifcProduct.eClass().getEStructuralFeature("geometry"));
			if (geometryInfo != null) {
				Boolean defualtVisiable = !ifcProduct.eClass().isSuperTypeOf(packageMetaData.getEClass("IfcSpace"));
				MaterialGetter materialGetter = new MaterialGetter(model);
				Material material = materialGetter.getMaterial(ifcProduct);
				adaptor.transform(geometryInfo, ifcProduct.getOid(), ifcProduct.eClass().getName(), defualtVisiable, material == null ? null : material.getAmbient());
				geometryList.add(adaptor);
			}
		}

		sendWebSocketMessage(geometryList);
	}

	public void sendWebSocketMessage(List<GeometryInfoVo> msg) {
		if (webSocketSession == null || !webSocketSession.isOpen()) {
			return;
		}
		Gson gson = new Gson();
		String jsonStr = gson.toJson(msg);
		//TODO
//		try {
//			JsonWriter w = new JsonWriter(new OutputStreamWriter(new FileOutputStream(new File(""))));
//			w.beginArray();
//			w.name("").va
//		} catch (FileNotFoundException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		TextMessage message = new TextMessage(jsonStr);
		try {
			webSocketSession.sendMessage(message);
			webSocketSession.close();
		} catch (Exception e) {
			e.printStackTrace();
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
			webSocketSession.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
