package cn.dlb.bim.ifc.stream.query.multithread;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.stream.query.Include;
import cn.dlb.bim.ifc.stream.query.ObjectProvidingStackFrame;
import cn.dlb.bim.ifc.stream.query.QueryContext;
import cn.dlb.bim.ifc.stream.query.QueryException;
import cn.dlb.bim.ifc.stream.query.QueryPart;

public class RunnableFollowReferenceStackFrame extends RunnableDatabaseReadingStackFrame implements ObjectProvidingStackFrame {

	private long oid;
	private boolean hasRun = false;
	private Include include;
	private EReference fromReference;
	
	public RunnableFollowReferenceStackFrame(MultiThreadQueryObjectProvider queryObjectProvider, Long oid, QueryContext reusable, QueryPart queryPart, EReference fromReference, Include include) {
		super(reusable, queryObjectProvider, queryPart);
		this.oid = oid;
		this.fromReference = fromReference;
		this.include = include;
	}

	@Override
	public boolean process() throws DatabaseException, QueryException, InterruptedException {
		if (getQueryObjectProvider().hasRead(oid)) {
			processPossibleIncludes(null, include);
			return true;
		}
		
		if (hasRun) {
			return true;
		}
		hasRun = true;
		if (oid == -1) {
			throw new DatabaseException("Cannot get object for oid " + oid);
		}
		Integer rid = getReusable().getRid();
		currentObject = getQueryObjectProvider().getVirtualObjectService().findOneByRidAndOid(rid, oid);
		decideUseForSerialization(currentObject);
		processPossibleIncludes(null, include);
		getQueryObjectProvider().addToStorage(currentObject);
		return true;
	}
	
	@Override
	public String toString() {
		try {
			EClass eClass = getQueryObjectProvider().getCatalogService().getEClassForOid(oid);
			return "FollowReferenceStackFrame (" + eClass.getName() + "." + fromReference.getName() + ", " + oid + ")";
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return "FollowReferenceStackFrame (" + fromReference.getName() + ")";
	}
}