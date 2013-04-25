package mbean;


import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import javax.management.*;

import model.MongoDBConnection;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;

public class RemoteMessageListener implements NotificationListener {
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd - H:mm:ss");
	public static MongoDBConnection mdbc;

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
	        	Properties props = (Properties)notification.getUserData();
	        	String response = (String)props.get("attribute");
	            String response2 = (String)props.get("value");
	        	System.out.println("<<Remote>> "+notification.getType() + " number "+ notification.getSequenceNumber() + " in MBean " + notification.getSource() + " with attribute = "+ response + " value = "+response2 + " at "+ formatter.format(notification.getTimeStamp()));
	        	mdbc.setColl("atr_hst");
	        	ObjectId objid = new ObjectId(); 
				BasicDBObject doc = new BasicDBObject("atr_id", response).append("value", response2).append("tstamp", objid.getTime());
				mdbc.insert_doc(doc);
	        } catch (Exception e) {
	        	trace(e.toString());
	        }
    	}else
    		System.out.println("<<Remote>> "+notification.getType() + " in MBean " + notification.getSource() + " at "+ formatter.format(notification.getTimeStamp()));
    }
}