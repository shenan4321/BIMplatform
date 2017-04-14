package cn.dlb.bim.ifc.database;

import java.util.List;
import java.util.Map;

import cn.dlb.bim.ifc.database.binary.IfcDataBase;
import cn.dlb.bim.ifc.database.binary.IfcModelBinary;
import cn.dlb.bim.ifc.database.queries.om.Query;
import cn.dlb.bim.ifc.database.queries.om.QueryPart;

public class QueryModel extends IfcModelBinary {
	
	private final Query query;

	public QueryModel(IfcDataBase ifcDataBase, Query query) {
		super(ifcDataBase);
		this.query = query;
	}
	
	public void get() {
		List<QueryPart> queryParts = query.getQueryParts();
		for (QueryPart queryPart : queryParts) {
			Map<String, Object> res = queryPart.getProperties();
		}
	}

}
