package cn.dlb.bim.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.EnvironmentLockedException;

import cn.dlb.bim.utils.PathUtils;

public class BDBEnvironment {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BDBEnvironment.class);
	
	private boolean isNew;
	private boolean useTransactions = true;
	private Environment environment;
	private CursorConfig safeCursorConfig;
	private CursorConfig unsafeCursorConfig;
	
	public BDBEnvironment(String dataDir) {
		Path dataPath = Paths.get(dataDir);
		if (Files.isDirectory(dataPath)) {
			try {
				if (PathUtils.list(dataPath).size() > 0) {
					LOGGER.info("Non-empty database directory found \"" + dataDir.toString() + "\"");
					isNew = false;
				} else {
					LOGGER.info("Empty database directory found \"" + dataDir.toString() + "\"");
					isNew = true;
				}
			} catch (IOException e) {
				LOGGER.error("", e);
			}
		} else {
			isNew = true;
			LOGGER.info("No database directory found, creating \"" + dataDir.toString() + "\"");
			try {
				Files.createDirectory(dataPath);
				LOGGER.info("Successfully created database dir \"" + dataDir.toString() + "\"");
			} catch (Exception e) {
				LOGGER.error("Error creating database dir \"" + dataDir.toString() + "\"");
			}
		}
		EnvironmentConfig envConfig = new EnvironmentConfig();
		envConfig.setCachePercent(50);
		envConfig.setAllowCreate(true);
		envConfig.setTransactional(useTransactions);
		envConfig.setTxnTimeout(10, TimeUnit.SECONDS);
		envConfig.setLockTimeout(2000, TimeUnit.MILLISECONDS);
		envConfig.setConfigParam(EnvironmentConfig.CHECKPOINTER_HIGH_PRIORITY, "true");
		envConfig.setConfigParam(EnvironmentConfig.CLEANER_THREADS, "5");
		try {
			environment = new Environment(dataPath.toFile(), envConfig);
		} catch (EnvironmentLockedException e) {
			String message = "Environment locked exception. Another process is using the same database, or the current user has no write access (database location: \""
					+ dataDir.toString() + "\")";
			LOGGER.error(message);
		} catch (com.sleepycat.je.DatabaseException e) {
			String message = "A database initialisation error has occured (" + e.getMessage() + ")";
			LOGGER.error(message);
		}
		
		safeCursorConfig = new CursorConfig();
		safeCursorConfig.setReadCommitted(true);

		unsafeCursorConfig = new CursorConfig();
		unsafeCursorConfig.setReadUncommitted(true);
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public boolean isUseTransactions() {
		return useTransactions;
	}

	public void setUseTransactions(boolean useTransactions) {
		this.useTransactions = useTransactions;
	}

	public CursorConfig getSafeCursorConfig() {
		return safeCursorConfig;
	}

	public void setSafeCursorConfig(CursorConfig safeCursorConfig) {
		this.safeCursorConfig = safeCursorConfig;
	}

	public CursorConfig getUnsafeCursorConfig() {
		return unsafeCursorConfig;
	}

	public void setUnsafeCursorConfig(CursorConfig unsafeCursorConfig) {
		this.unsafeCursorConfig = unsafeCursorConfig;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
	
	public void close() {
		environment.close();
		environment = null;
	}

}
