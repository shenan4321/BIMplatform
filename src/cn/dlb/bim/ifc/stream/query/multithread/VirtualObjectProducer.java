package cn.dlb.bim.ifc.stream.query.multithread;

import java.util.Collection;
import java.util.Set;

import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.query.QueryContext;

public class VirtualObjectProducer implements Runnable {
	private final QueryContext queryContext;
	private static final int BATCH_QUERY_SIZE = 1000;
	private long listProduceTime;
	
	private final VirtualObjectOrderList orderList;
	
	public VirtualObjectProducer(QueryContext queryContext, VirtualObjectOrderList orderList) {
		this.queryContext = queryContext;
		this.orderList = orderList;
		listProduceTime = System.currentTimeMillis();
	}
	
	@Override
	public void run() {
		while (true) {
			long currentTime = System.currentTimeMillis();
			if (orderList.size() >= BATCH_QUERY_SIZE || currentTime - listProduceTime > 2000) {
				Integer rid = queryContext.getRid();
				Collection<VirtualObject> virtualObjects = queryContext.getVirtualObjectService().findByRidAndOids(rid, orderList.getOids());
				for (VirtualObject virtualObject : virtualObjects) {
					Set<VirtualObjectConsumer> consumers = orderList.getOrderConsumers(virtualObject.getOid());
					for (VirtualObjectConsumer consumer : consumers) {
						consumer.transerProduct(virtualObject);
					}
				}
				synchronized (orderList) {
					orderList.notifyAll();
					orderList.clear();
				}
				listProduceTime = System.currentTimeMillis();
			}
		}
	}

}
