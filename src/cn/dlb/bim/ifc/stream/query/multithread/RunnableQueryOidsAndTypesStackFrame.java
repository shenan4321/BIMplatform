package cn.dlb.bim.ifc.stream.query.multithread;

import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EClass;

import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.stream.query.ObjectProvidingStackFrame;
import cn.dlb.bim.ifc.stream.query.QueryContext;
import cn.dlb.bim.ifc.stream.query.QueryException;
import cn.dlb.bim.ifc.stream.query.QueryPart;

public class RunnableQueryOidsAndTypesStackFrame extends RunnableDatabaseReadingStackFrame implements ObjectProvidingStackFrame {

	private EClass eClass;
	private Iterator<Long> oidIterator;

	public RunnableQueryOidsAndTypesStackFrame(MultiThreadQueryObjectProvider queryObjectProvider, EClass eClass, QueryPart queryPart,
			QueryContext reusable, List<Long> oids) throws DatabaseException, QueryException {
		super(reusable, queryObjectProvider, queryPart);
		this.eClass = eClass;

		oidIterator = oids.iterator();

	}

	@Override
	public boolean process() throws DatabaseException, QueryException, InterruptedException {
		while (oidIterator.hasNext()) {
			long oid = oidIterator.next();
			if (!getQueryObjectProvider().hasRead(oid)) {
				currentObject = getReusable().getVirtualObjectService().findOneByRidAndOid(getReusable().getRid(), oid);
				decideUseForSerialization(currentObject);
			}
			processPossibleIncludes(eClass, getQueryPart());
			getQueryObjectProvider().addToStorage(currentObject);
		}
		return true;
	}
	
}