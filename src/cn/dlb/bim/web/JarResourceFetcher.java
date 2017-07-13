package cn.dlb.bim.web;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JarResourceFetcher extends ResourceFetcher {
	
	public JarResourceFetcher() {
		addPath(Paths.get("home"));
		addPath(Paths.get("config"));
		addPath(Paths.get("."));
	}
	
	@Override
	public Path getFile(String name) throws IOException {
		if (name.startsWith("lib")) {
			Path file = Paths.get(name);
			if (Files.exists(file)) {
				return file;
			}
		}
		return super.getFile(name);
	}
}