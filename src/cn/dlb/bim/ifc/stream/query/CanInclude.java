package cn.dlb.bim.ifc.stream.query;

import java.util.List;

public interface CanInclude {
	void addInclude(Include include);
	boolean hasIncludes();
	List<Include> getIncludes();
	boolean isIncludeAllFields();
}
