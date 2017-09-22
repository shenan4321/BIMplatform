package cn.dlb.bim.ifc.stream.query.multithread;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.query.QueryException;

public abstract class RunnableStackFrame implements Runnable {

	public static enum Status {
		DONE, WAIT, PROCCESS;
	}
	
	private Status status;
	private MultiThreadQueryObjectProvider queryObjectProvider;
	
	public RunnableStackFrame(MultiThreadQueryObjectProvider queryObjectProvider) {
		this.queryObjectProvider = queryObjectProvider;
	}

	@Override
	public void run() {
		try {
			setStatus(Status.PROCCESS);
			boolean done = process();
			if (done) {
				setStatus(Status.DONE);
			} 
			queryObjectProvider.removeFuture(this);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IllegalMonitorStateException e) {
			e.printStackTrace();
		}
	}
	
	public MultiThreadQueryObjectProvider getQueryObjectProvider() {
		return queryObjectProvider;
	}

	public synchronized Status getStatus() {
		return status;
	}

	public synchronized void setStatus(Status status) {
		this.status = status;
	}

	public void syncNotify() {
		synchronized (this) {
			this.notifyAll();
		}
	}

	public abstract boolean process() throws InterruptedException, DatabaseException, QueryException,
			JsonParseException, JsonMappingException, IOException;
}
