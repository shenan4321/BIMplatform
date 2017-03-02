package cn.dlb.bim.common;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Path;

import org.springframework.stereotype.Component;

import cn.dlb.bim.emf.MetaDataManager;

@Component("CommonContext")
public class CommonContext {
	private static final String tempPathStr = "temp";
	
	private Path rootPath;
	private Path tempPath;
	private String classLocation;
	
	private MetaDataManager metaDataManager;
	
	public CommonContext() {
		init();
	}
	
	private void init() {
		String rootPathStr = "";
		try {
			rootPathStr = URLDecoder.decode(getClass().getResource("/").getFile(),"utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		rootPath = new File(rootPathStr).toPath();
		tempPath = new File(tempPathStr).toPath();
		classLocation = System.getProperty("java.class.path");
		metaDataManager = new MetaDataManager(tempPath);
		metaDataManager.init();
	}

	public Path getRootPath() {
		return rootPath;
	}

	public Path getTempPath() {
		return tempPath;
	}
	
	public String getClasslocation() {
		return classLocation;
	}

	public MetaDataManager getMetaDataManager() {
		return metaDataManager;
	}

	
}
