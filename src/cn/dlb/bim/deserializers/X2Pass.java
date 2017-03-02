package cn.dlb.bim.deserializers;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import com.google.common.base.Charsets;

public class X2Pass extends Pass {

	@Override
	public String process(int lineNumber, String result) throws DeserializeException {
		while (result.contains("\\X2\\")) {
			int index = result.indexOf("\\X2\\");
			int indexOfEnd = result.indexOf("\\X0\\", index);
			if (indexOfEnd == -1) {
				throw new DeserializeException(lineNumber, "\\X2\\ not closed with \\X0\\");
			}
			if ((indexOfEnd - index) % 4 != 0) {
				throw new DeserializeException(lineNumber, "Number of hex chars in \\X2\\ definition not divisible by 4");
			}
			try {
				ByteBuffer buffer = ByteBuffer.wrap(Hex.decodeHex(result.substring(index + 4, indexOfEnd).toCharArray()));
				CharBuffer decode = Charsets.UTF_16BE.decode(buffer);
				result = result.substring(0, index) + decode.toString() + result.substring(indexOfEnd + 4);
			} catch (DecoderException e) {
				throw new DeserializeException(lineNumber, e);
			}
		}
		return result;
	}
}
