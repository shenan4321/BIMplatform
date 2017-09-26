package cn.dlb.bim.ifc.stream.query.multithread;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EClass;

import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.query.ObjectProvidingStackFrame;
import cn.dlb.bim.ifc.stream.query.QueryContext;
import cn.dlb.bim.ifc.stream.query.QueryException;
import cn.dlb.bim.ifc.stream.query.QueryPart;

public class RunnableQueryOidsAndTypesStackFrame extends RunnableDatabaseReadingStackFrame implements ObjectProvidingStackFrame {

	private EClass eClass;
	private Collection<VirtualObject> objects;

	public RunnableQueryOidsAndTypesStackFrame(MultiThreadQueryObjectProvider queryObjectProvider, EClass eClass, QueryPart queryPart,
			QueryContext reusable, List<Long> oids) throws DatabaseException, QueryException {
		super(reusable, queryObjectProvider, queryPart);
		this.eClass = eClass;

		objects = getReusable().getVirtualObjectService().findByRidAndOids(getReusable().getRid(), oids);
//		oidIterator = oids.iterator();

	}

	@Override
	public boolean process() throws DatabaseException, QueryException, InterruptedException {
		for (VirtualObject object : objects) {
			long oid = object.getOid();
			if (!getQueryObjectProvider().hasRead(oid)) {
				currentObject = object;
				decideUseForSerialization(currentObject);
			}
			processPossibleIncludes(eClass, getQueryPart());
			getQueryObjectProvider().addToStorage(currentObject);
		}
		return true;
	}
	
}