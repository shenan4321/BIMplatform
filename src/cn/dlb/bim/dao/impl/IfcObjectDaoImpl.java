package cn.dlb.bim.dao.impl;

import java.util.Set;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.mongodb.DB;

import cn.dlb.bim.dao.IIfcObjectDao;
import cn.dlb.bim.dao.entity.IfcStoreModel;

@Repository("IfcObjectDaoImpl")
public class IfcObjectDaoImpl implements IIfcObjectDao {
	
	@Autowired  
    private MongoTemplate mongoTemplate; 
	
	public void test() {
		Set<String> colls = this.mongoTemplate.getCollectionNames();  
        for (String coll : colls) {  
            System.out.println("CollectionName=" + coll);  
        }  
        DB db = this.mongoTemplate.getDb();  
        System.out.println("db=" + db.toString());  
	}
	
	public void insertIfcStoreModel(IfcStoreModel ifcStoreModel) {
		mongoTemplate.insert(ifcStoreModel);
	}
}
