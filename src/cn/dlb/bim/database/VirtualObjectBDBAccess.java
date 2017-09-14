package cn.dlb.bim.database;

import java.nio.ByteBuffer;

import com.sleepycat.je.Transaction;

import cn.dlb.bim.ifc.stream.VirtualObject;

public class VirtualObjectBDBAccess {
	private BDBDatabase database;

	public void save(VirtualObject virtualObject, Transaction transaction) {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		virtualObject.write(buffer);
		ByteBuffer keyBuffer = ByteBuffer.allocate(12);
		keyBuffer.putInt(virtualObject.getRid());
		keyBuffer.putLong(virtualObject.getOid());
		database.getTableWrapper("virtualObject").storeNoOverwrite(keyBuffer.array(), buffer.array(), 0,
				buffer.position(), transaction);
	}

	public void update(VirtualObject virtualObject, Transaction transaction) {
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		virtualObject.write(buffer);
		ByteBuffer keyBuffer = ByteBuffer.allocate(12);
		keyBuffer.putInt(virtualObject.getRid());
		keyBuffer.putLong(virtualObject.getOid());
		database.getTableWrapper("virtualObject").store(keyBuffer.array(), buffer.array(), 0, buffer.position(),
				transaction);
	}

	public VirtualObject findByRidOid(Integer rid, Long oid) {
		ByteBuffer keyBuffer = ByteBuffer.allocate(12);
		keyBuffer.putInt(rid);
		keyBuffer.putLong(oid);
		byte[] bytes = database.getTableWrapper("virtualObject").get(keyBuffer.array(), null);
		if (bytes != null) {
			VirtualObject virtualObject = null;
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			virtualObject = new VirtualObject();
			virtualObject.read(buffer);
			return virtualObject;
		} else {
			return null;
		}
	}

	public VirtualObject findOneByRidCid(Integer rid, Short cid) {
		ByteBuffer keyBuffer = ByteBuffer.allocate(6);
		keyBuffer.putInt(rid);
		keyBuffer.putShort(cid);
		byte[] bytes = database.getTableWrapper("virtualObject").get("virtualObject_rid_cid", keyBuffer.array(), null);
		if (bytes != null) {
			VirtualObject virtualObject = null;
			ByteBuffer buffer = ByteBuffer.wrap(bytes);
			virtualObject = new VirtualObject();
			virtualObject.read(buffer);
			return virtualObject;
		} else {
			return null;
		}
	}

	public BDBDatabase getDatabase() {
		return database;
	}

	public void setDatabase(BDBDatabase batabase) {
		this.database = batabase;
	}

}
