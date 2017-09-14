package cn.dlb.bim.database.transaction;

import java.lang.reflect.Proxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sleepycat.je.Environment;

/**
 * 事务代理管理器
 * @author shenan4321
 *
 */
public class TransactionProxyManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TransactionProxyManager.class);
	
	private final Environment env; 
	
	public TransactionProxyManager(Environment env) {
		this.env = env;
	}
	
    /**
     * 为服务层代理事务
     * @param object
     * @return
     */
    @SuppressWarnings("rawtypes")
	public Object proxyFor(TransactionalService object)
    {  
    	LOGGER.info("TransactionProxyManager proxy for:" + object.getClass());
    	ClassLoader classLoder = object.getClass().getClassLoader();
		Class[] interfaces = object.getClass().getInterfaces();
        return Proxy.newProxyInstance(classLoder, interfaces, new TransactionInvocationHandler(object, env));  
    } 
}
