package cn.dlb.bim.ifc.serializers;

import java.io.OutputStream;

public interface StreamingReader {

	boolean write(OutputStream out) throws SerializerException;
}