package cn.dlb.bim.ifc.stream.query.multithread;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.emf.ecore.EClass;

import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.query.ObjectProvidingStackFrame;
import cn.dlb.bim.ifc.stream.query.QueryContext;
import cn.dlb.bim.ifc.stream.query.QueryException;
import cn.dlb.bim.ifc.stream.query.QueryPart;

/**
 * @author Administrator
 *
 */
public class RunnableQueryOidsAndTypesStackFrame extends RunnableDatabaseReadingStackFrame implements ObjectProvidingStackFrame {

	private EClass eClass;
	private Collection<VirtualObject> objects;
	private final List<Long> oids;

	public RunnableQueryOidsAndTypesStackFrame(MultiThreadQueryObjectProvider queryObjectProvider, EClass eClass, QueryPart queryPart,
			QueryContext reusable, List<Long> oids) throws DatabaseException, QueryException {
		super(reusable, queryObjectProvider, queryPart);
		this.eClass = eClass;
		this.oids = oids;
		objects = getReusable().getVirtualObjectService().findByRidAndOids(getReusable().getRid(), oids);
	}

	@Override
	public boolean process() throws DatabaseException, QueryException, InterruptedException {
		for (VirtualObject object : objects) {
			long oid = object.getOid();
			currentObject = object;
			if (!getQueryObjectProvider().hasRead(oid)) {
				decideUseForSerialization(currentObject);
			}
			processPossibleIncludes(eClass, getQueryPart());
			getQueryObjectProvider().addToStorage(currentObject);
		}
		return true;
	}
	
	@Override
	public String toString() {
		return "QueryOidsAndTypesStackFrame (" + eClass.getName() + ")";
	}
	
	public EClass getEClass() {
		return eClass;
	}

	public List<Long> getOids() {
		return oids;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
		hashCodeBuilder.append(getClass()).append(getQueryObjectProvider()).append(eClass).append(getQueryPart()).append(getReusable()).append(oids.toArray());
		return hashCodeBuilder.toHashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof RunnableQueryOidsAndTypesStackFrame) {
			RunnableQueryOidsAndTypesStackFrame stackFrame = (RunnableQueryOidsAndTypesStackFrame) o;
			if (stackFrame.getQueryObjectProvider() == getQueryObjectProvider() && stackFrame.getReusable() == getReusable() 
					&& stackFrame.getQueryPart() == getQueryPart() && stackFrame.getEClass() == getEClass() && stackFrame.getOids().equals(oids)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
}