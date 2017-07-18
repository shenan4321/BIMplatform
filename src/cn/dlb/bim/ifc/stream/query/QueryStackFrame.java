package cn.dlb.bim.ifc.stream.query;

import java.io.IOException;
import java.util.Iterator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import cn.dlb.bim.ifc.database.DatabaseException;
import cn.dlb.bim.ifc.database.queries.om.Query;
import cn.dlb.bim.ifc.database.queries.om.QueryException;
import cn.dlb.bim.ifc.database.queries.om.QueryPart;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.model.IfcHeader;

public class QueryStackFrame extends StackFrame {

	private Iterator<QueryPart> queryIterator;
	private QueryObjectProvider queryObjectProvider;
	private QueryContext reusable;

	public QueryStackFrame(QueryObjectProvider queryObjectProvider, Integer rid) throws JsonParseException, JsonMappingException, IOException {
		this.queryObjectProvider = queryObjectProvider;
		IfcHeader header = queryObjectProvider.getPlatformService().queryIfcHeader(rid);
		String ifcSchemaVersion = header.getIfcSchemaVersion();
		PackageMetaData packageMetaData = queryObjectProvider.getMetaDataManager().getPackageMetaData(ifcSchemaVersion);
		this.reusable = new QueryContext(queryObjectProvider.getPlatformService(), packageMetaData, rid);
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
