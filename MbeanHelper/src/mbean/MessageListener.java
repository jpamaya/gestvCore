package mbean;


import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import javax.management.*;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;

import model.MongoDBConnection;

public class MessageListener implements NotificationListener {
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd - H:mm:ss");
	public MBeanServer mbeanServer;
	//public static String filename = "Webservices";
	public static MongoDBConnection mdbc;

    public MessageListener() {
        super();
    	try {
			mdbc=MongoDBConnection.getInstance();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    }

    public void trace (String message) {
            System.out.println(message);
    }
    
    public void handleNotification(Notification notification, Object handback) {
    	System.out.println("NOTIFICATION");
    	if(notification.getType().equals("jmx.attribute.change")){
	        try {
	        	Properties props = (Properties)notification.getUserData();
	        	String response = (String)props.get("attribute");
	            String response2 = (String)props.get("value");
	        	System.out.println(notification.getType() + " number "+ notification.getSequenceNumber() + " in MBean " + notification.getSource() + " with attribute = "+ response + " value = "+response2 + " at "+ formatter.format(notification.getTimeStamp()));
	        	mdbc.setColl("attr_hst");
	        	ObjectId objid = new ObjectId(); 
				BasicDBObject doc = new BasicDBObject("atr_id", response).append("value", response2).append("tstamp", objid.getTime());
				mdbc.insert_doc(doc);
	        } catch (Exception e) {
	        	trace(e.toString());
	        }
    	}else
    		System.out.println(notification.getType() + " in MBean " + notification.getSource() + " at "+ formatter.format(notification.getTimeStamp()));
    }
}