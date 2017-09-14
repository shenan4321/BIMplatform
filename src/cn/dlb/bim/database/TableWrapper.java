package cn.dlb.bim.database;

import java.util.LinkedHashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;
import com.sleepycat.je.SecondaryConfig;
import com.sleepycat.je.SecondaryDatabase;
import com.sleepycat.je.SecondaryKeyCreator;
import com.sleepycat.je.Transaction;

public class TableWrapper {

	private static final Logger LOGGER = LoggerFactory.getLogger(TableWrapper.class);

	private String tableName;
	private BDBEnvironment environment;
	private Database database;
	private final Set<SecondaryDatabase> secondaryDatabases;
	private boolean transactional;

	public TableWrapper(BDBEnvironment environment, boolean transactional, String tableName) {
		this.environment = environment;
		this.tableName = tableName;
		this.transactional = transactional;
		secondaryDatabases = new LinkedHashSet<>();

		DatabaseConfig databaseConfig = new DatabaseConfig();
		databaseConfig.setAllowCreate(true);
		databaseConfig.setDeferredWrite(!transactional);

		databaseConfig.setTransactional(transactional);
		databaseConfig.setSortedDuplicates(false);
		database = environment.getEnvironment().openDatabase(null, tableName, databaseConfig);
	}
	
	public void createIndex(String keyName, SecondaryKeyCreator keyCreator) {
		SecondaryConfig secConfig = new SecondaryConfig();
		secConfig.setAllowCreate(true);
		secConfig.setSortedDuplicates(true);
		secConfig.setKeyCreator(keyCreator);
//		secConfig.setAllowPopulate(true);
		SecondaryDatabase secDatabase = environment.getEnvironment().openSecondaryDatabase(null, keyName,
				database, secConfig);
		secondaryDatabases.add(secDatabase);
	}

	public byte[] get(byte[] keyBytes, Transaction transaction) {
		DatabaseEntry key = new DatabaseEntry(keyBytes);
		DatabaseEntry value = new DatabaseEntry();
		OperationStatus operationStatus = database.get(transaction, key, value, getLockMode(transaction));
		if (operationStatus == OperationStatus.SUCCESS) {
			return value.getData();
		}
		return null;
	}

	public byte[] get(String keyName, byte[] keyBytes, Transaction transaction) {

		Database finalDatabase = getDatabase(keyName);
		DatabaseEntry key = new DatabaseEntry(keyBytes);
		DatabaseEntry value = new DatabaseEntry();
		OperationStatus operationStatus = finalDatabase.get(transaction, key, value, getLockMode(transaction));
		if (operationStatus == OperationStatus.SUCCESS) {
			return value.getData();
		}
		return null;
	}

	public void delete(byte[] key, Transaction transaction) {
		DatabaseEntry entry = new DatabaseEntry(key);
		database.delete(transaction, entry);
	}

	public void store(byte[] key, byte[] value, Transaction transaction) {
		store(key, value, 0, value.length, transaction);
	}

	public void store(byte[] key, byte[] value, int offset, int length, Transaction transaction) {
		DatabaseEntry dbKey = new DatabaseEntry(key);
		DatabaseEntry dbValue = new DatabaseEntry(value, offset, length);
		database.put(transaction, dbKey, dbValue);
	}

	public void storeNoOverwrite(byte[] key, byte[] value, Transaction transaction) {
		storeNoOverwrite(key, value, 0, value.length, transaction);
	}

	public void storeNoOverwrite(byte[] key, byte[] value, int index, int length, Transaction transaction) {
		DatabaseEntry dbKey = new DatabaseEntry(key);
		DatabaseEntry dbValue = new DatabaseEntry(value, index, length);
		OperationStatus putNoOverwrite = database.putNoOverwrite(transaction, dbKey, dbValue);
		if (putNoOverwrite == OperationStatus.KEYEXIST) {
			LOGGER.error("Key exists");
		}
	}

	public LockMode getLockMode(Transaction transaction) {
		if (transactional && transaction != null) {
			return LockMode.READ_COMMITTED;
		} else {
			return LockMode.READ_UNCOMMITTED;
		}
	}

	public boolean isTransactional() {
		return transactional;
	}

	public Database getDatabase() {
		return database;
	}

	public Set<SecondaryDatabase> getSecondaryDatabases() {
		return secondaryDatabases;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void close() {
		database.close();
		for (SecondaryDatabase secDatabase : secondaryDatabases) {
			secDatabase.close();
		}
		secondaryDatabases.clear();
		database = null;
		environment = null;
	}

	public BDBEnvironment getEnvironment() {
		return environment;
	}

	public void setEnvironment(BDBEnvironment environment) {
		this.environment = environment;
	}

	public Database getDatabase(String tableName) {
		Database finalDatabase = null;
		if (tableName.equals(database.getDatabaseName())) {
			finalDatabase = database;
		} else {
			for (SecondaryDatabase secDatabase : secondaryDatabases) {
				if (tableName.equals(secDatabase.getDatabaseName())) {
					finalDatabase = secDatabase;
					break;
				}
			}
		}
		return finalDatabase;
	}

}
