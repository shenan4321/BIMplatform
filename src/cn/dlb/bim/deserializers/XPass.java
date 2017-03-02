package cn.dlb.bim.deserializers;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import com.google.common.base.Charsets;

public class XPass extends Pass {

	@Override
	public String process(int lineNumber, String result) throws DeserializeException {
		while (result.contains("\\X\\")) {
			int index = result.indexOf("\\X\\");
			int code = Integer.parseInt(result.substring(index + 3, index + 5), 16);
			ByteBuffer b = ByteBuffer.wrap(new byte[] { (byte) (code) });
			CharBuffer decode = Charsets.ISO_8859_1.decode(b);
			result = result.substring(0, index) + decode.get() + result.substring(index + 5);
		}
		return result;
	}
}