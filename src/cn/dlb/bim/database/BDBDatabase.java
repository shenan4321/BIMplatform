package cn.dlb.bim.database;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BDBDatabase {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BDBDatabase.class);
	
	private Set<TableWrapper> tables = new LinkedHashSet<>();
	
	public void close() {
		for (TableWrapper tableWrapper : tables) {
			tableWrapper.close();
		}
	}
	
	public TableWrapper getTableWrapper(String tableName) {
		for (TableWrapper tableWrapper : tables) {
			if (tableName.equals(tableWrapper.getTableName())) {
				return tableWrapper;
			}
		}
		return null;
	}

	public Set<TableWrapper> getTables() {
		return tables;
	}

	public void setTables(Set<TableWrapper> tables) {
		this.tables = tables;
	}
	
}
