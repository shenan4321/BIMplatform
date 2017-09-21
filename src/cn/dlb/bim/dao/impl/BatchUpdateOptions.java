package cn.dlb.bim.dao.impl;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class BatchUpdateOptions {
	private Query query;
    private Update update;
    //如果文档中不存在update的记录,是否插入,默认是false,不插入.
    private boolean upsert = false;
    //只更新找到的第一条记录,如果这个参数为true,就把按条件查出来多条记录全部更新.
    private boolean multi = false;
    
    public BatchUpdateOptions(Query query, Update update, boolean upsert, boolean multi) {
    	this.query = query;
    	this.update = update;
    	this.upsert = upsert;
    	this.multi = multi;
	}
    
	public Query getQuery() {
		return query;
	}
	public void setQuery(Query query) {
		this.query = query;
	}
	public Update getUpdate() {
		return update;
	}
	public void setUpdate(Update update) {
		this.update = update;
	}
	public boolean isUpsert() {
		return upsert;
	}
	public void setUpsert(boolean upsert) {
		this.upsert = upsert;
	}
	public boolean isMulti() {
		return multi;
	}
	public void setMulti(boolean multi) {
		this.multi = multi;
	}
}
