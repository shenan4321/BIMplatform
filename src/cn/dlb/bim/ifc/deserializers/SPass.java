package cn.dlb.bim.ifc.deserializers;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import com.google.common.base.Charsets;

public class SPass extends Pass {
	public String process(int lineNumber, String result) {
		while (result.contains("\\S\\")) {
			int index = result.indexOf("\\S\\");
			char x = result.charAt(index + 3);
			ByteBuffer b = ByteBuffer.wrap(new byte[] { (byte) (x + 128) });
			CharBuffer decode = Charsets.ISO_8859_1.decode(b);
			result = result.substring(0, index) + decode.get() + result.substring(index + 4);
		}
		return result;
	}
}
