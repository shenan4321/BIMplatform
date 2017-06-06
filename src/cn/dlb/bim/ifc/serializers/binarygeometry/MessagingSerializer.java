package cn.dlb.bim.ifc.serializers.binarygeometry;

import java.io.IOException;
import java.io.OutputStream;

import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.serializers.SerializerException;
import cn.dlb.bim.ifc.shared.ProgressReporter;

public interface MessagingSerializer extends Writer {
	void init(IfcModelInterface model, PackageMetaData packageMetaData) throws SerializerException;
	/**
	 * @param streamingSocketInterface This is where you write your messages to
	 * @param progressReporter Report any available progress to the progressReporter
	 * @return true if there are more messages, false if there are no more
	 * @throws IOException
	 */
	boolean writeMessage(OutputStream outputStream, ProgressReporter progressReporter) throws IOException;
}