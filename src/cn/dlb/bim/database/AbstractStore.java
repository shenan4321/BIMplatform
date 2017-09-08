package cn.dlb.bim.database;

import java.util.concurrent.ConcurrentHashMap;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.LockConflictException;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;

public class AbstractStore {
	protected final int MAX_RETRY = 5; //  Used for while loop and deadlock retries  
    protected EntityStore store;  
    //protected Transaction txn;  
      
    //放需要commit和clean的Transaction  
    ConcurrentHashMap<EntityCursor<? extends Object>, Transaction> cursorTransactionMap   
        = new ConcurrentHashMap<EntityCursor<? extends Object>, Transaction>();  
      
    //-----Construction Method------  
    public AbstractStore(BDBEnvironment env){  
        store = new EntityStore(env.getEnv(), "EntityStore", env.getStoreConfig());  
    }  
      
    public void clean(){  
        store.getEnvironment().cleanLog();  
    }  
      
    /** 
     * entity cursor used after getEntityCursor() and operations. 
     *  
     * **/  
    public void cursorCommitAndClean(EntityCursor<? extends Object> cursor){  
          
        Transaction txn = cursorTransactionMap.get(cursor); //取到txn  
        cursorTransactionMap.remove(cursor);  
          
        boolean retry = true;  
        int retry_count = 0;  
        cursor.close();  
        cursor = null;  
        while (retry) {  
            try {  
                try {  
  
                    //txn.commit();  
                    txn.commit();  
                    txn = null;  
                } catch (DatabaseException e) {  
                    System.err.println("Error on txn commit: " + e.toString());  
                }  
                retry = false;  
            } catch (LockConflictException de) {  
                System.out.println("BDB: Deadlock");  
                // retry if necessary  
                if (retry_count < MAX_RETRY) {  
                    retry = true;  
                    retry_count++;  
                } else {  
                    System.err.println("BDB: Out of retries[commit entityCursor]. Giving up.");  
                    retry = false;  
                }  
            } catch (DatabaseException e) {  
                retry = false;   // abort and don't retry  
                System.err.println("BDB exception: " + e.toString());  
                e.printStackTrace();  
            } finally {  
                if (cursor != null) {  
                    cursor.close();  
                }  
                if (txn != null) {  
                    try {  
                        txn.abort();  
                    } catch (Exception e) {  
                        System.err.println("Error aborting transaction: " + e.toString());  
                        e.printStackTrace();  
                    }  
                }  
            }  
        }  
    }  
      
    // Close the store and environment  
    public void close() {  
        System.out.println("close dataBase.");  
        if (store != null ) {  
            try {  
                store.close();  
            } catch (DatabaseException e) {  
                System.err.println("closeEnv: store: " + e.toString());  
                e.printStackTrace();  
            }  
        }  
        if (store.getEnvironment() != null ) {  
            try {  
                store.getEnvironment().cleanLog();  
              
                store.getEnvironment().close();  
            } catch (DatabaseException e) {  
                System.err.println("closeEnv: " + e.toString());  
                e.printStackTrace();  
            }  
        }  
    }  
      
    public EntityStore getStore() {  
        return store;  
    }  
    public void setStore(EntityStore store) {  
        this.store = store;  
    }  
}
