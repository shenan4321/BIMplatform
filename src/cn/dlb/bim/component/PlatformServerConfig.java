package cn.dlb.bim.component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import cn.dlb.bim.utils.PathUtils;
import cn.dlb.bim.web.ResourceFetcher;
import cn.dlb.bim.web.WarResourceFetcher;

@Component("PlatformServerConfig")
public class PlatformServerConfig implements InitializingBean {
	
	public volatile static Boolean DEV_MODE = false;
	public final static String PLATFORM_VERSION = "0.1";
	
	@Autowired
	@Lazy(true)
	private ServletContext servletContext;
	
	private Path tempDir;
	private Path diskCachePath;
	private String classPath;
	private Path compileClassRoute;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (DEV_MODE) {
			Path projectDevPath = Paths.get(".").toAbsolutePath();
			classPath = System.getProperty("java.class.path");
			tempDir = projectDevPath.resolve("temp");
			diskCachePath = tempDir.resolve("cache");
			compileClassRoute = Paths.get(getClass().getResource("/").toURI());
		} else {
			ResourceFetcher resourceFetcher = new WarResourceFetcher(servletContext);
			compileClassRoute = Paths.get(servletContext.getRealPath("/")).resolve("WEB-INF/classes/");
			classPath = makeClassPath(resourceFetcher.getFile("lib")) + ";" + compileClassRoute;
			tempDir = Paths.get(servletContext.getRealPath("/")).resolve("temp");
			diskCachePath = tempDir.resolve("cache");
		}
	}
	
	private String makeClassPath(Path file) {
		if (file == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		try {
			for (Path f : PathUtils.list(file)) {
				if (f.getFileName().toString().toLowerCase().endsWith(".jar")) {
					sb.append(f.toString() + File.pathSeparator);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public String getClassPath() {
		return classPath;
	}

	public Path getTempDir() {
		return tempDir;
	}

	public Path getDiskCachePath() {
		return diskCachePath;
	}
	
	public Path getCompileClassRoute() {
		return compileClassRoute;
	}
	
}
