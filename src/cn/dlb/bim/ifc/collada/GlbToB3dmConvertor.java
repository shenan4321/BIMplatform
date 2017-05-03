package cn.dlb.bim.ifc.collada;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cn.dlb.bim.utils.Unsigned;

public class GlbToB3dmConvertor {
	private ByteBuffer glbToB3dm(ByteBuffer glbBuffer, ObjectNode featureTableJson) {
		int headerByteLength = 28;
		ByteBuffer featureTableJsonBuffer = getJsonBufferPadded(featureTableJson, headerByteLength);
		
	    int byteLength = headerByteLength + featureTableJsonBuffer.capacity() + glbBuffer.capacity();
	    ByteBuffer header = ByteBuffer.allocate(headerByteLength);
	    // magic
	    header.put("b3dm".getBytes());
	    // version
	    Unsigned.putUnsignedIntLE(header, 4, 1);
	    // byteLength - length of entire tile, including header, in bytes
	    Unsigned.putUnsignedIntLE(header, 8, byteLength);
	    // featureTableJSONByteLength - length of feature table JSON section in bytes.
	    Unsigned.putUnsignedIntLE(header, 12, featureTableJsonBuffer.capacity());
	    // featureTableBinaryByteLength - length of feature table binary section in bytes.
	    Unsigned.putUnsignedIntLE(header, 16, 0);
	    // batchTableJSONByteLength - length of batch table JSON section in bytes. (0 for basic, no batches)
	    Unsigned.putUnsignedIntLE(header, 20, 0);
	    // batchTableBinaryByteLength - length of batch table binary section in bytes. (0 for basic, no batches)
	    Unsigned.putUnsignedIntLE(header, 24, 0);
	    
	    header.position(0);
	    header.limit(header.capacity());
	    glbBuffer.flip();
	    featureTableJsonBuffer.position(0);
	    featureTableJsonBuffer.limit(featureTableJsonBuffer.capacity());
	    ByteBuffer result = ByteBuffer.allocate(byteLength);
	    result.put(header);
	    result.put(featureTableJsonBuffer);
	    result.put(glbBuffer);
	    return result;
	}
	
	private ByteBuffer getJsonBufferPadded(ObjectNode jsonValue, int byteOffset) {
		if (jsonValue == null || jsonValue.size() == 0) {
			return ByteBuffer.allocate(0);
		}
		String jsonStr = jsonValue.toString();
		
		int boundary = 8;
		int byteLength = jsonStr.getBytes().length;
		int remainder = (byteOffset + byteLength) % boundary;
		int padding = (remainder == 0) ? 0 : boundary - remainder;
		
		ByteBuffer result = ByteBuffer.allocate(byteLength + padding);
		result.put(jsonStr.getBytes());
		for (int i = 0; i < padding; ++i) result.put((byte) 0x20);
//		String s = new String(result.array());
		return result;
	}
	
	@SuppressWarnings("resource")
	public ByteBuffer getBufferFromUri(String uri, Path containingFolder) throws IOException {
		// 2.0以后只在外部存储bin文件
		Path binPath = containingFolder.resolve(uri);
		FileInputStream fis = new FileInputStream(binPath.toFile());
		FileChannel channel = fis.getChannel();
		ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
		channel.read(byteBuffer);
		return byteBuffer;
	}
	
	public static void main(String[] args) {
		GlbToB3dmConvertor convertor = new GlbToB3dmConvertor();
		JsonNodeFactory factory = new JsonNodeFactory(false);
		try {
			ObjectNode featureTableJsonNode = factory.objectNode();
			featureTableJsonNode.put("BATCH_LENGTH", 0);
			ByteBuffer glbBuffer = convertor.getBufferFromUri("", new File("D:\\tttt.glb").toPath());
			ByteBuffer b3dmBuffer = convertor.glbToB3dm(glbBuffer, featureTableJsonNode);
			OutputStream os = new FileOutputStream(new File("D:\\tttt.b3dm"));
			os.write(b3dmBuffer.array());
			os.flush();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
