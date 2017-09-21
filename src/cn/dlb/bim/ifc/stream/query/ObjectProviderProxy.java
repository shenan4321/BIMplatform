package cn.dlb.bim.ifc.stream.query;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import cn.dlb.bim.database.DatabaseException;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.serializers.ObjectProvider;

public class ObjectProviderProxy implements ObjectProvider {

	private ObjectProvider objectProvider;
	private ObjectListener objectListener;

	public ObjectProviderProxy(ObjectProvider objectProvider, ObjectListener objectListener) {
		this.objectProvider = objectProvider;
		this.objectListener = objectListener;
	}

	@Override
	public VirtualObject next() throws DatabaseException, InterruptedException, ExecutionException {
		VirtualObject next = objectProvider.next();
		if (next != null) {
			objectListener.newObject(next);
		}
		return next;
	}

	@Override
	public ObjectProvider copy() throws IOException, QueryException {
		return objectProvider.copy();
	}
}