package cn.dlb.bim.ifc.stream.query;

import java.io.IOException;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.springframework.data.util.CloseableIterator;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import cn.dlb.bim.ifc.database.DatabaseException;
import cn.dlb.bim.ifc.database.queries.om.CanInclude;
import cn.dlb.bim.ifc.database.queries.om.Include;
import cn.dlb.bim.ifc.database.queries.om.QueryException;
import cn.dlb.bim.ifc.database.queries.om.QueryPart;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.service.PlatformService;

public class QueryTypeStackFrame extends DatabaseReadingStackFrame implements ObjectProvidingStackFrame {
	
	CloseableIterator<VirtualObject> iterator;
	
	private EClass eClass;
	
	public QueryTypeStackFrame(QueryObjectProvider queryObjectProvider, EClass eClass, QueryContext reusable, QueryPart queryPart) {
		super(reusable, queryObjectProvider, queryPart);
		this.eClass = eClass;
		
		PlatformService platformService = reusable.getPlatformService();
		Integer rid = reusable.getRid();
		Short cid = platformService.getCidOfEClass(eClass);
		iterator = platformService.streamVirtualObject(rid, cid);
		
	}
	
	@Override
	boolean process() throws DatabaseException, QueryException, JsonParseException, JsonMappingException, IOException {
		
		currentObject = iterator.next();
		decideUseForSerialization(currentObject);
		
		processPossibleIncludes(eClass, getQueryPart());
		
		return false;
	}
	
	protected void processPossibleIncludes(EClass previousType, CanInclude canInclude) throws QueryException, DatabaseException {
		if (currentObject != null) {
			if (canInclude.hasIncludes()) {
				for (Include include : canInclude.getIncludes()) {
					processPossibleInclude(canInclude, include);
				}
			} else if (canInclude.isIncludeAllFields()) {
				EClass eclass = getQueryObjectProvider().getPlatformService().getEClassForCid(currentObject.getEClassId());
				for (EReference eReference : eclass.getEAllReferences()) {
					Include include = new Include(getReusable().getPackageMetaData());
					
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
			EClass eclass = getReusable().getPlatformService().getEClassForCid(currentObject.getEClassId());
			for (EClass filterClass : include.getTypes()) {
				if (!filterClass.isSuperTypeOf(eclass)) {
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

		getQueryObjectProvider().push(new QueryIncludeStackFrame(getQueryObjectProvider(), getReusable(), previousInclude, include, currentObject, getQueryPart()));
	}

	@Override
	public VirtualObject getCurrentObject() {
		return currentObject;
	}

}