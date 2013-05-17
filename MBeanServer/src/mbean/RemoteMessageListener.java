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
	        	String response = (String)props.get("attribute");
	            String response2 = (String)props.get("value");
	            ObjectId aid;
	            aid=getAtrId(response, maid);
	        	mdbc.setColl("atr_hst");
	        	ObjectId objid = new ObjectId(); 
				BasicDBObject doc = new BasicDBObject("atr_id", aid).append("value", response2).append("tstamp", objid.getTime());
				mdbc.insert_doc(doc);
	        	System.out.println("<<Remote>> "+notification.getType() + " number "+ notification.getSequenceNumber() + " in MBean " + notification.getSource() + " with attribute = "+ response + " value = "+response2 + " at "+ formatter.format(notification.getTimeStamp()));				
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
        	BasicDBObject doc = new BasicDBObject("man_rsc_id", new ObjectId(mrid)).append("mcr_atr_id", new ObjectId(maid)).append("tipo", "alarm").append("title", notification.getType()).append("msg", notification.getMessage()).append("tstamp", objid.getTime());
			mdbc.insert_doc(doc);
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