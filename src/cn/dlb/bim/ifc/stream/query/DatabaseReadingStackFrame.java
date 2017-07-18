package cn.dlb.bim.ifc.stream.query;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import cn.dlb.bim.ifc.database.DatabaseException;
import cn.dlb.bim.ifc.database.queries.om.CanInclude;
import cn.dlb.bim.ifc.database.queries.om.Include;
import cn.dlb.bim.ifc.database.queries.om.QueryException;
import cn.dlb.bim.ifc.database.queries.om.QueryPart;
import cn.dlb.bim.ifc.stream.VirtualObject;

public abstract class DatabaseReadingStackFrame extends StackFrame implements ObjectProvidingStackFrame {
	private QueryContext reusable;
	private QueryObjectProvider queryObjectProvider;
	protected VirtualObject currentObject;
	private QueryPart queryPart;

	public DatabaseReadingStackFrame(QueryContext reusable, QueryObjectProvider queryObjectProvider, QueryPart queryPart) {
		this.reusable = reusable;
		this.queryObjectProvider = queryObjectProvider;
		this.queryPart = queryPart;
	}
	
	@Override
	public VirtualObject getCurrentObject() {
		return currentObject;
	}
	
	public QueryObjectProvider getQueryObjectProvider() {
		return queryObjectProvider;
	}
	
	public QueryContext getReusable() {
		return reusable;
	}
	
	public QueryPart getQueryPart() {
		return queryPart;
	}
	
	protected void processPossibleIncludes(EClass previousType, CanInclude canInclude) throws QueryException, DatabaseException {
		if (currentObject != null) {
			EClass eclass = queryObjectProvider.getPlatformService().getEClassForCid(currentObject.getEClassId());
			if (canInclude.hasIncludes()) {
				for (Include include : canInclude.getIncludes()) {
					processPossibleInclude(canInclude, include);
				}
			} else if (canInclude.isIncludeAllFields()) {
				for (EReference eReference : eclass.getEAllReferences()) {
					Include include = new Include(reusable.getPackageMetaData());
					include.addType(eclass, false);
					include.addField(eReference.getName());
					processPossibleInclude(canInclude, include);
				}
			}
			if (canInclude instanceof Include) {
				processPossibleInclude(null, (Include) canInclude);
			}
		}
	}

	protected void processPossibleInclude(CanInclude previousInclude, Include include) throws QueryException, DatabaseException {
		if (include.hasTypes()) {
			for (EClass filterClass : include.getTypes()) {
				EClass eclass = queryObjectProvider.getPlatformService().getEClassForCid(currentObject.getEClassId());
				if (!filterClass.isSuperTypeOf(eclass)) {
//					System.out.println(filterClass.getName() + " / " + currentObject.eClass().getName());
					return;
				}
			}
		}
		getQueryObjectProvider().push(new QueryIncludeStackFrame(getQueryObjectProvider(), getReusable(), previousInclude, include, currentObject, queryPart));
	}
	
}