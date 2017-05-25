package cn.dlb.bim.cache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.activation.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.dlb.bim.utils.BinUtils;
import cn.dlb.bim.utils.PathUtils;

public class NewDiskCacheManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(NewDiskCacheManager.class);
	private final Path cacheDir;
	private final Set<String> cachedFileNames = new HashSet<>();
	private final Map<String, NewDiskCacheOutputStream> busyCaching = new HashMap<>();

	public NewDiskCacheManager(Path cacheDir) {
		this.cacheDir = cacheDir;
		try {
			if (!Files.exists(cacheDir)) {
				Files.createDirectory(cacheDir);
			}
			for (Path file : PathUtils.list(this.cacheDir)) {
				if (file.getFileName().toString().endsWith(".__tmp")) {
					Files.delete(file);
				} else {
					cachedFileNames.add(file.getFileName().toString());
				}
			}
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}
	
	public boolean contains(DownloadDescriptor downloadDescriptor) {
		String cacheKey = downloadDescriptor.getCacheKey();
		synchronized (busyCaching) {
			if (busyCaching.containsKey(cacheKey)) {
				return true;
			}
		}
		synchronized (cachedFileNames) {
			return cachedFileNames.contains(cacheKey);
		}
	}
	
	public DataSource get(DownloadDescriptor downloadDescriptor) {
		String cacheKey = downloadDescriptor.getCacheKey();
		NewDiskCacheOutputStream diskCacheOutputStream = null;
		synchronized (busyCaching) {
			diskCacheOutputStream = busyCaching.get(cacheKey);
		}
		if (diskCacheOutputStream != null) {
			try {
				LOGGER.info("Waiting for " + cacheKey);
				diskCacheOutputStream.waitForFinish();
			} catch (InterruptedException e) {
				LOGGER.error("", e);
			}
		}
		Path file = cacheDir.resolve(cacheKey);
		if (!Files.exists(file)) {
			// This is an inconsistency that can only happen when users remove cached files manually while server is running
			cachedFileNames.remove(file.getFileName().toString());
			busyCaching.remove(downloadDescriptor.getCacheKey());
			LOGGER.error("File " + file.getFileName().toString() + " not found in cache");
		} else {
			LOGGER.info("Reading from cache " + cacheKey);
			FileInputStreamDataSource fileInputStreamDataSource = new FileInputStreamDataSource(file);
			fileInputStreamDataSource.setName(downloadDescriptor.getFileNameWithoutExtension());
			return fileInputStreamDataSource;
		}
		return null;
	}
	
	public byte[] getData(DownloadDescriptor downloadDescriptor) {
		String cacheKey = downloadDescriptor.getCacheKey();
		NewDiskCacheOutputStream diskCacheOutputStream = null;
		synchronized (busyCaching) {
			diskCacheOutputStream = busyCaching.get(cacheKey);
		}
		if (diskCacheOutputStream != null) {
			try {
				LOGGER.info("Waiting for " + cacheKey);
				diskCacheOutputStream.waitForFinish();
			} catch (InterruptedException e) {
				LOGGER.error("", e);
			}
		}
		Path file = cacheDir.resolve(cacheKey);
		if (!Files.exists(file)) {
			// This is an inconsistency that can only happen when users remove cached files manually while server is running
			cachedFileNames.remove(file.getFileName().toString());
			busyCaching.remove(downloadDescriptor.getCacheKey());
			LOGGER.error("File " + file.getFileName().toString() + " not found in cache");
		} else {
			LOGGER.info("Reading from cache " + cacheKey);
			FileInputStreamDataSource fileInputStreamDataSource = new FileInputStreamDataSource(file);
			fileInputStreamDataSource.setName(downloadDescriptor.getFileNameWithoutExtension());
			byte[] inputBytes = null;
			InputStream inputStream = null;
			try {
				inputStream = fileInputStreamDataSource.getInputStream();
				inputBytes = BinUtils.readInputStream(inputStream);
			} catch (IOException e) {
				remove(diskCacheOutputStream);
				LOGGER.error("", e);
			} finally {
				try {
					if (diskCacheOutputStream != null)
						diskCacheOutputStream.close();
					inputStream.close();
				} catch (IOException e) {
					LOGGER.error("", e);
				}
			}
			return inputBytes;
		}
		return null;
	}

	public NewDiskCacheOutputStream startCaching(DownloadDescriptor downloadDescriptor) {
		try {
			String cacheKey = downloadDescriptor.getCacheKey();
			LOGGER.info("Start caching " + cacheKey);
			NewDiskCacheOutputStream out = new NewDiskCacheOutputStream(this, cacheDir.resolve(cacheKey), downloadDescriptor);
			synchronized (busyCaching) {
				busyCaching.put(cacheKey, out);
			}
			return out;
		} catch (FileNotFoundException e) {
			LOGGER.error("", e);
		}
		return null;
	}

	public synchronized Integer cleanup() {
		int removed = 0;
		try {
			for (Path file : PathUtils.list(cacheDir)) {
				try {
					Files.delete(file);
					removed++;
				} catch (IOException e) {
					LOGGER.error("", e);
				}
			}
		} catch (IOException e) {
			LOGGER.error("", e);
		}
		cachedFileNames.clear();
		return removed;
	}

	public void doneGenerating(NewDiskCacheOutputStream diskCacheOutputStream) {
		synchronized (busyCaching) {
			String cacheKey = diskCacheOutputStream.getDownloadDescriptor().getCacheKey();
			LOGGER.info("Done caching " + cacheKey);
			busyCaching.remove(cacheKey);
			cachedFileNames.add(cacheKey);
		}
	}

	public void remove(NewDiskCacheOutputStream diskCacheOutputStream) {
		String cacheKey = diskCacheOutputStream.getDownloadDescriptor().getCacheKey();
		LOGGER.info("Removing cache " + cacheKey);
		cachedFileNames.remove(cacheKey);
		synchronized (busyCaching) {
			busyCaching.remove(cacheKey);
		}
	}
	
	public void remove(DownloadDescriptor downloadDescriptor) {
		String cacheKey = downloadDescriptor.getCacheKey();
		LOGGER.info("Removing cache " + cacheKey);
		cachedFileNames.remove(cacheKey);
		synchronized (busyCaching) {
			busyCaching.remove(cacheKey);
		}
	}
}