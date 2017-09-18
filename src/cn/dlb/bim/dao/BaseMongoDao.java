package cn.dlb.bim.dao;

import java.util.Collection;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.util.CloseableIterator;
import cn.dlb.bim.dao.impl.BatchUpdateOptions;
import cn.dlb.bim.utils.Page;

public interface BaseMongoDao<T> {  
	  
    /** 
     * 通过条件查询实体(集合) 
     *  
     * @param query 
     */  
    public Collection<T> find(Query query);  
    
    /** 
     * 通过一定的条件查询一个实体 
     *  
     * @param query 
     * @return 
     */  
    public T findOne(Query query) ; 
    
    /** 
     * 通过条件查询更新数据 
     *  
     * @param query 
     * @param update 
     * @return 
     */  
    public T update(Query query, Update update) ;  
  
    /** 
     * 保存一个对象到mongodb 
     *  
     * @param entity 
     * @return 
     */  
    public T save(T entity) ;  
    
    /** 
     * 批量保存
     * @param entity 
     * @return 
     */  
    public void saveAll(Collection<T> entityList) ; 
  
    /** 
     * 通过ID获取记录 
     *  
     * @param id 
     * @return 
     */  
    public T findById(Object id) ;  
  
    /** 
     * 通过ID获取记录,并且指定了集合名(表的意思) 
     *  
     * @param id 
     * @param collectionName 
     *            集合名 
     * @return 
     */  
    public T findById(Object id, String collectionName) ;  
      
    /** 
     * 分页查询 
     * @param page 
     * @param query 
     * @return 
     */  
    public Page<T> findPage(Page<T> page,Query query);  
      
    /** 
     * 求数据总和 
     * @param query 
     * @return 
     */  
    public long count(Query query);  
    
    /**
     * stream 查询
     * @param query
     * @return
     */
    public CloseableIterator<T> stream(Query query);
    
    /**
     * 批量更新
     * @param updates
     * @return
     */
    public int updateAll(Collection<BatchUpdateOptions> updates);
      
}
