package cn.dlb.bim.ifc.serializers.binarygeometry;

import java.io.IOException;
import java.io.OutputStream;

import cn.dlb.bim.ifc.serializers.SerializerException;
import cn.dlb.bim.ifc.shared.ProgressReporter;

public interface Writer {
	boolean writeMessage(OutputStream outputStream, ProgressReporter progressReporter) throws IOException, SerializerException;
}
