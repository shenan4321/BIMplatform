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
	
	public static final String Key_Oid = "oid";
	public static final String Key_Location = "location";
	public static final String Key_Type = "type";
	public static final String Key_Name = "name";
	public static final String Key_Detail = "detail";

	public IfcProductRecordTextSearch(File indexDir) {
		super(indexDir);
	}

	@Override
	public List<Document> getDoc(List<IfcProductRecordText> items) {
		
		List<Document> docs = new ArrayList<Document>();  
        FieldType storedType = new FieldType();  
        storedType.setIndexed(true);  
        storedType.setStored(true);  
        storedType.setTokenized(true); 
        
        FieldType unTokeType = new FieldType();
        unTokeType.setIndexed(true);  
        unTokeType.setStored(true);   
        unTokeType.setTokenized(false); 
        
        for (IfcProductRecordText record : items) {
        	Document doc = new Document();
        	
        	Field oid = genStringFieldCheckNull(Key_Oid, record.getOid(), unTokeType);
        	Field location = genStringFieldCheckNull(Key_Location, record.getLocation(), storedType);
        	Field type = genStringFieldCheckNull(Key_Type, record.getType(), storedType);
        	Field name = genStringFieldCheckNull(Key_Name, record.getName(), storedType);
        	Field detail = genStringFieldCheckNull(Key_Detail, record.getDetail(), storedType);
        	
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
				String oid = hitDoc.get(Key_Oid);
				String location = hitDoc.get(Key_Location);
				String type = hitDoc.get(Key_Type);
				String name = hitDoc.get(Key_Name);
				String detail = hitDoc.get(Key_Detail);
	
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
