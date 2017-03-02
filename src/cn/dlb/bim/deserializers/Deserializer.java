package cn.dlb.bim.deserializers;

import java.io.InputStream;
import java.nio.file.Path;

import cn.dlb.bim.emf.IfcModelInterface;
import cn.dlb.bim.emf.PackageMetaData;

public interface Deserializer {
	void init(PackageMetaData packageMetaData);
	IfcModelInterface read(Path file, ByteProgressReporter progressReporter) throws DeserializeException;
	IfcModelInterface read(InputStream inputStream, String fileName, long fileSize, ByteProgressReporter progressReporter) throws DeserializeException;
	IfcModelInterface read(Path file) throws DeserializeException;
	IfcModelInterface read(InputStream inputStream, String fileName, long fileSize) throws DeserializeException;
}