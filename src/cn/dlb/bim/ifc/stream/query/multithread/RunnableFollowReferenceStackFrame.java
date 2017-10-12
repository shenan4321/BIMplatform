package cn.dlb.bim.ifc.stream.query.multithread;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
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
//		if (getQueryObjectProvider().hasRead(oid)) {
//			processPossibleIncludes(null, include);
//			return true;
//		}
		
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
			return "FollowReferenceStackFrame (" + eClass.getName() + "." + fromReference.getName() + ", " + oid + ")" + 
					" QueryPart" + getQueryPart().hashCode() + " Include : " + include.hashCode();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return "FollowReferenceStackFrame (" + fromReference.getName() + ")";
	}
	
	public long getOid() {
		return oid;
	}

	public Include getInclude() {
		return include;
	}

	public EReference getFromReference() {
		return fromReference;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
		hashCodeBuilder.append(getClass()).append(getQueryObjectProvider()).append(getReusable()).append(getQueryPart()).append(fromReference)
		.append(include).append(oid);
		return hashCodeBuilder.toHashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof RunnableFollowReferenceStackFrame) {
			RunnableFollowReferenceStackFrame stackFrame = (RunnableFollowReferenceStackFrame) o;
			if (stackFrame.getQueryObjectProvider() == getQueryObjectProvider() && stackFrame.getReusable() == getReusable() 
					&& stackFrame.getQueryPart() == getQueryPart() && stackFrame.getFromReference() == getFromReference()
					&& stackFrame.getInclude() == getInclude() && stackFrame.getOid() == getOid()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}