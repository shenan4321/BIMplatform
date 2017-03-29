package cn.dlb.bim.component;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;

@Component("MongoGridFs")
public class MongoGridFs {

	@Autowired
	@Qualifier("gridFsTemplate")
	GridFsTemplate gridFsTemplate;
	
	/**
	 * 
	 * @param inputStream
	 * @param fileName
	 */
	public GridFSFile saveSourceIfcFile(InputStream inputStream, String fileName) {
		DBObject metaData = new BasicDBObject();
		metaData.put("bimType", "ifcSource");
		return gridFsTemplate.store(inputStream, fileName);
	}
	
	public GridFSFile saveIfcModel(InputStream inputStream, String fileName, int rid) {
		DBObject metaData = new BasicDBObject("rid", rid);
		metaData.put("bimType", "ifcModel");
		metaData.put("rid", rid);
		return gridFsTemplate.store(inputStream, fileName, metaData);
	}
	
	public GridFSDBFile find(String fileName, String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("filename").is(fileName));
		query.addCriteria(Criteria.where("_id").is(id));
		return gridFsTemplate.findOne(query);
	}
	
	public GridFSDBFile findIfcModel(int rid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("metadata.rid").is(rid));
		return gridFsTemplate.findOne(query);
	}
	
}
