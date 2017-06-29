package cn.dlb.bim.action;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.google.gson.Gson;
import cn.dlb.bim.ifc.database.DatabaseException;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.IfcModelInterfaceException;
import cn.dlb.bim.ifc.serializers.SerializerException;
import cn.dlb.bim.ifc.serializers.binarygeometry.BinaryGeometryMessagingSerializer;
import cn.dlb.bim.ifc.shared.ProgressReporter;
import cn.dlb.bim.service.BimService;
import cn.dlb.bim.vo.ProgressVo;

public class BinaryGeometryAction extends LongAction {

	private final BimService bimService;
	private final Integer rid;
	private int lastPercentProcess = 0;
	
	public BinaryGeometryAction(BimService bimService, Integer rid, WebSocketSession webSocketSession) {
		super(webSocketSession);
		this.bimService = bimService;
		this.rid = rid;
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
		
		IfcModelInterface model = bimService.queryModelByRid(rid, progressReporter);
		BinaryGeometryMessagingSerializer serializer = new BinaryGeometryMessagingSerializer();
		try {
			serializer.init(model, model.getPackageMetaData());
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
		} catch (SerializerException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
