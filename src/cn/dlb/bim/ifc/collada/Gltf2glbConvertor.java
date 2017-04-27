package cn.dlb.bim.ifc.collada;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cn.dlb.bim.ifc.collada.GlbBody.Part;
import cn.dlb.bim.utils.SimpleImageInfo;

public class Gltf2glbConvertor {
	
	public static final String BINARY_EXTENSION = "KHR_binary_glTF";
	public static final String BINARY_BUFFER = "binary_glTF";

	private JsonNodeFactory jsonNodeFactory = new JsonNodeFactory(false);

	public void moveBuffersToBody(ObjectNode scene, GlbBody body) throws Exception {
		// Modify the GLTF data to reference the buffer in the body instead of
		// external references.

		JsonNode buffersNode = scene.get("buffers");
		Iterator<String> fieldIter = buffersNode.fieldNames();
		while (fieldIter.hasNext()) {
			String fieldKey = fieldIter.next();
			JsonNode gltfBufferNode = buffersNode.get(fieldKey);
			JsonNode typeNode = gltfBufferNode.get("type");
			String typeText = typeNode.asText();
			if (typeText != null && !typeText.equals("arraybuffer")) {
				throw new Exception("buffer type " + typeText + " not supported: " + fieldKey);
			}
			JsonNode uriNode = gltfBufferNode.get("uri");
			String uri = uriNode.asText();

			JsonNode byteLengthNode = gltfBufferNode.get("byteLength");
			int byteLength = byteLengthNode.asInt();

			Part part = body.add(uri, byteLength);

			JsonNode extrasNode = gltfBufferNode.get("extras");
			if (extrasNode == null) {
				extrasNode = jsonNodeFactory.objectNode();
				((ObjectNode) gltfBufferNode).set("extras", extrasNode);
			}
			((ObjectNode) extrasNode).put("byteOffset", part.offset);

		}

	}
	
	public ByteBuffer convert(ObjectNode scene, Path containingFolder, boolean embedShaders, boolean embedTextures) throws Exception {
		GlbBody body = new GlbBody(containingFolder);
		
		ArrayNode extensionsUsedNode = scene.withArray("extensionsUsed");
		extensionsUsedNode.add(BINARY_EXTENSION);
		
		moveBuffersToBody(scene, body);
		
		JsonNode bufferViewsNode = scene.get("bufferViews");
		
		Iterator<String> bufferViewsFieldIter = bufferViewsNode.fieldNames();
		
		while (bufferViewsFieldIter.hasNext()) {
			String fieldKey = bufferViewsFieldIter.next();
			JsonNode bufferViewNode = bufferViewsNode.get(fieldKey);
			JsonNode bufferIdNode = bufferViewNode.get("buffer");
			
			String bufferId = bufferIdNode.asText();
			JsonNode buffersNode = scene.get("buffers");
			JsonNode referencedBufferNode = buffersNode.get(bufferId);
			if (referencedBufferNode == null) {
				throw new Exception("buffer ID reference not found: " + bufferId); 
			}
			
			ObjectNode bufferViewObjectNode = ((ObjectNode) bufferViewNode);
			bufferViewObjectNode.put("buffer", BINARY_BUFFER);
			
			JsonNode extrasNode = referencedBufferNode.get("extras");
			JsonNode extrasByteOffsetNode = extrasNode.get("byteOffset");
			int extrasByteOffset = extrasByteOffsetNode.asInt();
			JsonNode bufferViewByteOffsetNode = bufferViewObjectNode.get("byteOffset");
			int bufferViewByteOffset = bufferViewByteOffsetNode.asInt();
			bufferViewObjectNode.put("byteOffset", bufferViewByteOffset + extrasByteOffset);
		}
		
		// Merge shader binaries into the main Binary GLTF file.
		JsonNode shadersNode = scene.get("shaders");
		if (embedShaders && shadersNode != null) {
			Iterator<String> shadersFieldIter = shadersNode.fieldNames();
			while (shadersFieldIter.hasNext()) {
				String fieldKey = shadersFieldIter.next();
				JsonNode shaderNode = shadersNode.get(fieldKey);
				JsonNode uriNode = shaderNode.get("uri");
				String uri = uriNode.asText();
				
				if (uri.startsWith("http://") || uri.startsWith("https://")) {
					continue;
				}
				
				// The "uri" property is ignored by Binary GLTF readers, but technically needs to be there
		        // as extensions to GLTF can't remove existing required properties.
				((ObjectNode) shaderNode).put("uri", "");
				
				Part part = body.add(uri);
				String bufferViewId = "binary_glTF_shader_" + fieldKey;
				
				JsonNode extensionsNode = shaderNode.get("extensions");
				if (extensionsNode == null) {
					extensionsNode = jsonNodeFactory.objectNode();
					((ObjectNode) shaderNode).set("extensions", extensionsNode);
				}
				ObjectNode extensionsNodeValue = jsonNodeFactory.objectNode();
				extensionsNodeValue.put("bufferView", bufferViewId);
				((ObjectNode) extensionsNode).set(BINARY_EXTENSION, extensionsNodeValue);
				
				ObjectNode bufferViewValue = jsonNodeFactory.objectNode();
				bufferViewValue.put("buffer", BINARY_BUFFER);
				bufferViewValue.put("byteLength", part.buffer.capacity());
				bufferViewValue.put("byteOffset", part.offset);
				((ObjectNode) bufferViewsNode).set(bufferViewId, bufferViewValue);
			}
		}
		
		JsonNode texturesNode = scene.get("textures");
		if (embedTextures && texturesNode != null) {
			Iterator<String> texturesFieldIter = texturesNode.fieldNames();
			while (texturesFieldIter.hasNext()) {
				String fieldKey = texturesFieldIter.next();
				JsonNode uriNode = texturesNode.get("uri");
				String uri = uriNode.asText();
				
				if (uri.startsWith("http://") || uri.startsWith("https://")) {
					continue;
				} 
				
				// The "uri" property is ignored by Binary GLTF readers, but technically needs to be there
		        // as extensions to GLTF can't remove existing required properties.
				((ObjectNode) texturesNode).put("uri", "");
				
				Part part = body.add(uri);
				String bufferViewId = "binary_glTF_images_" + fieldKey;
				
				// Get the properties of the image to add as metadata.
				SimpleImageInfo imageInfo = new SimpleImageInfo(part.buffer.array());
				
				JsonNode extensionsNode = texturesNode.get("extensions");
				if (extensionsNode == null) {
					extensionsNode = jsonNodeFactory.objectNode();
					((ObjectNode) texturesNode).set("extensions", extensionsNode);
				}
				
				ObjectNode extensionsNodeValue = jsonNodeFactory.objectNode();
				extensionsNodeValue.put("bufferView", bufferViewId);
				extensionsNodeValue.put("mimeType", imageInfo.getMimeType());
				extensionsNodeValue.put("height", imageInfo.getHeight());
				extensionsNodeValue.put("width", imageInfo.getWidth());
				((ObjectNode) extensionsNode).set(BINARY_EXTENSION, extensionsNodeValue);
				
				ObjectNode bufferViewValue = jsonNodeFactory.objectNode();
				bufferViewValue.put("buffer", BINARY_BUFFER);
				bufferViewValue.put("byteLength", part.buffer.capacity());
				bufferViewValue.put("byteOffset", part.offset);
				((ObjectNode) bufferViewsNode).set(bufferViewId, bufferViewValue);
			}
		}
		
		// All buffer views now reference the "binary_glTF" buffer, so the original buffer objects are
	    // no longer needed.
		ObjectNode emptyNode = jsonNodeFactory.objectNode(); 
		scene.set("buffers", emptyNode);
	    return body.createGlb(scene);
	}
}
