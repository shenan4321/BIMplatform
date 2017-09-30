package cn.dlb.bim.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.CloseableIterator;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import cn.dlb.bim.dao.BaseMongoDao;
import cn.dlb.bim.utils.Page;
import cn.dlb.bim.utils.ReflectionUtils;

public abstract class AbstractBaseMongoDao<T> implements BaseMongoDao<T>{  
	  
    private static final int DEFAULT_SKIP = 0;  
    private static final int DEFAULT_LIMIT = 200;  
      
    /** 
     * spring mongodb　集成操作类　 
     */  
    protected MongoTemplate mongoTemplate;  

    @Override  
    public List<T> find(Query query) {  
        return mongoTemplate.find(query, this.getEntityClass());  
    }  
    
    @Override  
    public T findOne(Query query) {  
        return mongoTemplate.findOne(query, this.getEntityClass());  
    }  
  
    @Override  
    public T update(Query query, Update update) {  
        return mongoTemplate.findAndModify(query, update, this.getEntityClass());  
    }  
  
    @Override  
    public T save(T entity) {  
        mongoTemplate.insert(entity);  
        return entity;  
    }  
    
    @Override
    public void saveAll(Collection<T> entityList) {
    	mongoTemplate.insertAll(entityList);
    }
  
    @Override  
    public T findById(Object id) {  
        return mongoTemplate.findById(id, this.getEntityClass());  
    }  
  
    @Override  
    public T findById(Object id, String collectionName) {  
        return mongoTemplate.findById(id, this.getEntityClass(), collectionName);  
    }  

    @Override  
    public Page<T> findPage(Page<T> page,Query query){  
        long count = this.count(query);
        page.setTotal(count);
        int pageNumber = page.getPageNumber();
        int pageSize = page.getPageSize();  
        query.skip((pageNumber - 1) * pageSize).limit(pageSize);  
        List<T> rows = this.find(query);  
        page.setRows(rows);  
        return page;  
    }  
      
    @Override  
    public long count(Query query){  
        return mongoTemplate.count(query, this.getEntityClass());  
    }  
    
    @Override
    public CloseableIterator<T> stream(Query query) {
    	return mongoTemplate.stream(query, this.getEntityClass());
    }
    
    @Override
    public int updateAll(Collection<BatchUpdateOptions> updates) {
    	String collectionName = determineCollectionName(ReflectionUtils.getSuperClassGenricType(getClass()));
		return doBatchUpdate(mongoTemplate.getCollection(collectionName), collectionName, updates, true);
    }
    
    public void findAndDelete(Query query) {
    	mongoTemplate.remove(query, this.getEntityClass());
    }
  
    /** 
     * 获取需要操作的实体类class 
     *  
     * @return 
     */  
    private Class<T> getEntityClass(){  
        return ReflectionUtils.getSuperClassGenricType(getClass());  
    }  
    
    private int doBatchUpdate(DBCollection dbCollection, String collName, Collection<BatchUpdateOptions> options,
			boolean ordered) {
		DBObject command = new BasicDBObject();
		command.put("update", collName);
		List<BasicDBObject> updateList = new ArrayList<BasicDBObject>();
		for (BatchUpdateOptions option : options) {
			BasicDBObject update = new BasicDBObject();
			update.put("q", option.getQuery().getQueryObject());
			update.put("u", option.getUpdate().getUpdateObject());
			update.put("upsert", option.isUpsert());
			update.put("multi", option.isMulti());
			updateList.add(update);
		}
		command.put("updates", updateList);
		command.put("ordered", ordered);
		CommandResult commandResult = dbCollection.getDB().command(command);
		return Integer.parseInt(commandResult.get("n").toString());
	}

	private static String determineCollectionName(Class<?> entityClass) {
		if (entityClass == null) {
			throw new InvalidDataAccessApiUsageException(
					"No class parameter provided, entity collection can't be determined!");
		}
		String collName = entityClass.getSimpleName();
		if (entityClass.isAnnotationPresent(Document.class)) {
			Document document = entityClass.getAnnotation(Document.class);
			collName = document.collection();
		} else {
			collName = collName.replaceFirst(collName.substring(0, 1), collName.substring(0, 1).toLowerCase());
		}
		return collName;
	}
  
    /** 
     * 注入mongodbTemplate 
     *  
     * @param mongoTemplate 
     */  
    protected abstract void setMongoTemplate(MongoTemplate mongoTemplate);  
  
}  
