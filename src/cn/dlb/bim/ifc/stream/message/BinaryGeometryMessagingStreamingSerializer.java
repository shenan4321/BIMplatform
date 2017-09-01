package cn.dlb.bim.ifc.stream.message;

/******************************************************************************
 * Copyright (C) 2009-2016  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.LittleEndianDataOutputStream;

import cn.dlb.bim.dao.entity.ConcreteRevision;
import cn.dlb.bim.ifc.database.DatabaseException;
import cn.dlb.bim.ifc.database.queries.om.QueryException;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.serializers.SerializerException;
import cn.dlb.bim.ifc.shared.ProgressReporter;
import cn.dlb.bim.ifc.stream.VirtualObject;
import cn.dlb.bim.ifc.stream.WrappedVirtualObject;
import cn.dlb.bim.ifc.stream.serializers.ObjectProvider;
import cn.dlb.bim.models.geometry.GeometryPackage;
import cn.dlb.bim.vo.Vector3f;

public class BinaryGeometryMessagingStreamingSerializer implements MessagingStreamingSerializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(BinaryGeometryMessagingStreamingSerializer.class);

	/*
	 * Format history (starting at version 8):
	 * 
	 * Version 8: - Using short instead of int for indices. SceneJS was converting
	 * the indices to Uint16 anyways, so this saves bytes and a conversion on the
	 * client-side Version 9: - Sending the materials/colors for splitted geometry
	 * as well, before sending the actual parts - Aligning bytes to 8s instead of 4s
	 * when sending splitted geometry - Incrementing splitcounter instead of
	 * decrementing (no idea why it was doing that) Version 10: - Sending the
	 * materials/colors for parts as well again
	 */

	private static final byte FORMAT_VERSION = 10;
	private boolean splitGeometry = true;

	private enum Mode {
		LOAD, START, DATA, END
	}

	private enum MessageType {
		INIT((byte) 0), GEOMETRY_TRIANGLES_PARTED((byte) 3), GEOMETRY_TRIANGLES((byte) 1), GEOMETRY_INFO((byte) 5), END(
				(byte) 6);

		private byte id;

		private MessageType(byte id) {
			this.id = id;
		}

		public byte getId() {
			return id;
		}
	}

	private Mode mode = Mode.LOAD;
	private long splitCounter = 0;
	private ObjectProvider objectProvider;
	private ConcreteRevision concreteRevision;
	private LittleEndianDataOutputStream dataOutputStream;
	private VirtualObject next;
	private ProgressReporter progressReporter;
	private int nrObjectsWritten;
	private int size;

	@Override
	public void init(ObjectProvider objectProvider, PackageMetaData packageMetaData, ConcreteRevision concreteRevision) throws SerializerException {
		this.objectProvider = objectProvider;
		this.concreteRevision = concreteRevision;
	}

	@Override
	public boolean writeMessage(OutputStream outputStream, ProgressReporter progressReporter)
			throws IOException, SerializerException {
		this.progressReporter = progressReporter;
		dataOutputStream = new LittleEndianDataOutputStream(outputStream);
		switch (mode) {
		case LOAD: {
			load();
			mode = Mode.START;
			// Explicitly no break here, move on to start right away
		}
		case START:
			writeStart();
			mode = Mode.DATA;
			break;
		case DATA:
			if (!writeData()) {
				mode = Mode.END;
				return true;
			}
			break;
		case END:
			writeEnd();
			return false;
		default:
			break;
		}
		return true;
	}

	public void setSplitGeometry(boolean splitGeometry) {
		this.splitGeometry = splitGeometry;
	}

	private void load() throws SerializerException {
		// long start = System.nanoTime();
		size = 0;
		VirtualObject next = null;
		try {
			next = objectProvider.next();
			while (next != null) {
				if (next.eClass() == GeometryPackage.eINSTANCE.getGeometryInfo()) {
					size++;
				}
				next = objectProvider.next();
			}
		} catch (DatabaseException e) {
			throw new SerializerException(e);
		}
		try {
			objectProvider = objectProvider.copy();
			this.next = objectProvider.next();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (QueryException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		// long end = System.nanoTime();
		// System.out.println(((end - start) / 1000000) + " ms prepare time");
	}

	private boolean writeEnd() throws IOException {
		dataOutputStream.write(MessageType.END.getId());
		return true;
	}

	private void writeStart() throws IOException {
		// Identifier for clients to determine if this server is even serving binary
		// geometry
		dataOutputStream.writeByte(MessageType.INIT.getId());
		dataOutputStream.writeUTF("BGS");

		// Version of the current format being outputted, should be changed for every
		// (released) change in protocol
		dataOutputStream.writeByte(FORMAT_VERSION);

		int skip = 4 - (7 % 4);
		if (skip != 0 && skip != 4) {
			dataOutputStream.write(new byte[skip]);
		}

		Vector3f minBounds = concreteRevision.getMinBounds();
		dataOutputStream.writeDouble(minBounds.getX());
		dataOutputStream.writeDouble(minBounds.getY());
		dataOutputStream.writeDouble(minBounds.getZ());
		Vector3f maxBounds = concreteRevision.getMaxBounds();
		dataOutputStream.writeDouble(maxBounds.getX());
		dataOutputStream.writeDouble(maxBounds.getY());
		dataOutputStream.writeDouble(maxBounds.getZ());
	}

	private boolean writeData() throws IOException, SerializerException {
		if (next == null) {
			return false;
		}
		if (GeometryPackage.eINSTANCE.getGeometryInfo() == next.eClass()) {
			VirtualObject info = next;
			Object transformation = info.eGet(info.eClass().getEStructuralFeature("transformation"));
			Object dataOid = info.eGet(info.eClass().getEStructuralFeature("data"));

			dataOutputStream.writeByte(MessageType.GEOMETRY_INFO.getId());
			dataOutputStream.write(new byte[7]);
			dataOutputStream.writeLong(info.getRid());
			dataOutputStream.writeLong(info.getOid());
			WrappedVirtualObject minBounds = (WrappedVirtualObject) info
					.eGet(info.eClass().getEStructuralFeature("minBounds"));
			WrappedVirtualObject maxBounds = (WrappedVirtualObject) info
					.eGet(info.eClass().getEStructuralFeature("maxBounds"));
			Double minX = (Double) minBounds.eGet("x");
			Double minY = (Double) minBounds.eGet("y");
			Double minZ = (Double) minBounds.eGet("z");
			Double maxX = (Double) maxBounds.eGet("x");
			Double maxY = (Double) maxBounds.eGet("y");
			Double maxZ = (Double) maxBounds.eGet("z");

			dataOutputStream.writeDouble(minX);
			dataOutputStream.writeDouble(minY);
			dataOutputStream.writeDouble(minZ);
			dataOutputStream.writeDouble(maxX);
			dataOutputStream.writeDouble(maxY);
			dataOutputStream.writeDouble(maxZ);
			dataOutputStream.write((byte[]) transformation);
			dataOutputStream.writeLong((Long) dataOid);

			nrObjectsWritten++;
			if (progressReporter != null) {
				progressReporter.update(nrObjectsWritten, size);
			}
		} else if (GeometryPackage.eINSTANCE.getGeometryData() == next.eClass()) {
			VirtualObject data = next;
			// This geometry info is pointing to a not-yet-sent geometry data, so we send
			// that first
			// This way the client can be sure that geometry data is always available when
			// geometry info is received, simplifying bookkeeping
			EStructuralFeature indicesFeature = data.eClass().getEStructuralFeature("indices");
			EStructuralFeature verticesFeature = data.eClass().getEStructuralFeature("vertices");
			EStructuralFeature normalsFeature = data.eClass().getEStructuralFeature("normals");
			EStructuralFeature materialsFeature = data.eClass().getEStructuralFeature("materials");

			byte[] indices = (byte[]) data.eGet(indicesFeature);
			byte[] vertices = (byte[]) data.eGet(verticesFeature);
			byte[] normals = (byte[]) data.eGet(normalsFeature);
			byte[] materials = (byte[]) data.eGet(materialsFeature);

			int totalNrIndices = indices.length / 4;
			int maxIndexValues = 16389;

			if (totalNrIndices > maxIndexValues) {
				dataOutputStream.write(MessageType.GEOMETRY_TRIANGLES_PARTED.getId());
				dataOutputStream.write(new byte[7]);
				dataOutputStream.writeLong(data.getOid());

				// Split geometry, this algorithm - for now - just throws away all the reuse of
				// vertices that might be there
				// Also, although usually the vertices buffers are too large, this algorithm is
				// based on the indices, so we
				// probably are not cramming as much data as we can in each "part", but that's
				// not really a problem I think

				int nrParts = (totalNrIndices + maxIndexValues - 1) / maxIndexValues;
				dataOutputStream.writeInt(nrParts);

				ByteBuffer indicesBuffer = ByteBuffer.wrap(indices);
				indicesBuffer.order(ByteOrder.LITTLE_ENDIAN);
				IntBuffer indicesIntBuffer = indicesBuffer.asIntBuffer();

				ByteBuffer vertexBuffer = ByteBuffer.wrap(vertices);
				vertexBuffer.order(ByteOrder.LITTLE_ENDIAN);
				FloatBuffer verticesFloatBuffer = vertexBuffer.asFloatBuffer();

				ByteBuffer normalsBuffer = ByteBuffer.wrap(normals);
				normalsBuffer.order(ByteOrder.LITTLE_ENDIAN);
				FloatBuffer normalsFloatBuffer = normalsBuffer.asFloatBuffer();

				for (int part = 0; part < nrParts; part++) {
					long splitId = splitCounter++;
					dataOutputStream.writeLong(splitId);

					short indexCounter = 0;
					int upto = Math.min((part + 1) * maxIndexValues, totalNrIndices);
					dataOutputStream.writeInt(upto - part * maxIndexValues);
					for (int i = part * maxIndexValues; i < upto; i++) {
						dataOutputStream.writeShort(indexCounter++);
					}

					// Aligning to 4-bytes
					if ((upto - part * maxIndexValues) % 2 != 0) {
						dataOutputStream.writeShort((short) 0);
					}

					int nrVertices = (upto - part * maxIndexValues) * 3;
					dataOutputStream.writeInt(nrVertices);
					for (int i = part * maxIndexValues; i < upto; i += 3) {
						int oldIndex1 = indicesIntBuffer.get(i);
						int oldIndex2 = indicesIntBuffer.get(i + 1);
						int oldIndex3 = indicesIntBuffer.get(i + 2);

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
					dataOutputStream.writeInt(nrVertices);
					for (int i = part * maxIndexValues; i < upto; i += 3) {
						int oldIndex1 = indicesIntBuffer.get(i);
						int oldIndex2 = indicesIntBuffer.get(i + 1);
						int oldIndex3 = indicesIntBuffer.get(i + 2);

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
					// Only when materials are used we send them
					if (materials != null) {
						ByteBuffer materialsByteBuffer = ByteBuffer.wrap(materials);
						materialsByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
						FloatBuffer materialsFloatBuffer = materialsByteBuffer.asFloatBuffer();

						dataOutputStream.writeInt(nrVertices * 4 / 3);
						for (int i = part * maxIndexValues; i < upto; i += 3) {
							int oldIndex1 = indicesIntBuffer.get(i);
							int oldIndex2 = indicesIntBuffer.get(i + 1);
							int oldIndex3 = indicesIntBuffer.get(i + 2);

							dataOutputStream.writeFloat(materialsFloatBuffer.get(oldIndex1 * 4));
							dataOutputStream.writeFloat(materialsFloatBuffer.get(oldIndex1 * 4 + 1));
							dataOutputStream.writeFloat(materialsFloatBuffer.get(oldIndex1 * 4 + 2));
							dataOutputStream.writeFloat(materialsFloatBuffer.get(oldIndex1 * 4 + 3));
							dataOutputStream.writeFloat(materialsFloatBuffer.get(oldIndex2 * 4));
							dataOutputStream.writeFloat(materialsFloatBuffer.get(oldIndex2 * 4 + 1));
							dataOutputStream.writeFloat(materialsFloatBuffer.get(oldIndex2 * 4 + 2));
							dataOutputStream.writeFloat(materialsFloatBuffer.get(oldIndex2 * 4 + 3));
							dataOutputStream.writeFloat(materialsFloatBuffer.get(oldIndex3 * 4));
							dataOutputStream.writeFloat(materialsFloatBuffer.get(oldIndex3 * 4 + 1));
							dataOutputStream.writeFloat(materialsFloatBuffer.get(oldIndex3 * 4 + 2));
							dataOutputStream.writeFloat(materialsFloatBuffer.get(oldIndex3 * 4 + 3));
						}
					} else {
						// No materials used
						dataOutputStream.writeInt(0);
					}
				}
			} else {
				dataOutputStream.write(MessageType.GEOMETRY_TRIANGLES.getId());
				dataOutputStream.write(new byte[7]);
				dataOutputStream.writeLong(data.getOid());

				ByteBuffer indicesBuffer = ByteBuffer.wrap(indices);
				indicesBuffer.order(ByteOrder.LITTLE_ENDIAN);
				dataOutputStream.writeInt(indicesBuffer.capacity() / 4);
				IntBuffer intBuffer = indicesBuffer.asIntBuffer();
				for (int i = 0; i < intBuffer.capacity(); i++) {
					dataOutputStream.writeShort((short) intBuffer.get());
				}

				// Aligning to 4-bytes
				if (intBuffer.capacity() % 2 != 0) {
					dataOutputStream.writeShort((short) 0);
				}

				ByteBuffer vertexByteBuffer = ByteBuffer.wrap(vertices);
				dataOutputStream.writeInt(vertexByteBuffer.capacity() / 4);
				dataOutputStream.write(vertexByteBuffer.array());

				ByteBuffer normalsBuffer = ByteBuffer.wrap(normals);
				dataOutputStream.writeInt(normalsBuffer.capacity() / 4);
				dataOutputStream.write(normalsBuffer.array());

				// Only when materials are used we send them
				if (materials != null) {
					ByteBuffer materialsByteBuffer = ByteBuffer.wrap(materials);

					dataOutputStream.writeInt(materialsByteBuffer.capacity() / 4);
					dataOutputStream.write(materialsByteBuffer.array());
				} else {
					// No materials used
					dataOutputStream.writeInt(0);
				}
			}
		}
		try {
			next = objectProvider.next();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		return next != null;
	}
}