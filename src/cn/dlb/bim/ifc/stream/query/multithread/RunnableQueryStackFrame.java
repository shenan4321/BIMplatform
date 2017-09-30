package cn.dlb.bim.ifc.stream.query.multithread;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.stream.query.Query;
import cn.dlb.bim.ifc.stream.query.QueryContext;
import cn.dlb.bim.ifc.stream.query.QueryException;
import cn.dlb.bim.ifc.stream.query.QueryObjectProvider;
import cn.dlb.bim.ifc.stream.query.QueryPart;
import cn.dlb.bim.ifc.stream.query.QueryPartStackFrame;

public class RunnableQueryStackFrame extends RunnableStackFrame {

	private Iterator<QueryPart> queryIterator;
	private QueryContext reusable;

	public RunnableQueryStackFrame(MultiThreadQueryObjectProvider queryObjectProvider, Integer rid, QueryContext queryContext) throws JsonParseException, JsonMappingException, IOException {
		super(queryObjectProvider);
		this.reusable = queryContext;
		Query query = queryObjectProvider.getQuery();
		queryIterator = query.getQueryParts().iterator();
	}

	@Override
	public boolean process() throws DatabaseException, QueryException {
		while (queryIterator.hasNext()) {
			QueryPart next = queryIterator.next();
			getQueryObjectProvider().push(new RunnableQueryPartStackFrame(getQueryObjectProvider(), next, reusable));
		}
		return true;
	}
	
	public QueryContext getReusable() {
		return reusable;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
		hashCodeBuilder.append(getClass()).append(getQueryObjectProvider()).append(reusable);
		return hashCodeBuilder.toHashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof RunnableQueryStackFrame) {
			RunnableQueryStackFrame stackFrame = (RunnableQueryStackFrame) o;
			if (stackFrame.getQueryObjectProvider() == getQueryObjectProvider() && stackFrame.getReusable() == getReusable()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
}
