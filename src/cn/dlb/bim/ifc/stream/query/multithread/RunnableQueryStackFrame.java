package cn.dlb.bim.ifc.stream.query.multithread;

import java.io.IOException;
import java.util.Iterator;

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
}
