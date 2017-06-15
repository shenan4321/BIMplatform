package cn.dlb.bim.ifc.serializers.binarygeometry;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;

import com.google.common.base.Charsets;
import com.google.common.io.LittleEndianDataOutputStream;

import cn.dlb.bim.ifc.emf.IdEObject;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.serializers.SerializerException;
import cn.dlb.bim.ifc.shared.ProgressReporter;
import cn.dlb.bim.models.geometry.GeometryData;
import cn.dlb.bim.models.geometry.GeometryInfo;

public class BinaryGeometryMessagingSerializer implements MessagingSerializer {
	private static final byte FORMAT_VERSION = 6;
	private IfcModelInterface model;
	private LittleEndianDataOutputStream dataOutputStream;
	
	private enum Mode {
		START,
		DATA,
		END
	}
	
	private enum MessageType {
		INIT((byte)0),
		GEOMETRY_TRIANGLES((byte)1),
		GEOMETRY_TRIANGLES_PARTED((byte)2),
		GEOMETRY_INSTANCE((byte)3),
		GEOMETRY_INSTANCE_PARTED((byte)4);
		
		private byte id;

		private MessageType(byte id) {
			this.id = id;
		}
		
		public byte getId() {
			return id;
		}
	}
	
	private Mode mode = Mode.START;
	private Map<Long, Object> concreteGeometrySent;
	private Iterator<IdEObject> iterator;
	private PackageMetaData packageMetaData;
	private long splitCounter = -1;

	@Override
	public void init(IfcModelInterface model, PackageMetaData packageMetaData) throws SerializerException {
		this.model = model;
		this.packageMetaData = packageMetaData;
	}

	@Override
	public boolean writeMessage(OutputStream outputStream, ProgressReporter progressReporter) throws IOException {
		dataOutputStream = new LittleEndianDataOutputStream(outputStream);
		switch (mode) {
		case START:
			if (!writeStart()) {
				mode = Mode.END;
				return false;
			}
			mode = Mode.DATA;
			break;
		case DATA:
			if (!writeData()) {
				mode = Mode.END;
				return false;
			}
			break;
		case END:
			return false;
		default:
			break;
		}
		return true;
	}
	
	private boolean writeStart() throws IOException {
		// Identifier for clients to determine if this server is even serving binary geometry
		dataOutputStream.writeByte(MessageType.INIT.getId());
		dataOutputStream.writeUTF("BGS");
		
		// Version of the current format being outputted, should be changed for every (released) change in protocol 
		dataOutputStream.writeByte(FORMAT_VERSION);
		
		Bounds modelBounds = new Bounds();
		int nrObjects = 0;
		
		// All access to EClass is being done generically to support multiple IFC schema's with 1 serializer
		EClass productClass = model.getPackageMetaData().getEClass("IfcProduct");
		
		List<IdEObject> products = model.getAllWithSubTypes(productClass);
		List<IdEObject> output = new ArrayList<>();
		for (IdEObject ifcProduct : products) {
			GeometryInfo geometryInfo = (GeometryInfo) ifcProduct.eGet(ifcProduct.eClass().getEStructuralFeature("geometry"));
			if (geometryInfo != null && geometryInfo.getTransformation() != null && !packageMetaData.getEClass("IfcSpace").isSuperTypeOf(ifcProduct.eClass()) 
					&& !packageMetaData.getEClass("IfcFeatureElementSubtraction").isSuperTypeOf(ifcProduct.eClass())) {
				output.add(ifcProduct);
			}
		}
		
		// First iteration, to determine number of objects with geometry and calculate model bounds
		for (IdEObject ifcProduct : output) {
			GeometryInfo geometryInfo = (GeometryInfo) ifcProduct.eGet(ifcProduct.eClass().getEStructuralFeature("geometry"));
			Bounds objectBounds = new Bounds(
					new Double3(
						geometryInfo.getMinBounds().getX(), 
						geometryInfo.getMinBounds().getY(), 
						geometryInfo.getMinBounds().getZ()), 
					new Double3(
						geometryInfo.getMaxBounds().getX(), 
						geometryInfo.getMaxBounds().getY(), 
						geometryInfo.getMaxBounds().getZ()));
			modelBounds.integrate(objectBounds);
			nrObjects++;
		}
		
		int skip = 4 - (7 % 4);
		if(skip != 0 && skip != 4) {
			dataOutputStream.write(new byte[skip]);
		}
		
		modelBounds.writeTo(dataOutputStream);
		dataOutputStream.writeInt(nrObjects);
		
		concreteGeometrySent = new HashMap<Long, Object>();
		iterator = output.iterator();
		
		return nrObjects > 0;
	}
	
	@SuppressWarnings("unchecked")
	private boolean writeData() throws IOException {
		IdEObject ifcProduct = iterator.next();
		GeometryInfo geometryInfo = (GeometryInfo) ifcProduct.eGet(ifcProduct.eClass().getEStructuralFeature("geometry"));
		if (geometryInfo != null && geometryInfo.getTransformation() != null) {
			GeometryData geometryData = geometryInfo.getData();
			
			int totalNrIndices = geometryData.getIndices().length / 4;
			int maxIndexValues = 16389;
			
			Object reuse = concreteGeometrySent.get(geometryData.getOid());
			MessageType messageType = null;
			if (reuse == null) {
				if (totalNrIndices > maxIndexValues) {
					messageType = MessageType.GEOMETRY_TRIANGLES_PARTED;
				} else {
					messageType = MessageType.GEOMETRY_TRIANGLES;
				}
			} else {
				if (reuse instanceof List) {
					messageType = MessageType.GEOMETRY_INSTANCE_PARTED;
				} else {
					messageType = MessageType.GEOMETRY_INSTANCE;
				}
			}
			
			dataOutputStream.writeByte(messageType.getId());
//			dataOutputStream.writeUTF(ifcProduct.eClass().getName());
			
//			int rid = model.getModelMetaData().getRevisionId();
//			dataOutputStream.writeInt(rid);
			dataOutputStream.writeLong(geometryInfo.getOid());
			
			// BEWARE, ByteOrder is always LITTLE_ENDIAN, because that's what GPU's seem to prefer, Java's ByteBuffer default is BIG_ENDIAN though!
			
			int skip = 8 - 1;//writeUTF 前两位是用于表示字符串长度
			if(skip != 0 && skip != 8) {
				dataOutputStream.write(new byte[skip]);
			}
			
			dataOutputStream.write(geometryInfo.getTransformation());
			
			if (reuse != null && reuse instanceof Long) {
				// Reused geometry, only send the id of the reused geometry data
				dataOutputStream.writeLong(geometryData.getOid());
			} else if (reuse != null && reuse instanceof List) {
				List<Long> list = (List<Long>)reuse;
				dataOutputStream.writeInt(list.size());
				for (long coreId : list) {
					dataOutputStream.writeLong(coreId);
				}
			} else {
				if (totalNrIndices > maxIndexValues) {
					// Split geometry, this algorithm - for now - just throws away all the reuse of vertices that might be there
					// Also, although usually the vertices buffers are too large, this algorithm is based on the indices, so we
					// probably are not cramming as much data as we can in each "part", but that's not really a problem I think
	
					int nrParts = (totalNrIndices + maxIndexValues - 1) / maxIndexValues;
					dataOutputStream.writeInt(nrParts);
					
					int skipIndices = 4 % 8;
					if(skipIndices != 0 && skipIndices != 8) {
						dataOutputStream.write(new byte[skipIndices]);
					}
	
//					Bounds objectBounds = new Bounds(geometryInfo.getMinBounds(), geometryInfo.getMaxBounds());
//					objectBounds.writeTo(dataOutputStream);
	
					ByteBuffer indicesBuffer = ByteBuffer.wrap(geometryData.getIndices());
					indicesBuffer.order(ByteOrder.LITTLE_ENDIAN);
					IntBuffer indicesIntBuffer = indicesBuffer.asIntBuffer();
					
	//					ByteBuffer indicesForLinesWireFrameBuffer = ByteBuffer.wrap(geometryData.getIndicesForLinesWireFrame());
	//					indicesForLinesWireFrameBuffer.order(ByteOrder.LITTLE_ENDIAN);
	//					IntBuffer indicesForLinesWireFrameIntBuffer = indicesForLinesWireFrameBuffer.asIntBuffer();
	
					ByteBuffer vertexBuffer = ByteBuffer.wrap(geometryData.getVertices());
					vertexBuffer.order(ByteOrder.LITTLE_ENDIAN);
					FloatBuffer verticesFloatBuffer = vertexBuffer.asFloatBuffer();
					
					ByteBuffer normalsBuffer = ByteBuffer.wrap(geometryData.getNormals());
					normalsBuffer.order(ByteOrder.LITTLE_ENDIAN);
					FloatBuffer normalsFloatBuffer = normalsBuffer.asFloatBuffer();
					
					List<Long> arrayList = new ArrayList<Long>();
					
					for (int part=0; part<nrParts; part++) {
						long splitId = splitCounter--;
						arrayList.add(splitId);
						dataOutputStream.writeLong(splitId);
						
						int indexCounter = 0;
						int upto = Math.min((part + 1) * maxIndexValues, totalNrIndices);
						dataOutputStream.writeInt(upto - part * maxIndexValues);
						for (int i=part * maxIndexValues; i<upto; i++) {
							dataOutputStream.writeShort(indexCounter++);
						}
						
						// Aligning to 4-bytes
						if ((upto - part * maxIndexValues) % 2 != 0) {
							dataOutputStream.writeShort((short)0);
						}
						
						dataOutputStream.writeInt((upto - part * maxIndexValues) * 3);
						for (int i=part * maxIndexValues; i<upto; i+=3) {
							int oldIndex1 = indicesIntBuffer.get(i);
							int oldIndex2 = indicesIntBuffer.get(i+1);
							int oldIndex3 = indicesIntBuffer.get(i+2);
							dataOutputStream.writeFloat(verticesFloatBuffer.get(oldIndex1 * 3));
							dataOutputStream.writeFloat(verticesFloatBuffer.get(oldIndex1 * 3 + 1));
							dataOutputStream.writeFloat(verticesFloatBuffer.get(oldIndex1 * 3 + 2));
							dataOutputStream.writeFloat(verticesFloatBuffer.get(oldIndex2 * 3));
							dataOutputStream.writeFloat(verticesFloatBuffer.get(oldIndex2 * 3 + 1));
							dataOutputStream.writeFloat(verticesFloatBuffer.get(oldIndex2 * 3 + 2));
							dataOutputStream.writeFloat(verticesFloatBuffer.get(oldIndex3 * 3));
							dataOutputStream.writeFloat(verticesFloatBuffer.get(oldIndex3 * 3 + 1));
							dataOutputStream.writeFloat(verticesFloatBuffer.get(oldIndex3 * 3 + 2));
						}
						dataOutputStream.writeInt((upto - part * maxIndexValues) * 3);
						for (int i=part * maxIndexValues; i<upto; i+=3) {
							int oldIndex1 = indicesIntBuffer.get(i);
							int oldIndex2 = indicesIntBuffer.get(i+1);
							int oldIndex3 = indicesIntBuffer.get(i+2);
							
							dataOutputStream.writeFloat(normalsFloatBuffer.get(oldIndex1 * 3));
							dataOutputStream.writeFloat(normalsFloatBuffer.get(oldIndex1 * 3 + 1));
							dataOutputStream.writeFloat(normalsFloatBuffer.get(oldIndex1 * 3 + 2));
							dataOutputStream.writeFloat(normalsFloatBuffer.get(oldIndex2 * 3));
							dataOutputStream.writeFloat(normalsFloatBuffer.get(oldIndex2 * 3 + 1));
							dataOutputStream.writeFloat(normalsFloatBuffer.get(oldIndex2 * 3 + 2));
							dataOutputStream.writeFloat(normalsFloatBuffer.get(oldIndex3 * 3));
							dataOutputStream.writeFloat(normalsFloatBuffer.get(oldIndex3 * 3 + 1));
							dataOutputStream.writeFloat(normalsFloatBuffer.get(oldIndex3 * 3 + 2));
						}
						dataOutputStream.writeInt(0);
					}
					concreteGeometrySent.put(geometryData.getOid(), arrayList);
				} else {
//					Bounds objectBounds = new Bounds(geometryInfo.getMinBounds(), geometryInfo.getMaxBounds());
//					objectBounds.writeTo(dataOutputStream);
					
					dataOutputStream.writeLong(geometryData.getOid());
					
					ByteBuffer indicesBuffer = ByteBuffer.wrap(geometryData.getIndices());
					indicesBuffer.order(ByteOrder.LITTLE_ENDIAN);
					dataOutputStream.writeInt(indicesBuffer.capacity() / 4);
					IntBuffer intBuffer = indicesBuffer.asIntBuffer();
					for (int i=0; i<intBuffer.capacity(); i++) {
						dataOutputStream.writeShort((short)intBuffer.get());
					}
					// Aligning to 4-bytes
					if (intBuffer.capacity() % 2 != 0) {
						dataOutputStream.writeShort((short)0);
					}
					
					ByteBuffer indicesForLinesWireFrameBuffer = ByteBuffer.wrap(geometryData.getIndicesForLinesWireFrame());
					indicesForLinesWireFrameBuffer.order(ByteOrder.LITTLE_ENDIAN);
					dataOutputStream.writeInt(indicesForLinesWireFrameBuffer.capacity() / 4);
					IntBuffer indicesForLinesWireFrameIntBuffer = indicesForLinesWireFrameBuffer.asIntBuffer();
					for (int i=0; i<indicesForLinesWireFrameIntBuffer.capacity(); i++) {
						dataOutputStream.writeShort((short)indicesForLinesWireFrameIntBuffer.get());
					}
					// Aligning to 4-bytes
					if (indicesForLinesWireFrameIntBuffer.capacity() % 2 != 0) {
						dataOutputStream.writeShort((short)0);
					}
					
					ByteBuffer vertexByteBuffer = ByteBuffer.wrap(geometryData.getVertices());
					dataOutputStream.writeInt(vertexByteBuffer.capacity() / 4);
					dataOutputStream.write(vertexByteBuffer.array());
					
					ByteBuffer normalsBuffer = ByteBuffer.wrap(geometryData.getNormals());
					dataOutputStream.writeInt(normalsBuffer.capacity() / 4);
					dataOutputStream.write(normalsBuffer.array());
					
					// Only when materials are used we send them
					if (geometryData.getMaterials() != null) {
						ByteBuffer materialsByteBuffer = ByteBuffer.wrap(geometryData.getMaterials());
						
						dataOutputStream.writeInt(materialsByteBuffer.capacity() / 4);
						dataOutputStream.write(materialsByteBuffer.array());
					} else {
						// No materials used
						dataOutputStream.writeInt(0);
					}

					concreteGeometrySent.put(geometryData.getOid(), geometryData.getOid());
				}
			}
		}
		return iterator.hasNext();
	}
}