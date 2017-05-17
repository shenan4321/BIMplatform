package cn.dlb.bim.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;

public class IfcProductRecordTextSearch extends AbstractLuceneSearch<IfcProductRecordText> {

	public IfcProductRecordTextSearch(File indexDir) {
		super(indexDir);
	}

	@Override
	public List<Document> getDoc(List<IfcProductRecordText> items) {
		
		List<Document> docs = new ArrayList<Document>();  
        FieldType storedType = new FieldType();  
        storedType.setIndexed(true);  
        storedType.setStored(false);  
        storedType.setTokenized(true); 
        
        FieldType unTokeType = new FieldType();
        unTokeType.setIndexed(true);  
        unTokeType.setStored(true);   
        unTokeType.setTokenized(false); 
        
        for (IfcProductRecordText record : items) {
        	Document doc = new Document();
        	
        	Field oid = genStringFieldCheckNull("oid", record.getOid(), unTokeType);
        	Field location = genStringFieldCheckNull("location", record.getLocation(), storedType);
        	Field type = genStringFieldCheckNull("type", record.getType(), storedType);
        	Field name = genStringFieldCheckNull("name", record.getName(), storedType);
        	Field detail = genStringFieldCheckNull("detail", record.getDetail(), storedType);
        	
        	doc.add(oid);
        	doc.add(location);
        	doc.add(type);
        	doc.add(name);
        	doc.add(detail);
        	
        	docs.add(doc);
        }
		return docs;
		
		
	}

	@Override
	public List<IfcProductRecordText> toBean(IndexSearcher indexSearcher, Query query, ScoreDoc[] hits) {
		List<IfcProductRecordText> hitRecords = new ArrayList<IfcProductRecordText>();  
		try {
			
			for (int i = 0; i < hits.length; i++) {
				ScoreDoc scoreDoc = hits[i];
				Document hitDoc = indexSearcher.doc(scoreDoc.doc);
				String oid = hitDoc.get("oid");
				String location = hitDoc.get("location");
				String type = hitDoc.get("type");
				String name = hitDoc.get("name");
				String detail = hitDoc.get("detail");
	
				IfcProductRecordText record = new IfcProductRecordText();
				record.setOid(oid);
				record.setLocation(location);
				record.setType(type);
				record.setName(name);
				record.setDetail(detail);
				
				hitRecords.add(record);
				
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		} 
		return hitRecords;
	}
	
	private Field genStringFieldCheckNull(String fieldName, String fieldValue, FieldType type) {
		if (fieldValue != null) {
			 return new Field(fieldName, fieldValue, type);
		} else {
			return new Field(fieldName, "", type);
		}
	}

}
