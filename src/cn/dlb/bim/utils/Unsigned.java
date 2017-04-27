package cn.dlb.bim.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Unsigned {

	public static short getUnsignedByte(ByteBuffer buff) {
		return (short) (buff.get() & 0xff);
	}

	public static short getUnsignedByte(ByteBuffer buff, int position) {
		return (short) (buff.get(position) & (short) 0xff);
	}

	public static void putUnsignedByte(ByteBuffer buff, int value) {
		buff.put((byte) (value & 0xff));
	}

	public static void putUnsignedByte(ByteBuffer buff, int position, int value) {
		buff.put(position, (byte) (value & 0xff));
	}

	// --------------------------------------------
	public static int getUnsignedShort(ByteBuffer buff) {
		return buff.getShort() & 0xffff;
	}

	public static int getUnsignedShort(ByteBuffer buff, int position) {
		return buff.getShort(position) & (short) 0xffff;
	}

	public static void putUnsignedShort(ByteBuffer buff, int value) {
		buff.putShort((short) (value & 0xffff));
	}

	public static void putUnsignedShort(ByteBuffer buff, int position, int value) {
		buff.putShort(position, (short) (value & 0xffff));
	}

	// --------------------------------------------
	public static long getUnsignedInt(ByteBuffer buff) {
		return buff.getInt() & 0xffffffffL;
	}

	public static long getUnsignedInt(ByteBuffer buff, int position) {
		return buff.getInt(position) & 0xffffffffL;
	}
	
	public static long getUnsignedIntLE(ByteBuffer buff, int position) {
		buff.order(ByteOrder.LITTLE_ENDIAN);
		long result = getUnsignedInt(buff, position);
		buff.order(ByteOrder.BIG_ENDIAN);
		return result;
	}

	public static void putUnsignedInt(ByteBuffer buff, int value) {
		buff.putInt((int) (value & 0xffffffffL));
	}

	public static void putUnsignedInt(ByteBuffer buff, int position, int value) {
		buff.putInt(position, (int) (value & 0xffffffffL));
	}
	
	public static void putUnsignedIntLE(ByteBuffer buff, int position, int value) {
		buff.order(ByteOrder.LITTLE_ENDIAN);
		buff.putInt(position, (int) (value & 0xffffffffL));
		buff.order(ByteOrder.BIG_ENDIAN);
	}

}
