package cn.dlb.bim.ifc.collada;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColladaProcess {
	// Thread-related.
	public boolean done = false;
	public boolean crashed = false;

	// File-related.
	private Path basePath = null;
	private Collada2GLTFConfiguration configuration = new Collada2GLTFConfiguration(); 

	public ColladaProcess(Path executeFile, Path file, Path basePath){
		this.configuration = new Collada2GLTFConfiguration(executeFile.getFileName().toString(), file.getFileName().toString());
		this.basePath = basePath;
	}
	
	public ColladaProcess(Path basePath, Collada2GLTFConfiguration configuration){
		this.basePath = basePath;
		this.configuration = configuration;
	}

	public void process() {
		done = false;
		crashed = false;
		// Build the process.
		ProcessBuilder builder = new ProcessBuilder(configuration.getCall());
		// Set the working directory to the place where the DAE is.
		builder.directory(basePath.toFile());
		try {
			// Attempt to run the subprocess.
			Process p = builder.start();
			// Attempt to wait for it to finish.
			synchronized(p) {
				p.waitFor();
			}
		} catch (IOException e) {
			e.printStackTrace();
			crashed = true;
		} catch (InterruptedException e) {
			e.printStackTrace();
			crashed = true;
		}
		done = true;
	}

	public static class Collada2GLTFConfiguration {
		// OpenGL Transformation Format settings.
		public String executeFile;
		public String fileName = null;
		public String compressionType = null; // Can be Open3DGC; -c "Open3DGC"
		public String compressionMode = null; // Can be "ascii", "binary"; -m "ascii"
		public boolean wantDefaultLighting = false; // -l (if true)
		public boolean wantExperimentalMode = false; // -s (if true)
		public boolean wantExportAnimations = true; // -a true (default; or) -a false
		public boolean wantInvertTransparency = false; // -i (if true)
		public boolean wantIndividualPasses = false; // -d (if true)
		
		public Collada2GLTFConfiguration()
		{
			// Use default settings.
		}
		
		public Collada2GLTFConfiguration(String executeFile, String fileName)
		{
			this.executeFile = executeFile;
			this.fileName = fileName;
		}
		
		public Collada2GLTFConfiguration(String executeFile, String fileName, String compressionType, String compressionMode, boolean wantDefaultLighting, boolean wantExperimentalMode, boolean wantExportAnimations, boolean wantInvertTransparency, boolean wantIndividualPasses) {
			this.executeFile = executeFile;
			this.fileName = fileName;
			this.compressionType = compressionType;
			this.compressionMode = compressionMode;
			this.wantDefaultLighting = wantDefaultLighting;
			this.wantExperimentalMode = wantExperimentalMode;
			this.wantExportAnimations = wantExportAnimations;
			this.wantInvertTransparency = wantInvertTransparency;
			this.wantIndividualPasses = wantIndividualPasses;
		}
		
		public Collada2GLTFConfiguration(String compressionType, String compressionMode, boolean wantDefaultLighting, boolean wantExperimentalMode, boolean wantExportAnimations, boolean wantInvertTransparency, boolean wantIndividualPasses) {
			this.compressionType = compressionType;
			this.compressionMode = compressionMode;
			this.wantDefaultLighting = wantDefaultLighting;
			this.wantExperimentalMode = wantExperimentalMode;
			this.wantExportAnimations = wantExportAnimations;
			this.wantInvertTransparency = wantInvertTransparency;
			this.wantIndividualPasses = wantIndividualPasses;
		}
		
		public List<String> getCall()
		{
			ArrayList<String> list = new ArrayList<String>();
			// Add the application.
			list.add("D:\\collada2gltf-web-service-master\\collada2gltf\\win32\\collada2gltf.exe");
			// Required parameters.
			if (fileName != null)
				list.addAll(Arrays.asList(new String[] {"-f", String.format("\"%s\"", fileName)}));
			else
				return list;
			// Optional parameters.
			if (compressionType != null)
				list.addAll(Arrays.asList(new String[] {"-c", String.format("\"%s\"", compressionType)}));
			if (compressionMode != null)
				list.addAll(Arrays.asList(new String[] {"-m", String.format("\"%s\"", compressionMode)}));
			if (wantDefaultLighting)
				list.addAll(Arrays.asList(new String[] {"-l", }));
			if (wantExperimentalMode)
				list.addAll(Arrays.asList(new String[] {"-s", }));
			if (!wantExportAnimations)
				list.addAll(Arrays.asList(new String[] {"-a", String.format("\"%s\"", "false")}));
			if (wantInvertTransparency)
				list.addAll(Arrays.asList(new String[] {"-i", }));
			if (wantIndividualPasses)
				list.addAll(Arrays.asList(new String[] {"-d", }));
			return list;
		}
	}
}
