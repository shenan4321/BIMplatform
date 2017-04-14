package cn.dlb.bim.ifc.database.queries.om;

import java.util.List;

public interface CanInclude {
	void addInclude(Include include);
	boolean hasIncludes();
	List<Include> getIncludes();
	boolean isIncludeAllFields();
}
