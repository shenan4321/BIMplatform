package cn.dlb.bim.ifc.stream.query;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import cn.dlb.bim.ifc.database.DatabaseException;
import cn.dlb.bim.ifc.database.queries.om.Query;
import cn.dlb.bim.ifc.database.queries.om.QueryException;
import cn.dlb.bim.ifc.database.queries.om.QueryPart;

public class QueryStackFrame extends StackFrame {

	private Iterator<QueryPart> queryIterator;
	private QueryObjectProvider queryObjectProvider;
	private QueryContext reusable;

	public QueryStackFrame(QueryObjectProvider queryObjectProvider, Integer rid, QueryContext queryContext) throws JsonParseException, JsonMappingException, IOException {
		this.queryObjectProvider = queryObjectProvider;
		this.reusable = queryContext;
		Query query = queryObjectProvider.getQuery();
		queryIterator = query.getQueryParts().iterator();
	}

	@Override
	public boolean process() throws DatabaseException, QueryException {
		QueryPart next = queryIterator.next();
		queryObjectProvider.push(new QueryPartStackFrame(queryObjectProvider, next, reusable));
		if (queryIterator.hasNext()) {
			return false;
		}
		return true;
	}
}
