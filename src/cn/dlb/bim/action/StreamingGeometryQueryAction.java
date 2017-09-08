package cn.dlb.bim.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.emf.ecore.EClass;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.google.gson.Gson;

import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.dao.entity.ConcreteRevision;
import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.emf.IfcModelInterfaceException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.serializers.SerializerException;
import cn.dlb.bim.ifc.shared.ProgressReporter;
import cn.dlb.bim.ifc.stream.message.BinaryGeometryMessagingStreamingSerializer;
import cn.dlb.bim.ifc.stream.query.Include;
import cn.dlb.bim.ifc.stream.query.Query;
import cn.dlb.bim.ifc.stream.query.QueryContext;
import cn.dlb.bim.ifc.stream.query.QueryException;
import cn.dlb.bim.ifc.stream.query.QueryObjectProvider;
import cn.dlb.bim.ifc.stream.query.QueryPart;
import cn.dlb.bim.vo.ProgressVo;

public class StreamingGeometryQueryAction extends LongAction {

	private final PlatformServer server;
	private final QueryContext queryContext;
	private final ConcreteRevision concreteRevision;
	private int lastPercentProcess = 0;
	
	public StreamingGeometryQueryAction(WebSocketSession webSocketSession, PlatformServer server,
			QueryContext queryContext, ConcreteRevision concreteRevision) {
		super(webSocketSession);
		this.server = server;
		this.queryContext = queryContext;
		this.concreteRevision = concreteRevision;
	}

	@Override
	public void execute() throws DatabaseException, IfcModelInterfaceException {
		
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
		
		try {
			PackageMetaData packageMetaData = queryContext.getPackageMetaData();
			Query query = new Query(packageMetaData);
			QueryPart queryPart = query.createQueryPart();
			EClass geometryInfoElcass = packageMetaData.getEClassIncludingDependencies("GeometryInfo");
			queryPart.addType(geometryInfoElcass, false);
			Include include = queryPart.createInclude();
			include.addType(geometryInfoElcass, false);
			include.addField("data");
			QueryObjectProvider queryObjectProvider = new QueryObjectProvider(queryContext.getPlatformService(), server,
					query, queryContext.getRid(), packageMetaData);
			BinaryGeometryMessagingStreamingSerializer serializer = new BinaryGeometryMessagingStreamingSerializer();
			serializer.init(queryObjectProvider, packageMetaData, concreteRevision);
			ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
			boolean write = true;
			do {
				write = serializer.writeMessage(byteOutputStream, progressReporter);
				if (byteOutputStream.toByteArray().length > 0) {
					BinaryMessage message = new BinaryMessage(byteOutputStream.toByteArray());
					webSocketSession.sendMessage(message);
				}
				byteOutputStream.reset();
			} while (write);
			webSocketSession.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (SerializerException e) {
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
				try {
					webSocketSession.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}

}
