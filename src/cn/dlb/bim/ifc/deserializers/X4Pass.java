package cn.dlb.bim.ifc.deserializers;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

public class X4Pass extends Pass {
	public String process(int lineNumber, String result) throws DeserializeException {
		while (result.contains("\\X4\\")) {
			int index = result.indexOf("\\X4\\");
			int indexOfEnd = result.indexOf("\\X0\\", index);
			if (indexOfEnd == -1) {
				throw new DeserializeException(lineNumber, "\\X4\\ not closed with \\X0\\");
			}
			if ((indexOfEnd - (index + 4)) % 8 != 0) {
				throw new DeserializeException(lineNumber, "Number of hex chars in \\X4\\ definition not divisible by 8");
			}
			try {
				ByteBuffer buffer = ByteBuffer.wrap(Hex.decodeHex(result.substring(index + 4, indexOfEnd).toCharArray()));
				CharBuffer decode = Charset.forName("UTF-32").decode(buffer);
				result = result.substring(0, index) + decode.toString() + result.substring(indexOfEnd + 4);
			} catch (DecoderException e) {
				throw new DeserializeException(lineNumber, e);
			} catch (UnsupportedCharsetException e) {
				throw new DeserializeException(lineNumber, "UTF-32 is not supported on your system", e);
			}
		}
		return result;
	}
}
