package cn.dlb.bim.ifc.collada;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cn.dlb.bim.utils.Unsigned;

public class GlbBody {

	public class Part {
		public int offset;
		public ByteBuffer buffer;
	}

	private int length = 0;
	private List<Part> parts;
	private Path containingFolder;

	public GlbBody(Path containingFolder) {
		this.containingFolder = containingFolder;
		parts = new ArrayList<>();
	}

	public Part add(String uri, int len) throws IOException {
		ByteBuffer buffer = getBufferFromUri(uri, containingFolder);
		len = Math.min(len, buffer.capacity());
		buffer.position(0);
		buffer.limit(len);
		buffer = buffer.slice();

		int offset = length;
		this.length += len;

		Part part = new Part();
		part.offset = offset;
		part.buffer = buffer;
		parts.add(part);
		return part;
	}

	public Part add(String uri) throws IOException {
		ByteBuffer buffer = getBufferFromUri(uri, containingFolder);
		int len = buffer.capacity();
		return add(uri, len);
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

	public ByteBuffer createGlb(ObjectNode scene) {
		JsonNodeFactory factory = new JsonNodeFactory(false);
		JsonNode buffersNode = scene.get("buffers");
		if (buffersNode == null) {
			buffersNode = factory.objectNode();
			scene.set("buffers", buffersNode);
		}

		final int bodyLength = this.length;

		ObjectNode binaryGlTFNode = factory.objectNode();
		binaryGlTFNode.put("byteLength", bodyLength);
		binaryGlTFNode.put("uri", "");
		((ObjectNode) buffersNode).set(Gltf2glbConvertor.BINARY_BUFFER, binaryGlTFNode);

		String newSceneStr = scene.toString();

		int contentLength = newSceneStr.getBytes().length;
		// As body is 4-byte aligned, the scene length must be padded to have a
		// multiple of 4.
		int paddedContentLength = padTo4Bytes(contentLength);

		// Header is 20 bytes long.
		int bodyOffset = paddedContentLength + 20;
		int fileLength = bodyOffset + bodyLength;

		// Let's create our GLB file!
		ByteBuffer glbFile = ByteBuffer.allocate(fileLength);
		
		// Magic number (the ASCII string 'glTF').
		Unsigned.putUnsignedInt(glbFile, 0, 0x676C5446);
		
		// Binary GLTF is little endian.
		// Version of the Binary glTF container format as a uint32 (version 1).
		Unsigned.putUnsignedIntLE(glbFile, 4, 1);

		// Total length of the generated file in bytes (uint32).
		Unsigned.putUnsignedIntLE(glbFile, 8, fileLength);

		// Total length of the scene in bytes (uint32).
		Unsigned.putUnsignedIntLE(glbFile, 12, paddedContentLength);

		// Scene format as a uint32 (JSON is 0).
		Unsigned.putUnsignedIntLE(glbFile, 16, 0);

		// Write the scene.
		glbFile.position(20);
		glbFile.put(newSceneStr.getBytes(), 0, contentLength);
		
		// Add spaces as padding to ensure scene is a multiple of 4 bytes.
	    for (int i = contentLength + 20; i < bodyOffset; ++i) glbFile.put(i, (byte) 0x20);
		
		// Write the body.
	    for (Part part : parts) {
	    	glbFile.position(part.offset + bodyOffset);
	    	glbFile.put(part.buffer);
	    }
	    glbFile.flip();
		return glbFile;
	}

	private int padTo4Bytes(int x) {
		return (x + 3) & ~3;
	}
	
}
