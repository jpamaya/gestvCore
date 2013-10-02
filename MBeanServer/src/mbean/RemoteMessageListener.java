package mbean;


import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import javax.management.*;

import model.MongoDBConnection;

import org.bson.types.ObjectId;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;

public class RemoteMessageListener implements NotificationListener {
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd - H:mm:ss");
	public static MongoDBConnection mdbc;
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public RemoteMessageListener() {
        super();
        try {
			mdbc=MongoDBConnection.getInstance();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    }

    private void trace (String message) {
            System.out.println(message);
    }
    
    public void handleNotification(Notification notification, Object handback) {
    	if(notification.getType().equals("jmx.attribute.change")){
	    	try {
	    		String mrid, maid;
	    		JsonElement jelement = new JsonParser().parse((String) handback);
	            JsonObject  jobject = jelement.getAsJsonObject();
	            mrid=jobject.get("mrid").toString();
	            maid=jobject.get("maid").toString();
	            mrid=mrid.replace("\"", "");
	            maid=maid.replace("\"", "");
	        	Properties props = (Properties)notification.getUserData();
	        	String atrname = (String)props.get("attribute");
	            String value = (String)props.get("value");
	            int timestamp = (int) notification.getTimeStamp();
	            
	            ObjectId aid;
	            aid=getAtrId(atrname, maid);
	        	mdbc.setColl("atr_hsts");
				BasicDBObject doc = new BasicDBObject("atr_id", aid).append("value", value).append("tstamp", timestamp);
				mdbc.insert_doc(doc);
	        	System.out.println("<<Remote>> "+notification.getType() + " number "+ notification.getSequenceNumber() + " in MBean " + notification.getSource() + " with attribute = "+ atrname + " value = "+value + " at "+ formatter.format(notification.getTimeStamp()));
	        } catch (Exception e) {
	        	trace(e.toString());
	        }
    	}else{
    		String mrid, maid;
    		JsonElement jelement = new JsonParser().parse((String) handback);
            JsonObject  jobject = jelement.getAsJsonObject();
            mrid=jobject.get("mrid").toString();
            maid=jobject.get("maid").toString();
            mrid=mrid.replace("\"", "");
            maid=maid.replace("\"", "");
    		mdbc.setColl("alrts");
            ObjectId objid = new ObjectId();
            int estampa=objid.getTimeSecond();
            String titulo=notification.getType();
			DBCollection coll = mdbc.getColl();
			BasicDBObject query = new BasicDBObject("title", titulo).append("tipo", "alarm").append("state", new BasicDBObject("$ne", "solved"));
			DBCursor cursor = coll.find(query);
			System.out.println("count="+cursor.count());
			if (cursor.count()==0){
	        	BasicDBObject doc = new BasicDBObject("man_rsc_id", new ObjectId(mrid)).append("mcr_atr_id", new ObjectId(maid)).append("tipo", "alarm").append("title", titulo).append("msg", notification.getMessage()).append("tstamp_ini", estampa).append("tstamp_last", estampa).append("count", 1).append("state", "noAtt");
				mdbc.insert_doc(doc);
			}else{
				DBObject searchQuery=cursor.next();
				DBObject modifier = new BasicDBObject("count", 1);
				DBObject incQuery = new BasicDBObject("$inc", modifier).append("$set", new BasicDBObject().append("tstamp_last", estampa));;
				coll.update(searchQuery, incQuery);
			}
			cursor.close();            
    		System.out.println("<<Remote>> "+notification.getType() + " in MBean " + notification.getSource() + " at "+ formatter.format(notification.getTimeStamp()));
    	}
    }

	public ObjectId getAtrId(String atrname, String maid){
    	ObjectId aid = null;
    	DBCollection coll;
		BasicDBObject query1;
		DBCursor cursor1;
		DBObject obj;
		DB db = mdbc.getDb();
		
    	coll = db.getCollection("atrs");
		query1 = new BasicDBObject("name", atrname).append("mcr_atr_id", new ObjectId(maid));
		cursor1 = coll.find(query1);
		try {
		   while(cursor1.hasNext()) {
			   obj=cursor1.next();
			   aid = (ObjectId) obj.get("_id");
		   }
		} finally {
		   cursor1.close();
		}
		return aid;
    }    
}