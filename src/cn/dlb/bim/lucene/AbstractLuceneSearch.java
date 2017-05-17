package cn.dlb.bim.lucene;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

public abstract class AbstractLuceneSearch<T> {  
    public File indexDir; // 存放索引文件的目录  
    protected static Analyzer analyzer = new IKAnalyzer(); // 分词器 
    public static Version LUCENE_VERSION = Version.LUCENE_47;  
  
    public AbstractLuceneSearch(File indexDir) {  
        this.indexDir = indexDir;  
    }  
  
    /** 
     * 为数据库检索数据创建索引 
     *  
     * @param <T> 
     */  
    public void createIndex(List<T> items) {  
  
        Directory directory = null;  
        IndexWriter indexWriter = null;  
        try {  
            directory = FSDirectory.open(indexDir);  
            IndexWriterConfig config = new IndexWriterConfig(LUCENE_VERSION, analyzer);  
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);// 设置打开索引模式为创建或追加  
            indexWriter = new IndexWriter(directory, config);  
            // 装配成document  
            List<Document> docs = getDoc(items);  
            for (Document doc : docs) {  
                indexWriter.addDocument(doc);  
            }  
  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
        	destoryIndexWriter(indexWriter); 
        	destoryFSDirectory(directory);
        }  
    }  
    
    /**
     * 更新索引
     * @param items
     * @param tern 需要更新的term
     */
    public void insertOrUpdateIndex(List<T> items, Term tern) {
        Directory directory = null;  
        IndexWriter indexWriter = null;  
        try {  
            directory = FSDirectory.open(indexDir);  
            IndexWriterConfig config = new IndexWriterConfig(LUCENE_VERSION, analyzer);  
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);// 设置打开索引模式为创建或追加  
            indexWriter = new IndexWriter(directory, config);  
            // 装配成document  
            List<Document> docs = getDoc(items);  
            indexWriter.updateDocuments(tern, docs);
  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
        	destoryIndexWriter(indexWriter); 
        	destoryFSDirectory(directory);
        }  
    }
  
    /** 
     * 搜索索引 
     *  
     * @param queryStr 
     * @param queryField 
     * @return 
     */  
    public List<T> search(String queryStr, String queryField, int limit) {  
        List<T> hitItem = null;  
        IndexReader reader = null;  
        IndexSearcher indexSearcher = null;  
        try {  
            indexSearcher = getIndexSearcher();
            QueryParser parser = new QueryParser(LUCENE_VERSION, queryField, analyzer);  
            Query query = parser.parse(queryStr);  
            ScoreDoc[] hits = indexSearcher.search(query, limit).scoreDocs;  
            hitItem = toBean(indexSearcher, query, hits);  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (reader != null)  
                try {  
                    reader.close();  
                } catch (IOException e) {  
                    e.printStackTrace();  
                }  
        }  
  
        return hitItem;  
    }  
    
    /**
     * 删除索引
     * @param query
     */
    public void delete(Term delTerm) {  
    	Directory directory = null;  
        IndexWriter indexWriter = null;  
    	try {
			directory = FSDirectory.open(indexDir);
			IndexWriterConfig config = new IndexWriterConfig(LUCENE_VERSION, analyzer);  
	        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);// 设置打开索引模式为创建或追加  
	        indexWriter = new IndexWriter(directory, config);  

	        indexWriter.deleteDocuments(delTerm);
		} catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
        	destoryIndexWriter(indexWriter); 
        	destoryFSDirectory(directory);
        }  
        
    } 
    
    private void destoryIndexWriter(IndexWriter writer) {
    	if (writer == null) {
    		return;
    	}
    	try {  
    		writer.close();  
    	} catch (IOException e) {  
    		e.printStackTrace();  
    	} 
    }
    
    private void destoryFSDirectory(Directory directory) {
    	if (directory == null) {
    		return;
    	}
    	
    	try {
			directory.close();
		} catch (IOException e) {
			e.printStackTrace();
		}  
    }
    
    
    /** 
     * 装配成document对象 
     *  
     * @param items 
     * @return 
     */  
    public abstract List<Document> getDoc(List<T> items);  
    
    /** 
     * 将搜索结果还原成Bean 
     *  
     * @param indexSearcher 
     * @param query 
     * @param hits 
     * @return 
     */  
    public abstract List<T> toBean(IndexSearcher indexSearcher, Query query, ScoreDoc[] hits);  
  
    /** 
     * 高亮设置 
     *  
     * @param query 
     * @param doc 
     * @param field 
     * @return 
     */  
    protected String toHighlighter(Query query, Document doc, String field) {  
        try {  
            SimpleHTMLFormatter simpleHtmlFormatter = new SimpleHTMLFormatter("<font color=\"blue\">", "</font>");  
            Highlighter highlighter = new Highlighter(simpleHtmlFormatter, new QueryScorer(query));  
            TokenStream tokenStream1 = analyzer.tokenStream("text", new StringReader(doc.get(field)));  
            String highlighterStr = highlighter.getBestFragment(tokenStream1, doc.get(field));  
            return highlighterStr == null ? doc.get(field) : highlighterStr;  
        } catch (IOException e) {  
            e.printStackTrace();  
        } catch (InvalidTokenOffsetsException e) {  
            e.printStackTrace();  
        }  
        return null;  
    }  
    
    private IndexSearcher getIndexSearcher() {
    	IndexSearcher search = null;
    	String key = indexDir.getPath();
        if (LuceneCacheHelper.exist(key)) {
            search = LuceneCacheHelper.get(key);
        } else {
        	IndexReader reader;
			try {
				reader = DirectoryReader.open(FSDirectory.open(indexDir));
				search = new IndexSearcher(reader);
				LuceneCacheHelper.insert(key, search);
			} catch (IOException e) {
				e.printStackTrace();
			} 
        }
        return search;
    }
}
