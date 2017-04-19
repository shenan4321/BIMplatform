package cn.dlb.bim.ifc.collada;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class GlbBody {

	public class Part {
		public int offset;
		public ByteBuffer buffer;
	}

	private int length = 0;
	private Map<Integer, Buffer> parts;
	private Path containingFolder;

	public GlbBody(Path containingFolder) {
		this.containingFolder = containingFolder;
		parts = new HashMap<>();
	}

	public Part add(String uri, int len) throws IOException {
		ByteBuffer buffer = getBufferFromUri(uri, containingFolder);
		len = Math.min(len, buffer.capacity());
		buffer.position(0);
		buffer.limit(len);
		buffer = buffer.slice();

		int offset = length;
		parts.put(offset, buffer);
		this.length += len;

		Part part = new Part();
		part.offset = offset;
		part.buffer = buffer;

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
		Path binPath = containingFolder;
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

		String newSceneStr = scene.asText();

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
		glbFile.putInt(0, 0x676C5446);

		// Binary GLTF is little endian.
		// Version of the Binary glTF container format as a uint32 (version 1).
		glbFile.putInt(4, 1);

		// Total length of the generated file in bytes (uint32).
		glbFile.putInt(8, fileLength);

		// Total length of the scene in bytes (uint32).
		glbFile.putInt(12, paddedContentLength);

		// Scene format as a uint32 (JSON is 0).
		glbFile.putInt(16, 0);

		// Write the scene.
		glbFile.put(newSceneStr.getBytes(), 20, contentLength);

		return glbFile;
	}

	private int padTo4Bytes(int x) {
		return (x + 3) & ~3;
	}
}
