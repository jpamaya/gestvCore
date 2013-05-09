package model;

import java.net.UnknownHostException;
import java.util.Set;

//import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
//import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

public class MongoDBConnection {
	private static MongoDBConnection instance;
	private MongoClient mc;
	private DB db;
	private DBCollection coll;
	
	private MongoDBConnection() throws UnknownHostException{
		mc = new MongoClient();
		db = mc.getDB("webgestv_development");
	}
	
	public DB getDb(){
		return db;
	}
	
	public void setColl(String collection){
		coll = db.getCollection(collection);
	}
	
	public Set<String> getAllColls(){
		Set<String> colls = db.getCollectionNames();
		return colls;
	}
	
	public static MongoDBConnection getInstance() throws UnknownHostException{
		if(instance==null){
			instance = new MongoDBConnection();
		}
		return instance;
	}
	
	public WriteResult insert_doc(BasicDBObject dbo){
		return coll.insert(dbo);
	}
	
	public DBCursor getAllDocs(){
		return coll.find();
	}
	
	public void see_all_doc_by_coll(){
		DBCursor c = getAllDocs();
		try {
			while (c.hasNext()) {
				System.out.println(c.next());
			}
		} finally {
			c.close();
		}
	}
	public void remove_all_doc_by_coll(){
		DBCursor c = getAllDocs();
		try {
			while (c.hasNext()) {
				coll.remove(c.next());
			}
		} finally {
			c.close();
		}
	}
	@SuppressWarnings("finally")
	public BasicDBObject get_document_by_key(String key, Object value){
		DBCursor cursor = coll.find(new BasicDBObject(key,value));
		BasicDBObject dbo = new BasicDBObject();
        try {
            while(cursor.hasNext()) {
            	dbo = (BasicDBObject) cursor.next();
                System.out.println(dbo);
                
            }
        } finally {
            cursor.close();
            return dbo;
        }
	}
}
