package cn.dlb.bim;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;

public class PlatformContext {
	
	private final static String platformVersion = "0.1";
	
	private final static String tempPathStr = "temp";
	
	private final static Path classRootPath;
	private final static Path userPath;
	private final static Path tempPath;
	private final static Path diskCachePath;
	private final static String classLocation;
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
		descriptor = userPath.resolve("WebContent/WEB-INF/web.xml").toString();
		tempPath = new File(tempPathStr).toPath();
		diskCachePath = tempPath.resolve("cache/");
	}
	
	public static String getPlatformVersion() {
		return platformVersion;
	}

	public static String getClasslocation() {
		return classLocation;
	}

	public static Path getClassRootPath() {
		return classRootPath;
	}

	public static Path getUserPath() {
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

	public static Path getDiskCachepath() {
		return diskCachePath;
	}
	
}
