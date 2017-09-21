package cn.dlb.bim.ifc.stream.query.multithread;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.query.CanInclude;
import cn.dlb.bim.ifc.stream.query.Include;
import cn.dlb.bim.ifc.stream.query.ObjectProvidingStackFrame;
import cn.dlb.bim.ifc.stream.query.QueryContext;
import cn.dlb.bim.ifc.stream.query.QueryException;
import cn.dlb.bim.ifc.stream.query.QueryIncludeStackFrame;
import cn.dlb.bim.ifc.stream.query.QueryObjectProvider;
import cn.dlb.bim.ifc.stream.query.QueryPart;

public abstract class RunnableDatabaseReadingStackFrame extends RunnableStackFrame implements ObjectProvidingStackFrame {
	private QueryContext reusable;
	protected VirtualObject currentObject;
	private QueryPart queryPart;

	public RunnableDatabaseReadingStackFrame(QueryContext reusable, MultiThreadQueryObjectProvider queryObjectProvider, QueryPart queryPart) {
		super(queryObjectProvider);
		this.reusable = reusable;
		this.queryPart = queryPart;
	}

	@Override
	public VirtualObject getCurrentObject() {
		return currentObject;
	}
	
	public QueryContext getReusable() {
		return reusable;
	}
	
	public QueryPart getQueryPart() {
		return queryPart;
	}
	
	protected void processPossibleIncludes(EClass previousType, CanInclude canInclude) throws QueryException, DatabaseException {
		if (currentObject != null) {
			if (canInclude.hasIncludes()) {
				for (Include include : canInclude.getIncludes()) {
					processPossibleInclude(canInclude, include);
				}
			} else if (canInclude.isIncludeAllFields()) {
				for (EReference eReference : currentObject.eClass().getEAllReferences()) {
					Include include = new Include(reusable.getPackageMetaData());
					include.addType(currentObject.eClass(), false);
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
				if (!filterClass.isSuperTypeOf(currentObject.eClass())) {
//					System.out.println(filterClass.getName() + " / " + currentObject.eClass().getName());
					return;
				}
			}
		}
		if (include.hasFields()) {
			for (EStructuralFeature eStructuralFeature : include.getFields()) {
				currentObject.addUseForSerialization(eStructuralFeature);
			}
		}
		getQueryObjectProvider().push(new RunnableQueryIncludeStackFrame(getQueryObjectProvider(), getReusable(), previousInclude, include, currentObject, queryPart));
	}
	
	@SuppressWarnings("rawtypes")
	protected void decideUseForSerialization(VirtualObject object) throws DatabaseException {
		PackageMetaData packageMetaData = getReusable().getPackageMetaData();
		for (EStructuralFeature feature : object.eClass().getEAllStructuralFeatures()) {
			if (packageMetaData.useForDatabaseStorage(object.eClass(), feature)) {
				Object featureValue = object.eGet(feature);
				if (featureValue != null) {
					if (featureValue instanceof List) {
						List featureValueList = (List) featureValue;
						decideUseForSerializationList(object, feature, featureValueList);
					} else {
						if (feature instanceof EReference && featureValue instanceof Long) {
							EClass referenceClass = getReusable().getCatalogService().getEClassForOid((Long) featureValue);
							if (getQueryObjectProvider().hasReadOrIsGoingToRead(((Long) featureValue))
									|| getQueryObjectProvider().hasReadOrIsGoingToRead(referenceClass)) {
								object.addUseForSerialization(feature);
							}
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected void decideUseForSerializationList(VirtualObject object, EStructuralFeature feature, List featureValue)
			throws DatabaseException {
		if (!(feature.getEType() instanceof EClass)) {
			return;
		}
		for (int i = 0; i < featureValue.size(); i++) {
			Object objectInList = featureValue.get(i);
			if (objectInList instanceof List && objectInList instanceof VirtualObject) {
				List twodimensionalarray = (List) objectInList;
				EStructuralFeature twodimensionalarrayFeature = ((EClass) feature.getEType())
						.getEStructuralFeature("List");
				decideUseForSerializationList((VirtualObject) objectInList, twodimensionalarrayFeature,
						twodimensionalarray);
			} else if (feature instanceof EReference && objectInList instanceof Long) {
				EClass referenceClass = getReusable().getCatalogService().getEClassForOid((Long) objectInList);
				if (getQueryObjectProvider().hasReadOrIsGoingToRead(((Long) objectInList)) || getQueryObjectProvider().hasReadOrIsGoingToRead(referenceClass)) {
					object.addUseForSerialization(feature, i);
				}
			}
		}
	}
}