package cn.dlb.bim.database.transaction;

import com.sleepycat.je.Transaction;

/**
 * 带事务的server，配合TransactionProxyManager，TransactionInvaocationHandler使用
 * @author shenan4321
 */
public abstract class TransactionalService {
	
	private Transaction transaction;
	
	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	public Transaction getTransaction() {
		return transaction;
	}
	
}
