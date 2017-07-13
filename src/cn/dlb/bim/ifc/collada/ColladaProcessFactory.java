package cn.dlb.bim.ifc.collada;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.component.PlatformServerConfig;
import cn.dlb.bim.utils.PathUtils;

public class ColladaProcessFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(ColladaProcessFactory.class);
	
	private Path nativeExcuteFile;
	private PlatformServer server;
	
	public ColladaProcessFactory(PlatformServer server) {
		this.server = server;
	}
	
	public void initialize() {
		try {
			String os = System.getProperty("os.name").toLowerCase();
			String libraryName = "";
			if (os.contains("windows")) {
				libraryName = "collada2gltf.exe";
			} else if (os.contains("osx") || os.contains("os x") || os.contains("darwin")) {
				libraryName = "collada2gltf";
			} else if (os.contains("linux")) {
				libraryName = "collada2gltf";
			}
			InputStream inputStream = Files.newInputStream(server.getPlatformServerConfig().getCompileClassRoute().resolve("lib/" + System.getProperty("sun.arch.data.model") + "/" + libraryName));
			if (inputStream != null) {
				try {
					Path tmpFolder = server.getPlatformServerConfig().getTempDir();
					Path nativeFolder = tmpFolder.resolve("collada2gltf");
					nativeExcuteFile = nativeFolder.resolve(libraryName);
					if (Files.exists(nativeFolder)) {
						try {
							PathUtils.removeDirectoryWithContent(nativeFolder);
						} catch (IOException e) {
							// Ignore
						}
					}
					Files.createDirectories(nativeFolder);
					OutputStream outputStream = Files.newOutputStream(nativeExcuteFile);
					try {
						IOUtils.copy(inputStream, outputStream);
					} finally {
						outputStream.close();
					}
				} finally {
					inputStream.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ColladaProcess createColladaProcess(Path file, Path basePath) {
		ColladaProcess process = new ColladaProcess(nativeExcuteFile, file, basePath);
		return process;
	}
}
