package cn.dlb.bim.ifc.stream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Charsets;
import cn.dlb.bim.utils.BinUtils;

public abstract class ReadWriteVirtualObject implements MinimalVirtualObject {
	
	private enum ReadWriteType {
		Unkown(0), String(1), Integer(2), Double(3), Float(4), Long(5), Boolean(6), List(7), ByteArray(8), ReadWriteVirtualObject(9);
		
		ReadWriteType(int value) {
			this.value = value;
		}
		private int value;
		public int getValue() {
			return value;
		}
		public static ReadWriteType getType(int value) {
			for (ReadWriteType type : ReadWriteType.values()) {
				if (value == type.getValue()) {
					return type;
				}
			}
			return null;
		}
	}
	
	public abstract void write(ByteBuffer buffer);
	public abstract void read(ByteBuffer buffer);
	public abstract ReadWriteVirtualObject createReadWriteVirtualObject();
	
	@SuppressWarnings({ "rawtypes", "unused" })
	protected void writeFeature(ByteBuffer buffer, Object value){
		ReadWriteType type = ReadWriteType.Unkown;
		if (value instanceof String) {
			type = ReadWriteType.String;
			ensureCapacity(buffer, 4);
			buffer.putInt(type.getValue());
			if (value == null) {
				ensureCapacity(buffer, 4);
				buffer.putInt(-1);
			} else {
				String stringValue = (String) value;
				byte[] bytes = stringValue.getBytes(Charsets.UTF_8);
				ensureCapacity(buffer, 4 + bytes.length);
				buffer.putInt(bytes.length);
				buffer.put(bytes);
			}
		} else if (value instanceof Integer) {
			type = ReadWriteType.Integer;
			ensureCapacity(buffer, 8);
			buffer.putInt(type.getValue());
			if (value == null) {
				buffer.putInt(0);
			} else {
				buffer.putInt((Integer) value);
			}
		} else if (value instanceof Double) {
			type = ReadWriteType.Double;
			ensureCapacity(buffer, 12);
			buffer.putInt(type.getValue());
			if (value == null) {
				buffer.putDouble(0D);
			} else {
				buffer.putDouble((Double) value);
			}
		} else if (value instanceof Float) {
			type = ReadWriteType.Float;
			ensureCapacity(buffer, 8);
			buffer.putInt(type.getValue());
			if (value == null) {
				buffer.putFloat(0F);
			} else {
				buffer.putFloat((Float) value);
			}
		} else if (value instanceof Long) {
			type = ReadWriteType.Long;
			ensureCapacity(buffer, 12);
			buffer.putInt(type.getValue());
			if (value == null) {
				buffer.putLong(0L);
			} else {
				buffer.putLong((Long) value);
			}
		} else if (value instanceof Boolean) {
			type = ReadWriteType.Boolean;
			ensureCapacity(buffer, 5);
			buffer.putInt(type.getValue());
			if (value == null) {
				buffer.put((byte) 0);
			} else {
				buffer.put(((Boolean) value) ? (byte) 1 : (byte) 0);
			}
		} else if (value instanceof List) {
			type = ReadWriteType.List;
			ensureCapacity(buffer, 8);
			buffer.putInt(type.getValue());
			List list = (List) value;
			buffer.putInt(list.size());
			for (int i = 0; i < list.size(); i++) {
				Object valueInList = list.get(i);
				writeFeature(buffer, valueInList);
			}
		} else if (value instanceof byte[]) {
			type = ReadWriteType.ByteArray;
			ensureCapacity(buffer, 4);
			buffer.putInt(type.getValue());
			if (value == null) {
				ensureCapacity(buffer, 4);
				buffer.putInt(0);
			} else {
				byte[] valueByte = ((byte[]) value);
				ensureCapacity(buffer, 4 + valueByte.length);
				buffer.putInt(valueByte.length);
				buffer.put(valueByte);
			}
		} else if (value instanceof ReadWriteVirtualObject) {
			type = ReadWriteType.ReadWriteVirtualObject;
			ensureCapacity(buffer, 4);
			buffer.putInt(type.getValue());
			((ReadWriteVirtualObject) value).write(buffer);
		} else {
			System.err.println("unhandled type.");
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object readFeature(ByteBuffer buffer) {
		int typeValue = buffer.getInt();
		ReadWriteType type = ReadWriteType.getType(typeValue);
		if (type == null) {
			System.out.println();
		}
		Object value = null;
		switch (type) {
			case String:
				int length = buffer.getInt();
				if (length != -1) {
					value = BinUtils.readString(buffer, length);
				} 
				break;
			case Integer:
				value = buffer.getInt();
				break;
			case Double:
				value = buffer.getDouble();
				break;
			case Float:
				value = buffer.getFloat();
				break;
			case Long:
				value = buffer.getLong();
				break;
			case Boolean:
				value = (buffer.get() == 1);
				break;
			case List:
				List list = new ArrayList<>();
				int size = buffer.getInt();
				for (int i = 0; i < size; i++) {
					Object valueInList = readFeature(buffer);
					list.add(valueInList);
				}
				value = list;
				break;
			case ByteArray:
				int byteLength = buffer.getInt();
				if (byteLength != 0) {
					byte[] valueByte = new byte[byteLength];
					buffer.get(valueByte);
					value = valueByte;
				}
				break;
			case ReadWriteVirtualObject:
				ReadWriteVirtualObject readWriteVirtualObject = createReadWriteVirtualObject();
				readWriteVirtualObject.read(buffer);
				value = readWriteVirtualObject;
				break;
			default:
				break;
		}
		return value;
	}
	
	protected void ensureCapacity(ByteBuffer buffer, int sizeToAdd) {
		int currentPos = buffer.position();
		if (buffer.capacity() < currentPos + sizeToAdd) {
			int newSize = Math.max(currentPos + sizeToAdd, buffer.capacity() * 2);
			ByteBuffer newBuffer = ByteBuffer.allocate(newSize);
			buffer.position(0);
			buffer.get(newBuffer.array(), 0, currentPos);
			buffer = newBuffer;
			buffer.position(currentPos);
		}
	}
}
