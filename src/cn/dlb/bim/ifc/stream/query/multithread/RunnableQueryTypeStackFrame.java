package cn.dlb.bim.ifc.stream.query.multithread;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.query.CanInclude;
import cn.dlb.bim.ifc.stream.query.Include;
import cn.dlb.bim.ifc.stream.query.ObjectProvidingStackFrame;
import cn.dlb.bim.ifc.stream.query.QueryContext;
import cn.dlb.bim.ifc.stream.query.QueryException;
import cn.dlb.bim.ifc.stream.query.QueryPart;

public class RunnableQueryTypeStackFrame extends RunnableDatabaseReadingStackFrame implements ObjectProvidingStackFrame {
	
	private Collection<VirtualObject> virtualObjects;
	
	private EClass eClass;
	
	public RunnableQueryTypeStackFrame(MultiThreadQueryObjectProvider queryObjectProvider, EClass eClass, QueryContext reusable, QueryPart queryPart) {
		super(reusable, queryObjectProvider, queryPart);
		this.eClass = eClass;
		
		Integer rid = reusable.getRid();
		Short cid = reusable.getCatalogService().getCidOfEClass(eClass);
		virtualObjects = reusable.getVirtualObjectService().findByRidAndCid(rid, cid);
		
	}
	
	@Override
	public boolean process() throws DatabaseException, QueryException, JsonParseException, JsonMappingException, IOException, InterruptedException {
			
		for (VirtualObject virtualObject : virtualObjects) {
			currentObject = virtualObject;
			
			decideUseForSerialization(currentObject);
			
			processPossibleIncludes(eClass, getQueryPart());
			
			getQueryObjectProvider().addToStorage(currentObject);
		}
		
		return true;
	}
	
	protected void processPossibleIncludes(EClass previousType, CanInclude canInclude) throws QueryException, DatabaseException {
		if (currentObject != null) {
			if (canInclude.hasIncludes()) {
				for (Include include : canInclude.getIncludes()) {
					processPossibleInclude(canInclude, include);
				}
			} else if (canInclude.isIncludeAllFields()) {
				for (EReference eReference : currentObject.eClass().getEAllReferences()) {
					Include include = new Include(getReusable().getPackageMetaData());
					
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
		if (include.hasDirectFields()) {
			for (EReference eReference : include.getFieldsDirect()) {
				Object ref = currentObject.get(eReference.getName());
				if (ref != null) {
					currentObject.setReference(eReference, (Long)ref);
				}
			}
		}
//		if (include.hasFields()) {
//			for (EStructuralFeature eStructuralFeature : include.getFields()) {
//				// TODO do we really have to iterate through the EAtrributes as well?
//				currentObject.addUseForSerialization(eStructuralFeature);
//			}
//		}

		getQueryObjectProvider().push(new RunnableQueryIncludeStackFrame(getQueryObjectProvider(), getReusable(), previousInclude, include, currentObject, getQueryPart()));
	}

	@Override
	public VirtualObject getCurrentObject() {
		return currentObject;
	}
	
	@Override
	public String toString() {
		return "QueryTypeStackFrame (" + eClass.getName() + ")";
	}

	@Override
	public int stackFrameHash() {
		List<Object> hashElements = Arrays.asList(getClass(), getQueryObjectProvider(), eClass.hashCode(), getQueryPart(), getReusable());
		return Arrays.hashCode(hashElements.toArray());
	}

}