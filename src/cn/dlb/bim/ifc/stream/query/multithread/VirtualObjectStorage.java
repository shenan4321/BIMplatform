package cn.dlb.bim.ifc.stream.query.multithread;

import java.util.concurrent.ConcurrentHashMap;
import cn.dlb.bim.ifc.stream.VirtualObject;

public class VirtualObjectStorage extends ConcurrentHashMap<Long, VirtualObject> {
	private static final long serialVersionUID = 7260867707834865750L;
}
