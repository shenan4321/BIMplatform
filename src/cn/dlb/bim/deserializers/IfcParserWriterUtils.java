package cn.dlb.bim.deserializers;

/******************************************************************************
 * Copyright (C) 2009-2016  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

import cn.dlb.bim.emf.PackageMetaData;
import cn.dlb.bim.serializers.SerializerException;

public class IfcParserWriterUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(IfcParserWriterUtils.class);
	private static final boolean USE_ISO_8859_1 = false;
	
	private static final ParserPlan[] plans = new ParserPlan[]{
		new ParserPlan(new XPass(), new X2Pass(), new X4Pass(), new SPass()),
		new ParserPlan(new SPass(), new XPass(), new X2Pass(), new X4Pass())
	};

	public static Object convertSimpleValue(PackageMetaData packageMetaData, Class<?> instanceClass, String value, int lineNumber) throws DeserializeException {
		if (!value.equals("")) {
			if (instanceClass == Integer.class || instanceClass == int.class) {
				try {
					return Integer.parseInt(value);
				} catch (NumberFormatException e) {
					try {
			            new BigInteger(value);
			        } catch (Exception e1) {
			            throw e; // re-throw, this was a formatting problem
			        }
					throw new NumberFormatException("Input is outside of Integer range (" + value + ")");
				}
			} else if (instanceClass == Long.class || instanceClass == long.class) {
				return Long.parseLong(value);
			} else if (instanceClass == Boolean.class || instanceClass == boolean.class) {
				return Boolean.parseBoolean(value);
			} else if (instanceClass.getSimpleName().equals("Tristate")) {
				EEnum eEnum = packageMetaData.getEEnum("Tristate");
				if (value.toString().equals("TRUE")) {
					return eEnum.getEEnumLiteral("TRUE");
				} else if (value.toString().equals("FALSE")) {
					return eEnum.getEEnumLiteral("FALSE");
				} else if (value.toString().equals("UNDEFINED")) {
					return eEnum.getEEnumLiteral("UNDEFINED");
				}
				throw new DeserializeException("Unknown value: " + value);
			} else if (instanceClass == Double.class || instanceClass == double.class) {
				try {
					return Double.parseDouble(value);
				} catch (NumberFormatException e) {
					throw new DeserializeException(lineNumber, "Incorrect double floating point value: " + value, e);
				}
			} else if (instanceClass == String.class) {
				if (value.startsWith("'") && value.endsWith("'")) {
					return readString(value, lineNumber);
				} else {
					return value;
				}
			} else if (instanceClass == byte[].class) {
				if (value.startsWith("\"") && value.endsWith("\"")) {
					try {
						// TODO Skipping the first one here to make even...
						String substring = value.substring(2, value.length() - 1);
						byte[] decoded = Hex.decodeHex(substring.toCharArray());
						return decoded;
					} catch (DecoderException e) {
						throw new DeserializeException(e);
					}
				} else {
					throw new DeserializeException("Byte[] not starting/ending with \"");
				}
			} else {
				throw new DeserializeException("Unimplemented " + instanceClass);
			}
		}
		return null;
	}
	
	/**
	 * Decode a piece of text according to ISO-10303-21
	 * 
	 * Known possible problems:
	 * 	- The order of parsing (X, X2, X4, S) at the time of writing is arbitrary (and has been changed, the S was in front before, causing problems with IFC files outputted by other software). The ISO docs don't say anything about it.
	 * 		- Example: \X2\3010\X0\S\X2\301194DD540891D1\X0\, results will be different depending on whether the 'X2' is parsed first or the 'S'
	 *  - This code will process the output of one encoding phase, and possibly use it in the next phase, this is most definitely not according to the standard. Somehow it should be marked which parts are still up for parsing and which parst aren't
	 *  - Overall structure of the code is quite error prone, using a well known parser library/strategy might be better
	 *  - One more note of frustration: It is completely insane to encode text within text
	 * 
	 * 
	 * @param value The original value
	 * @param lineNumber, used for throwing exceptions with line numbers
	 * @return The decoded string
	 * @throws DeserializeException
	 */
	public static String readString(String value, int lineNumber) throws DeserializeException {
		String result = value.substring(1, value.length() - 1);
		// Replace all '' with '
		while (result.contains("''")) {
			int index = result.indexOf("''");
			result = result.substring(0, index) + "'" + result.substring(index + 2);
		}
		
		for (ParserPlan parserPlan : plans) {
			try {
				result = parserPlan.process(lineNumber, result);
				break;
			} catch (NumberFormatException e) {
				if (parserPlan == plans[plans.length -1]) {
					throw e;
				}
				// Try the next plan
			}
		}
		// Replace all \\ with \
		while (result.contains("\\\\")) {
			int index = result.indexOf("\\\\");
			result = result.substring(0, index) + "\\" + result.substring(index + 2);
		}
		return result;
	}

	public static void writePrimitive(Object val, OutputStream outputStream) throws SerializerException, IOException {
		if (val.getClass().getSimpleName().equals("Tristate")) {
			if (val.toString().equals("TRUE")) {
				outputStream.write(".T.".getBytes(Charsets.UTF_8));
			} else if (val.toString().equals("FALSE")) {
				outputStream.write(".F.".getBytes(Charsets.UTF_8));
			} else if (val.toString().equals("UNDEFINED")) {
				outputStream.write(".U.".getBytes(Charsets.UTF_8));
			}
		} else if (val instanceof Double) {
			if (((Double)val).isInfinite() || (((Double)val).isNaN())) {
				LOGGER.info("Serializing infinite or NaN double as 0.0");
				outputStream.write("0.0".getBytes(Charsets.UTF_8));
			} else {
				String string = val.toString();
				if (string.endsWith(".0")) {
					outputStream.write((string.substring(0, string.length() - 1)).getBytes(Charsets.UTF_8));
				} else {
					outputStream.write(string.getBytes(Charsets.UTF_8));
				}
			}
		} else if (val instanceof Boolean) {
			Boolean bool = (Boolean)val;
			if (bool) {
				outputStream.write(".T.".getBytes(Charsets.UTF_8));
			} else {
				outputStream.write(".F.".getBytes(Charsets.UTF_8));
			}
		} else if (val instanceof String) {
			outputStream.write("'".getBytes(Charsets.UTF_8));
			String stringVal = (String)val;
			for (int i=0; i<stringVal.length(); i++) {
				char c = stringVal.charAt(i);
				if (c == '\'') {
					outputStream.write("\'\'".getBytes(Charsets.UTF_8));
				} else if (c == '\\') {
					outputStream.write("\\\\".getBytes(Charsets.UTF_8));
				} else if (c >= 32 && c <= 126) {
					// ISO 8859-1
					outputStream.write(("" + c).getBytes(Charsets.UTF_8));
				} else if (c < 255) {
					//  ISO 10646 and ISO 8859-1 are the same < 255 , using ISO_8859_1
					outputStream.write(("\\X\\" + new String(Hex.encodeHex(Charsets.ISO_8859_1.encode(CharBuffer.wrap(new char[]{(char) c})).array())).toUpperCase()).getBytes(Charsets.UTF_8));
				} else {
					if (USE_ISO_8859_1) {
						// ISO 8859-1 with -128 offset
						ByteBuffer encode = Charsets.ISO_8859_1.encode(new String(new char[]{(char) (c - 128)}));
						outputStream.write(("\\S\\" + (char)encode.get()).getBytes(Charsets.UTF_8));
					} else {
						// The following code has not been tested (2012-04-25)
						// Use UCS-2 or UCS-4
						
						// TODO when multiple sequential characters should be encoded in UCS-2 or UCS-4, we don't really need to add all those \X0\ \X2\ and \X4\ chars
						if (Character.isLowSurrogate(c)) {
							throw new SerializerException("Unexpected low surrogate range char");
						} else if (Character.isHighSurrogate(c)) {
							// We need UCS-4, this is probably never happening
							if (i + 1 < stringVal.length()) {
								char low = stringVal.charAt(i + 1);
								if (!Character.isLowSurrogate(low)) {
									throw new SerializerException("High surrogate char should be followed by char in low surrogate range");
								}
								try {
									outputStream.write(("\\X4\\" + new String(Hex.encodeHex(Charset.forName("UTF-32").encode(new String(new char[]{c, low})).array())).toUpperCase() + "\\X0\\").getBytes(Charsets.UTF_8));
								} catch (UnsupportedCharsetException e) {
									throw new SerializerException(e);
								}
								i++;
							} else {
								throw new SerializerException("High surrogate char should be followed by char in low surrogate range, but end of string reached");
							}
						} else {
							// UCS-2 will do
							outputStream.write(("\\X2\\" + new String(Hex.encodeHex(Charsets.UTF_16BE.encode(CharBuffer.wrap(new char[]{c})).array())).toUpperCase() + "\\X0\\").getBytes(Charsets.UTF_8));
						}
					}
				}
			}
			outputStream.write("'".getBytes(Charsets.UTF_8));
		} else if (val instanceof Enumerator) {
			outputStream.write(("." + val + ".").getBytes(Charsets.UTF_8));
		} else if (val instanceof byte[]) {
			// TODO printing default leading 0, must be wrong
			outputStream.write(("\"0" + Hex.encodeHexString((byte[])val) + "\"").getBytes(Charsets.UTF_8));
		} else {
			outputStream.write((val == null ? "$" : val.toString()).getBytes(Charsets.UTF_8));
		}
	}

	public static void writePrimitive(Object val, PrintWriter printWriter) throws SerializerException, IOException {
		if (val.getClass().getSimpleName().equals("Tristate")) {
			if (val.toString().equals("TRUE")) {
				printWriter.write(".T.");
			} else if (val.toString().equals("FALSE")) {
				printWriter.write(".F.");
			} else if (val.toString().equals("UNDEFINED")) {
				printWriter.write(".U.");
			}
		} else if (val instanceof Double) {
			if (((Double)val).isInfinite() || (((Double)val).isNaN())) {
				LOGGER.info("Serializing infinite or NaN double as 0.0");
				printWriter.write("0.0");
			} else {
				String string = val.toString();
				if (string.endsWith(".0")) {
					printWriter.write((string.substring(0, string.length() - 1)));
				} else {
					printWriter.write(string);
				}
			}
		} else if (val instanceof Boolean) {
			Boolean bool = (Boolean)val;
			if (bool) {
				printWriter.write(".T.");
			} else {
				printWriter.write(".F.");
			}
		} else if (val instanceof String) {
			printWriter.write('\'');
			String stringVal = (String)val;
			for (int i=0; i<stringVal.length(); i++) {
				char c = stringVal.charAt(i);
				if (c == '\'') {
					printWriter.write(new char[]{'\'', '\''});
				} else if (c == '\\') {
					printWriter.write(new char[]{'\\', '\\'});
				} else if (c >= 32 && c <= 126) {
					// ISO 8859-1
					printWriter.write(new char[]{c});
				} else if (c < 255) {
					//  ISO 10646 and ISO 8859-1 are the same < 255 , using ISO_8859_1
					printWriter.write("\\X\\");
					printWriter.write(Hex.encodeHex(Charsets.ISO_8859_1.encode(CharBuffer.wrap(new char[]{(char) c})).array(), false));
				} else {
					if (USE_ISO_8859_1) {
						// ISO 8859-1 with -128 offset
						printWriter.write("\\S\\");
						printWriter.write((char)Charsets.ISO_8859_1.encode(new String(new char[]{(char) (c - 128)})).get());
					} else {
						// The following code has not been tested (2012-04-25)
						// Use UCS-2 or UCS-4
						
						// TODO when multiple sequential characters should be encoded in UCS-2 or UCS-4, we don't really need to add all those \X0\ \X2\ and \X4\ chars
						if (Character.isLowSurrogate(c)) {
							throw new SerializerException("Unexpected low surrogate range char");
						} else if (Character.isHighSurrogate(c)) {
							// We need UCS-4, this is probably never happening
							if (i + 1 < stringVal.length()) {
								char low = stringVal.charAt(i + 1);
								if (!Character.isLowSurrogate(low)) {
									throw new SerializerException("High surrogate char should be followed by char in low surrogate range");
								}
								try {
									printWriter.write("\\X4\\");
									printWriter.write(Hex.encodeHex(Charset.forName("UTF-32").encode(new String(new char[]{c, low})).array(), false));
									printWriter.write("\\X0\\");
								} catch (UnsupportedCharsetException e) {
									throw new SerializerException(e);
								}
								i++;
							} else {
								throw new SerializerException("High surrogate char should be followed by char in low surrogate range, but end of string reached");
							}
						} else {
							// UCS-2 will do
							printWriter.write(("\\X2\\"));
							printWriter.write(Hex.encodeHex(Charsets.UTF_16BE.encode(CharBuffer.wrap(new char[]{c})).array(), false));
							printWriter.write("\\X0\\");
						}
					}
				}
			}
			printWriter.write('\'');
		} else if (val instanceof Enumerator) {
			printWriter.write(".");
			printWriter.write(val.toString());
			printWriter.write(".");
		} else if (val instanceof byte[]) {
			// TODO printing default leading 0, must be wrong
			printWriter.write("\"0");
			printWriter.write(Hex.encodeHexString((byte[])val));
			printWriter.write("\"");
		} else {
			printWriter.write((val == null ? "$" : val.toString()));
		}
	}
}