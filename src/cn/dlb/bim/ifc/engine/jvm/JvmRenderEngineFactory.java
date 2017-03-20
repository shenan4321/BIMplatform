package cn.dlb.bim.ifc.engine.jvm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import cn.dlb.bim.PlatformContext;
import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.controller.RootController;
import cn.dlb.bim.ifc.emf.PackageMetaData;
import cn.dlb.bim.ifc.engine.IRenderEngine;
import cn.dlb.bim.ifc.engine.IRenderEngineFactory;
import cn.dlb.bim.ifc.engine.RenderEngineException;
import cn.dlb.bim.utils.PathUtils;

public class JvmRenderEngineFactory implements IRenderEngineFactory {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JvmRenderEngineFactory.class);
	
	private Path nativeFolder;
	private Path schemaFile;
	
	private PlatformServer server;
	
	public JvmRenderEngineFactory(PlatformServer server) {
		this.server = server;
	}
	
	public void initialize() {
		try {
			String os = System.getProperty("os.name").toLowerCase();
			String libraryName = "";
			if (os.contains("windows")) {
				libraryName = "ifcengine.dll";
			} else if (os.contains("osx") || os.contains("os x") || os.contains("darwin")) {
				libraryName = "libIFCEngine.dylib";
			} else if (os.contains("linux")) {
				libraryName = "libifcengine.so";
			}
			InputStream inputStream = Files.newInputStream(PlatformContext.getClassRootPath().resolve("lib/" + System.getProperty("sun.arch.data.model") + "/" + libraryName));
			if (inputStream != null) {
				try {
					Path tmpFolder = PlatformContext.getTempPath();
					nativeFolder = tmpFolder.resolve("ifcenginedll");
					Path file = nativeFolder.resolve(libraryName);
					if (Files.exists(nativeFolder)) {
						try {
							PathUtils.removeDirectoryWithContent(nativeFolder);
						} catch (IOException e) {
							// Ignore
						}
					}
					Files.createDirectories(nativeFolder);
					OutputStream outputStream = Files.newOutputStream(file);
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

	@Override
	public IRenderEngine createRenderEngine(String schema) throws RenderEngineException {
		try {
			PackageMetaData packageMetaData = server.getMetaDataManager().getPackageMetaData(schema);
			schemaFile = packageMetaData.getSchemaPath();
			if (schemaFile == null) {
				throw new RenderEngineException("No schema file");
			}
			List<String> classPathEntries = new ArrayList<>();
			
//			for (Dependency dependency : pluginContext.getDependencies()) {
//				Path path = dependency.getPath();
//				classPathEntries.add(path.toAbsolutePath().toString());
//			}
			
			return new JvmIfcEngine(schemaFile, nativeFolder, PlatformContext.getTempPath(), PlatformContext.getClasslocation(), classPathEntries);
		} catch (RenderEngineException e) {
			throw e;
		}
	}

}
