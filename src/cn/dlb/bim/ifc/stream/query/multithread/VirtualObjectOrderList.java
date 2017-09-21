package cn.dlb.bim.ifc.stream.query.multithread;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualObjectOrderList {
	private Map<Long, Set<VirtualObjectConsumer>> orderList;
	
	public VirtualObjectOrderList() {
		orderList = new ConcurrentHashMap<Long, Set<VirtualObjectConsumer>>();
	}
	
	public void addOrder(Long oid, VirtualObjectConsumer consumer) {
		if (!orderList.containsKey(oid)) {
			orderList.put(oid, Collections.synchronizedSet(new HashSet<>()));
		}
		orderList.get(oid).add(consumer);
	}
	
	public Set<VirtualObjectConsumer> getOrderConsumers(Long oid) {
		return orderList.get(oid);
	}
	
	public int size() {
		return orderList.size();
	}
	
	public Set<Long> getOids() {
		return orderList.keySet();
	}
	
	public void clear() {
		orderList.clear();
	}
}
