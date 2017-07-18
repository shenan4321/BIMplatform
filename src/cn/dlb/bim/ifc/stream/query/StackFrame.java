package cn.dlb.bim.ifc.stream.query;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import cn.dlb.bim.ifc.database.DatabaseException;
import cn.dlb.bim.ifc.database.queries.om.QueryException;

public abstract class StackFrame {
	private boolean done = false;
	
	public boolean isDone() {
		return done;
	}
	
	public void setDone(boolean done) {
		this.done = done;
	}
	
	abstract boolean process() throws DatabaseException, QueryException, JsonParseException, JsonMappingException, IOException;
}