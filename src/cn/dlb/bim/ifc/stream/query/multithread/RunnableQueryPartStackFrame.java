package cn.dlb.bim.ifc.stream.query.multithread;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.eclipse.emf.ecore.EClass;

import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.stream.query.InBoundingBox;
import cn.dlb.bim.ifc.stream.query.QueryContext;
import cn.dlb.bim.ifc.stream.query.QueryException;
import cn.dlb.bim.ifc.stream.query.QueryOidsAndTypesStackFrame;
import cn.dlb.bim.ifc.stream.query.QueryPart;
import cn.dlb.bim.ifc.stream.query.QueryTypeStackFrame;

public class RunnableQueryPartStackFrame extends RunnableStackFrame {

	private Iterator<EClass> typeIterator;
	private QueryContext reusable;
	private QueryPart partialQuery;
	private final Map<EClass, List<Long>> oids;
	private final Set<String> guids;
	private final Set<String> names;
	private Map<String, Object> properties;
	private InBoundingBox inBoundingBox;
	private Set<String> classifications;

	public RunnableQueryPartStackFrame(MultiThreadQueryObjectProvider queryObjectProvider, QueryPart partialQuery, QueryContext reusable) throws DatabaseException, QueryException {
		super(queryObjectProvider);
		this.partialQuery = partialQuery;
		this.reusable = reusable;
		if (partialQuery.hasOids()) {
			Set<Long> oidsList = partialQuery.getOids();
			this.oids = new HashMap<EClass, List<Long>>();
			if (oidsList.size() == 0) {
				throw new QueryException("\"oids\" parameter of type array is of size 0");
			}
			Iterator<Long> iterator = oidsList.iterator();
			while (iterator.hasNext()) {
				long oid = iterator.next();
				EClass eClass = queryObjectProvider.getCatalogService().getEClassForOid(oid);
				List<Long> list = this.oids.get(eClass);
				if (list == null) {
					list = new ArrayList<Long>();
					this.oids.put(eClass, list);
				}
				list.add(oid);
			}
		} else {
			oids = null;
		}
		if (!partialQuery.hasTypes()) {
			if (oids == null) {
				typeIterator = null;
			} else {
				typeIterator = oids.keySet().iterator();
			}
		} else {
			typeIterator = partialQuery.getTypes().iterator();
		}
		if (this.partialQuery.getGuids() != null) {
			this.guids = partialQuery.getGuids();
		} else {
			guids = null;
		}
		if (this.partialQuery.getNames() != null) {
			this.names = partialQuery.getNames();
		} else {
			names = null;
		}
		this.properties = partialQuery.getProperties();
		this.classifications = partialQuery.getClassifications();
		this.inBoundingBox = partialQuery.getInBoundingBox();
	}

	@Override
	public boolean process() throws DatabaseException, QueryException {
		if (typeIterator == null) {
			return true;
		}
		
		while (typeIterator.hasNext()) {
			EClass eClass = typeIterator.next();
			if (oids != null) {
				List<Long> oids2 = oids.get(eClass);
				if (oids2 != null) {
					getQueryObjectProvider().push(new RunnableQueryOidsAndTypesStackFrame(getQueryObjectProvider(), eClass, partialQuery, reusable, oids2));
				}
			} /*else if (guids != null) {
				queryObjectProvider.push(new QueryGuidsAndTypesStackFrame(queryObjectProvider, eClass, partialQuery, reusable, guids));
			} else if (names != null) {
				queryObjectProvider.push(new QueryNamesAndTypesStackFrame(queryObjectProvider, eClass, partialQuery, reusable, names));
			} else if (properties != null) {
				queryObjectProvider.push(new QueryPropertiesAndTypesStackFrame(queryObjectProvider, eClass, partialQuery, reusable, properties));
			} else if (classifications != null) {
				queryObjectProvider.push(new QueryClassificationsAndTypesStackFrame(queryObjectProvider, eClass, partialQuery, reusable, classifications));
			} else if (inBoundingBox != null) {
				queryObjectProvider.push(new QueryBoundingBoxStackFrame(queryObjectProvider, eClass, partialQuery, reusable, inBoundingBox));
			} */else {
				getQueryObjectProvider().push(new RunnableQueryTypeStackFrame(getQueryObjectProvider(), eClass, reusable, partialQuery));
			}
		}
		
		return true;
	}
	
	public QueryContext getReusable() {
		return reusable;
	}

	public QueryPart getPartialQuery() {
		return partialQuery;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder hashCodeBuilder = new HashCodeBuilder();
		hashCodeBuilder.append(getClass()).append(getQueryObjectProvider()).append(reusable).append(partialQuery);
		return hashCodeBuilder.toHashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof RunnableQueryPartStackFrame) {
			RunnableQueryPartStackFrame stackFrame = (RunnableQueryPartStackFrame) o;
			if (stackFrame.getQueryObjectProvider() == getQueryObjectProvider() && stackFrame.getReusable() == getReusable() 
					&& stackFrame.getPartialQuery() == getPartialQuery()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
}