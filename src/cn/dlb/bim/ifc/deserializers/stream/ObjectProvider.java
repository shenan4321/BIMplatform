package cn.dlb.bim.ifc.deserializers.stream;

import java.io.IOException;

import cn.dlb.bim.ifc.database.DatabaseException;
import cn.dlb.bim.ifc.query.QueryException;

public interface ObjectProvider {

	/**
	 * @return Will return new objects as long as the query finds more. Will never return the same object twice. Returns @null when no more objects can be found.
	 * @throws BimserverDatabaseException
	 */
	VirtualObject next() throws DatabaseException;

	ObjectProvider copy() throws IOException, QueryException;
}
