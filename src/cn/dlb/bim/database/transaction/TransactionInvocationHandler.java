package cn.dlb.bim.database.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;
import com.sleepycat.je.TransactionConfig;

/**
 * 事务调用handler
 * @author shenan4321
 *
 */
public class TransactionInvocationHandler implements InvocationHandler {
	
	
    private TransactionalService proxy;  
    private Environment env;
        
    /**
     * 构造函数
     * @param object
     * @param factory
     */
    TransactionInvocationHandler(TransactionalService object, Environment env)  
    {  
        this.proxy = object;  
        this.env = env;  
    }  
        
    
    @Override
    public Object invoke(Object o, Method method, Object[] objects) throws Throwable  
    {  
    	
    	Method originalMethod = proxy.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (!originalMethod.isAnnotationPresent(Transactional.class))
        {
            return method.invoke(proxy, objects);
        }
    	
    	Transaction txn = null; 
    	TransactionConfig transactionConfig = new TransactionConfig();
		transactionConfig.setReadCommitted(true);
		
        Object result = null;  
        try  
        {  
        	txn = env.beginTransaction(null, transactionConfig);
        	proxy.setTransaction(txn);
            result = method.invoke(proxy, objects);  
            txn.commit();
        } catch (Exception e)  
        {  
        	txn.abort();  
        }
        return result;  
    }  
}
