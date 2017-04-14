package cn.dlb.bim.ifc.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import cn.dlb.bim.ifc.emf.PackageMetaData;

public class Include implements CanInclude {
	private Set<EClass> types;
	private List<EReference> fields;
	private List<Include> includes;
	
	private PackageMetaData packageMetaData;
	
	public Include(PackageMetaData packageMetaData) {
		this.packageMetaData = packageMetaData;
	}

	@Override
	public void addInclude(Include newInclude) {
		if (includes == null) {
			includes = new ArrayList<Include>();
		}
		includes.add(newInclude);
	}

	@Override
	public boolean hasIncludes() {
		return includes != null;
	}

	@Override
	public List<Include> getIncludes() {
		return includes;
	}

	@Override
	public boolean isIncludeAllFields() {
		return false;
	}
	
	public void addField(String fieldName) throws QueryException {
		EReference feature = null;
		for (EClass eClass : types) {
			if (eClass.getEStructuralFeature(fieldName) == null) {
				throw new QueryException("Class \"" + eClass.getName() + "\" does not have the field \"" + fieldName + "\"");
			}
			if (feature == null) {
				if (!(eClass.getEStructuralFeature(fieldName) instanceof EReference)) {
					throw new QueryException(fieldName + " is not a reference");
				}
				feature = (EReference) eClass.getEStructuralFeature(fieldName);
			} else {
				if (feature != eClass.getEStructuralFeature(fieldName)) {
					throw new QueryException("Classes \"" + eClass.getName() + "\" and \"" + feature.getEContainingClass().getName() + "\" have fields with the same name, but they are not logically the same");
				}
			}
		}
		if (fields == null) {
			fields = new ArrayList<>();
		}
		fields.add(feature);
	}
	
	public void addType(EClass eClass, boolean includeAllSubTypes) {
		if (eClass == null) {
			throw new IllegalArgumentException("eClass cannot be null");
		}
		if (types == null) {
			types = new HashSet<>();
		}
		types.add(eClass);
		if (includeAllSubTypes) {
			types.addAll(packageMetaData.getAllSubClasses(eClass));
		}
	}
	
	public Set<EClass> getTypes() {
		return types;
	}
	
	public List<EReference> getFields() {
		return fields;
	}
	
	public boolean hasFields() {
		return fields != null;
	}

	public boolean hasTypes() {
		return types != null;
	}

}
