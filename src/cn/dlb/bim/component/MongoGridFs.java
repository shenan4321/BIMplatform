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
	
	private static String STORE_BIM_TYPE = "bim_type";
	private static String STORE_IFC_FILE = "ifc_file";
	private static String STORE_IFC_MODEL = "ifc_model";
	private static String STORE_GLB_FILE = "glb_file";
	
	/**
	 * 
	 * @param inputStream
	 * @param fileName
	 */
	public GridFSFile saveSourceIfcFile(InputStream inputStream, String fileName) {
		DBObject metaData = new BasicDBObject();
		metaData.put(STORE_BIM_TYPE, STORE_IFC_FILE);
		return gridFsTemplate.store(inputStream, fileName);
	}
	
	public GridFSFile saveIfcModel(InputStream inputStream, String fileName, int rid) {
		DBObject metaData = new BasicDBObject("rid", rid);
		metaData.put(STORE_BIM_TYPE, STORE_IFC_MODEL);
		metaData.put("rid", rid);
		return gridFsTemplate.store(inputStream, fileName, metaData);
	}
	
	public GridFSDBFile find(String fileName, String id) {
		Query query = new Query();
		query.addCriteria(Criteria.where("filename").is(fileName));
		query.addCriteria(Criteria.where("_id").is(id));
		return gridFsTemplate.findOne(query);
	}
	
	@SuppressWarnings("static-access")
	public GridFSDBFile findIfcModel(int rid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("metadata.rid").is(rid).andOperator(Criteria.where("metadata." + STORE_BIM_TYPE).is(STORE_IFC_MODEL)));
		return gridFsTemplate.findOne(query);
	}
	
	public GridFSFile saveGlbFile(InputStream inputStream, String fileName, int rid, double lon, double lat) {
		DBObject metaData = new BasicDBObject("rid", rid);
		metaData.put(STORE_BIM_TYPE, STORE_GLB_FILE);
		metaData.put("rid", rid);
		metaData.put("lon", lon);
		metaData.put("lat", lat);
		return gridFsTemplate.store(inputStream, fileName, metaData);
	}
	
	@SuppressWarnings("static-access")
	public GridFSDBFile findGlbFile(int rid) {
		Query query = new Query();
		query.addCriteria(Criteria.where("metadata.rid").is(rid).andOperator(Criteria.where("metadata." + STORE_BIM_TYPE).is(STORE_GLB_FILE)));
		return gridFsTemplate.findOne(query);
	}
	
}
