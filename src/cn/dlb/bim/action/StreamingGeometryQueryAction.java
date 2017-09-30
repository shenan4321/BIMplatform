package cn.dlb.bim.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EClass;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
import cn.dlb.bim.ifc.stream.query.QueryPart;
import cn.dlb.bim.ifc.stream.query.multithread.MultiThreadQueryObjectProvider;
import cn.dlb.bim.ifc.stream.serializers.ObjectProvider;
import cn.dlb.bim.vo.ProgressVo;

public class StreamingGeometryQueryAction extends LongAction {

	private final QueryContext queryContext;
	private final ConcreteRevision concreteRevision;
	private int lastPercentProcess = 0;
	private final ThreadPoolTaskExecutor executor;
	
	public StreamingGeometryQueryAction(ThreadPoolTaskExecutor executor, WebSocketSession webSocketSession,
			QueryContext queryContext, ConcreteRevision concreteRevision) {
		super(webSocketSession);
		this.queryContext = queryContext;
		this.concreteRevision = concreteRevision;
		this.executor = executor;
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
			EClass productClass = packageMetaData.getEClass("IfcProduct");
			Set<EClass> subClasses = packageMetaData.getAllSubClasses(productClass);
			for (EClass subClass : subClasses) {
				if (!packageMetaData.getEClass("IfcSpace").isSuperTypeOf(subClass) 
						&& !packageMetaData.getEClass("IfcFeatureElementSubtraction").isSuperTypeOf(subClass)) {
					queryPart.addType(subClass, false);
					Include include = queryPart.createInclude();
					include.addType(subClass, false);
					include.addField("geometry");
					Include dataInclude = include.createInclude();
					EClass geometryInfoClass = packageMetaData.getEClassIncludingDependencies("GeometryInfo");
					dataInclude.addType(geometryInfoClass, false);
					dataInclude.addField("data");
				}
			}
			ObjectProvider queryObjectProvider = new MultiThreadQueryObjectProvider(executor, queryContext.getCatalogService(), queryContext.getVirtualObjectService(),
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
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
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
