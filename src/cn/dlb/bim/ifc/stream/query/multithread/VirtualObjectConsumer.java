package cn.dlb.bim.ifc.stream.query.multithread;

import cn.dlb.bim.ifc.stream.VirtualObject;

public abstract class VirtualObjectConsumer implements Runnable {
	
	protected final VirtualObjectOrderList orderList;
	protected final VirtualObjectStorage storage = new VirtualObjectStorage();
	
	public VirtualObjectConsumer(VirtualObjectOrderList orderList) {
		this.orderList = orderList;
	}

	protected void addOrderAndWaitFor(Long oid) throws InterruptedException {
		System.out.println("start wait oid: " + oid);
		
		while (!storage.containsKey(oid)) {
			synchronized (orderList) {
				orderList.addOrder(oid, this);
				orderList.wait();
			}
		}
		System.out.println("end wait oid: " + oid);
	}
	
	protected void transerProduct(VirtualObject virtualObject) {
		storage.put(virtualObject.getOid(), virtualObject);
	}
	
	protected VirtualObject getProduct(Long oid) {
		return storage.get(oid);
	}
}
