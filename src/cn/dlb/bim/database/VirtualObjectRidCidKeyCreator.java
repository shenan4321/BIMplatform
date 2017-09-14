package cn.dlb.bim.database;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;

import cn.dlb.bim.ifc.stream.VirtualObject;

public class VirtualObjectRidCidKeyCreator implements SecondaryKeyCreator {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TableWrapper.class);
	
	@Override
	public boolean createSecondaryKey(SecondaryDatabase secondary, DatabaseEntry key, DatabaseEntry data,
			DatabaseEntry result) {
		try {
			byte[] dataBytes = data.getData();
			VirtualObject object = new VirtualObject();
			ByteBuffer dataBuffer = ByteBuffer.wrap(dataBytes);
			object.read(dataBuffer);
			ByteBuffer secondaryKeyBuffer = ByteBuffer.allocate(6);
			Integer rid = object.getRid();
			secondaryKeyBuffer.putInt(rid);
			Short cid = object.getEClassId();
			secondaryKeyBuffer.putShort(cid);
			result.setData(secondaryKeyBuffer.array());
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
		return true;
	}

}
