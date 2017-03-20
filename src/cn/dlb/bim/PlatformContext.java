package cn.dlb.bim;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;

public class PlatformContext {
	private final static String tempPathStr = "temp";
	
	private final static Path classRootPath;
	private final static Path userPath;
	private final static Path tempPath;
	private final static String classLocation;
	
//	private static final String userPath;
	private final static String descriptor;
	private final static String resourceBase;
	private final static String contextPath = "/BIMplatform";
	private final static int serverPort = 8080;
	
	static {
		classLocation = System.getProperty("java.class.path");
		String classRootPathStr = "";
		try {
			classRootPathStr = URLDecoder.decode(PlatformContext.class.getResource("/").getFile(),"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		classRootPath = new File(classRootPathStr).toPath();
		userPath = new File(System.getProperty("user.dir")).toPath();
		resourceBase = userPath.resolve("WebContent/").toString();
		descriptor = userPath.resolve("WEB-INF/web.xml").toString();
		tempPath = new File(tempPathStr).toPath();
	}
	
	public static String getClasslocation() {
		return classLocation;
	}

	public static Path getClassRootPath() {
		return classRootPath;
	}

	public static Path getUserpPath() {
		return userPath;
	}

	public static Path getTempPath() {
		return tempPath;
	}

	public static String getDescriptor() {
		return descriptor;
	}

	public static String getResourceBase() {
		return resourceBase;
	}

	public static String getContextPath() {
		return contextPath;
	}

	public static int getServerPort() {
		return serverPort;
	}
	
}
