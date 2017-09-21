package cn.dlb.bim.ifc.stream.serializers;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.query.QueryException;

public interface ObjectProvider {

	/**
	 * @return Will return new objects as long as the query finds more. Will never return the same object twice. Returns @null when no more objects can be found.
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws BimserverDatabaseException
	 */
	VirtualObject next() throws DatabaseException, InterruptedException, ExecutionException;

	ObjectProvider copy() throws IOException, QueryException;
}
