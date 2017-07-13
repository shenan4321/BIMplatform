package cn.dlb.bim.web;

import java.nio.file.Paths;
import javax.servlet.ServletContext;

public class WarResourceFetcher extends ResourceFetcher {

	public WarResourceFetcher(ServletContext servletContext) {
		String realPath = servletContext.getRealPath("/");
		if (!realPath.endsWith("/")) {
			realPath = realPath + "/";
		}
		addPath(Paths.get(realPath + "WEB-INF"));
	}
}