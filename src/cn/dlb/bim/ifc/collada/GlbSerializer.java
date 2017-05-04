package cn.dlb.bim.ifc.collada;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cn.dlb.bim.PlatformContext;
import cn.dlb.bim.component.PlatformServer;
import cn.dlb.bim.ifc.collada.ColladaProcess.Collada2GLTFConfiguration;
import cn.dlb.bim.ifc.emf.IfcModelInterface;
import cn.dlb.bim.ifc.emf.ProjectInfo;
import cn.dlb.bim.ifc.serializers.EmfSerializer;
import cn.dlb.bim.ifc.serializers.SerializerException;
import cn.dlb.bim.ifc.shared.ProgressReporter;
import cn.dlb.bim.utils.PathUtils;

public class GlbSerializer extends EmfSerializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(GlbSerializer.class);
	private ColladaSerializer colladaSerializer = null;
	private ProjectInfo projectInfo = null;
	private final PlatformServer server;
	
	// Export settings-related.
	private String returnType = ".glb";
	public Collada2GLTFConfiguration configuration = new Collada2GLTFConfiguration();
	
	// Filter files that are not directories and Collada files (.DAE).
	private static final FilenameFilter ignoreDAEFilter = new FilenameFilter()
	{
		@Override public boolean accept(File dir, String name)
		{
			File file = new File(dir, name);
			boolean notADirectory = !file.isDirectory();
			String realName = name.toLowerCase(Locale.ENGLISH);
			boolean notADAE = !realName.endsWith(".dae");
			return notADirectory && notADAE;
		}
	};
	
	//
	public GlbSerializer(PlatformServer server) {
		super();
		this.server = server;
	}

	public GlbSerializer(PlatformServer server, Collada2GLTFConfiguration configuration) {
		super();
		//
		this.server = server;
		this.configuration = configuration;
	}

	public GlbSerializer(PlatformServer server, Collada2GLTFConfiguration configuration, String returnType) {
		super();
		//
		this.server = server;
		this.configuration = configuration;
		this.returnType = returnType.toLowerCase(Locale.ENGLISH);
	}

	public GlbSerializer(PlatformServer server, String returnType) {
		super();
		//
		this.server = server;
		this.returnType = returnType.toLowerCase(Locale.ENGLISH);
	}

	//
	@Override
	public void init(IfcModelInterface model, ProjectInfo projectInfo, boolean normalizeOids) throws SerializerException {
		super.init(model, projectInfo, normalizeOids);
		this.projectInfo = projectInfo;
		try {
			colladaSerializer = new ColladaSerializer();
			colladaSerializer.init(model, projectInfo, normalizeOids);
		} catch (SerializerException e) {
			throw new SerializerException(e);
		}
		// Set the file name to be exported (after it's been serialized in the Collada serializer).
		this.configuration.fileName = projectInfo.getName() + ".dae";
	}

	@Override
	protected boolean write(OutputStream outputStream, ProgressReporter progressReporter) throws SerializerException {
		if (getMode() == Mode.BODY) {
			Path writeDirectory = null;
			try {
				Path tempDirectory = PlatformContext.getTempPath();
				if (!Files.exists(tempDirectory))
					Files.createDirectory(tempDirectory);
				//
				UUID id = UUID.randomUUID();
				writeDirectory = tempDirectory.resolve(id.toString());
				if (!Files.exists(writeDirectory)) {
					Files.createDirectories(writeDirectory);
				}
				// Export the IFC objects internally into a DAE then into the output of collada2gltf. 
				exportToGLTF(writeDirectory);
				//
				if (returnType == ".json")
					jsonTheDirectory(outputStream, writeDirectory);
				else if (returnType == ".glb") 
					glbTheDirectory(outputStream, writeDirectory);
			} catch (IOException e) {
				LOGGER.error("", e);
			} finally {
				// Attempt to clean up the temporary directory created by this serializer.
				try {
					if (writeDirectory != null && Files.exists(writeDirectory)) {
						PathUtils.removeDirectoryWithContent(writeDirectory);
					}
				} catch (IOException ioe) {}
			}
			setMode(Mode.FINISHED);
			return true;
		} else if (getMode() == Mode.FINISHED) {
			return false;
		} else if (getMode() == Mode.HEADER) {
			setMode(Mode.BODY);
			return true;
		}
		return false;
	}

	private void jsonTheDirectory(OutputStream outputStream, Path writeDirectory) throws IOException, UnsupportedEncodingException {
		OutputStream jsonOutputStream = outputStream;
		// Write the opening brace and a new-line.
		jsonOutputStream.write(String.format("{%n").getBytes());
		
		glbTheDirectory(jsonOutputStream, writeDirectory);
		
		// Write the closing brace.
		jsonOutputStream.write(String.format("}").getBytes());
		// Push the data into the parent stream (gets returned to the server).
		jsonOutputStream.flush();
		jsonOutputStream.close();
	}

	public void encodeFileToBase64Stream(Path file, OutputStream base64OutputStream) throws IOException {
		InputStream inputStream = Files.newInputStream(file);
		OutputStream out = new Base64OutputStream(base64OutputStream, true);
		IOUtils.copy(inputStream, out);
		inputStream.close();
		out.close();
	}

	private void exportToGLTF(Path writeDirectory) throws IOException, FileNotFoundException, SerializerException {
		Path colladaFile = writeDirectory.resolve(projectInfo.getName() + ".dae");
		// Create the Collada file: example.dae
		if (!Files.exists(colladaFile)) {
			Files.createFile(colladaFile);
		}
		// Prepare to write the Collada file.
		OutputStream outputStream = Files.newOutputStream(colladaFile);
		// Write into the Collada file.
		colladaSerializer.writeToOutputStream(outputStream, null);
		// Push the data into the stream.
		outputStream.flush();
		// Finalize the stream and close the file.
		outputStream.close();
		// Launch a thread to run the collada2gltf converter.
		ColladaProcess thread = server.getColladaProcessFactory().createColladaProcess(colladaFile, writeDirectory);
		thread.process();
//		synchronized (thread) {
//			thread.start();
//			// Force wait until the thread's subprocess is done running (i.e. the files have all been created).
//			while (thread.done == false)
//			{
//				// Intentional no operation.
//			}
//		}
	}

	private void glbTheDirectory(OutputStream outputStream, Path writeDirectory) throws IOException, UnsupportedEncodingException {
		ObjectMapper objMapper = new ObjectMapper(); 
		Gltf2glbConvertor convertor = new Gltf2glbConvertor();
		for (Path f : PathUtils.list(writeDirectory)) {
			File file = f.toFile();
			if (file.getName().endsWith(".gltf")) {
				ObjectNode scene = (ObjectNode) objMapper.readTree(file);  
				try {
					ByteBuffer byteBuffer = convertor.convert(scene, writeDirectory, true, true);
					outputStream.write(byteBuffer.array());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		outputStream.flush();
		outputStream.close();
	}

}