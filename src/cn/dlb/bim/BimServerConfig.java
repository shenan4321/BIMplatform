package cn.dlb.bim;

/******************************************************************************
 * Copyright (C) 2009-2016  BIMserver.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see {@literal<http://www.gnu.org/licenses/>}.
 *****************************************************************************/

import java.nio.file.Path;

import cn.dlb.bim.web.ResourceFetcher;

public class BimServerConfig {
	private Path tempDir;
	private ResourceFetcher resourceFetcher;
	private String classPath;
	private int port;
	private boolean localDev;
	private Path developmentBaseDir;
	 
	public ResourceFetcher getResourceFetcher() {
		return resourceFetcher;
	}

	/**
	 * @param resourceFetcher A ResourceFetcher is an abstraction layer between BIMserver and (usually) the file system, but some implementations get resources from memory or other places as well
	 */
	public void setResourceFetcher(ResourceFetcher resourceFetcher) {
		this.resourceFetcher = resourceFetcher;
	}

	public Path getTempDir() {
		return tempDir;
	}

	public void setTempDir(Path tempDir) {
		this.tempDir = tempDir;
	}

	public String getClassPath() {
		return classPath;
	}

	/**
	 * @param classPath When running BIMserver from your IDE, some plugins can be loaded from the default classpath, you can set it here (or another classpath if you want)
	 */
	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}

	/**
	 * @param port The port on which the web server will listen (make sure it is started by calling setStartEmbeddedWebServer(true)
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	public int getPort() {
		return port;
	}

	public boolean isLocalDev() {
		return localDev;
	}

	/**
	 * @param localDev Whether this is a local development environment, it's only used for a hack to attach the webdefault.xml to jetty
	 */
	public void setLocalDev(boolean localDev) {
		this.localDev = localDev;
	}

	public void setDevelopmentBaseDir(Path developmentBaseDir) {
		this.developmentBaseDir = developmentBaseDir;
	}
	
	public Path getDevelopmentBaseDir() {
		return developmentBaseDir;
	}
}