package cn.dlb.bim.ifc.stream.query;

import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EClass;

import cn.dlb.bim.ifc.database.DatabaseException;
import cn.dlb.bim.ifc.database.queries.om.QueryException;
import cn.dlb.bim.ifc.database.queries.om.QueryPart;

public class QueryOidsAndTypesStackFrame extends DatabaseReadingStackFrame implements ObjectProvidingStackFrame {

	private EClass eClass;
	private Iterator<Long> oidIterator;

	public QueryOidsAndTypesStackFrame(QueryObjectProvider queryObjectProvider, EClass eClass, QueryPart queryPart,
			QueryContext reusable, List<Long> oids) throws DatabaseException, QueryException {
		super(reusable, queryObjectProvider, queryPart);
		this.eClass = eClass;

		oidIterator = oids.iterator();

	}

	@Override
	public boolean process() throws DatabaseException, QueryException {
		if (!oidIterator.hasNext()) {
			return true;
		}
		long oid = oidIterator.next();
		if (!getQueryObjectProvider().hasRead(oid)) {
			currentObject = getReusable().getPlatformService().queryVirtualObject(getReusable().getRid(), oid);
			decideUseForSerialization(currentObject);

		}
		processPossibleIncludes(eClass, getQueryPart());
		return false;
	}
	
}