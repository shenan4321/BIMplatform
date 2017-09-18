package cn.dlb.bim.database;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;

import cn.dlb.bim.database.record.Record;
import cn.dlb.bim.database.record.RecordIterator;
import cn.dlb.bim.ifc.stream.VirtualObject;

public class VirtualObjectBDBAccess {
	
	private static final String tableName = "virtualObject";
	private static final String ridCidIndex = "rid_cid";
	
	private BDBDatabase database;
	
	public VirtualObjectBDBAccess(BDBDatabase database) {
		this.database = database;
		TableWrapper virtualObjectTable = new TableWrapper(database.getEnvironment(), true, tableName);
		virtualObjectTable.createIndex(ridCidIndex, new VirtualObjectRidCidKeyCreator());
		database.getTables().add(virtualObjectTable);
	}

	public void save(VirtualObject virtualObject, Transaction transaction) {
		ByteBuffer buffer = ByteBuffer.allocate(256);
		buffer = virtualObject.write(buffer);
		
		ByteBuffer keyBuffer = ByteBuffer.allocate(12);
		keyBuffer.putInt(virtualObject.getRid());
		keyBuffer.putLong(virtualObject.getOid());
		database.getTableWrapper(tableName).storeNoOverwrite(keyBuffer.array(), buffer.array(), 0,
				buffer.position(), transaction);
	}

	public void update(VirtualObject virtualObject, Transaction transaction) {
		ByteBuffer buffer = ByteBuffer.allocate(256);
		buffer = virtualObject.write(buffer);
		ByteBuffer keyBuffer = ByteBuffer.allocate(12);
		keyBuffer.putInt(virtualObject.getRid());
		keyBuffer.putLong(virtualObject.getOid());
		database.getTableWrapper(tableName).store(keyBuffer.array(), buffer.array(), 0, buffer.position(),
				transaction);
	}

	public VirtualObject findByRidOid(Integer rid, Long oid) {
		ByteBuffer keyBuffer = ByteBuffer.allocate(12);
		keyBuffer.putInt(rid);
		keyBuffer.putLong(oid);
		byte[] bytes = database.getTableWrapper(tableName).get(keyBuffer.array(), null);
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
		byte[] bytes = database.getTableWrapper(tableName).get(ridCidIndex, keyBuffer.array(), null);
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
	
	public List<VirtualObject> findByRidCid(Integer rid, Short cid) {
		ByteBuffer keyBuffer = ByteBuffer.allocate(6);
		keyBuffer.putInt(rid);
		keyBuffer.putShort(cid);
		RecordIterator iterator = database.getTableWrapper(tableName).getRecordIterator(ridCidIndex, keyBuffer.array(), null);
		Record record = iterator.next();
		List<VirtualObject> result = new ArrayList<VirtualObject>();
		while (record != null) {
			byte[] data = record.getValue();
			VirtualObject virtualObject = new VirtualObject();
			ByteBuffer buffer = ByteBuffer.wrap(data);
			virtualObject.read(buffer);
			result.add(virtualObject);
		}
		return result;
	}
	
	public Transaction beginTransaction() {
		TransactionConfig transactionConfig = new TransactionConfig();
		transactionConfig.setReadCommitted(true);
		return database.getEnvironment().getEnvironment().beginTransaction(null, transactionConfig);
	}

	public BDBDatabase getDatabase() {
		return database;
	}

	public void setDatabase(BDBDatabase batabase) {
		this.database = batabase;
	}

}
