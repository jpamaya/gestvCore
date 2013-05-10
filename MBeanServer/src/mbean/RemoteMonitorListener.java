package mbean;


import java.net.UnknownHostException;

import javax.management.*;
import javax.management.monitor.*;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;

import model.MongoDBConnection;

public class RemoteMonitorListener implements NotificationListener {
	
	public static MongoDBConnection mdbc;

    public RemoteMonitorListener() {
        super();
        try {
			mdbc=MongoDBConnection.getInstance();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
    }

    public void handleNotification(Notification notification, Object handback) {
        MonitorNotification notif = (MonitorNotification)notification;
        String object=notif.getObservedObject().toString();
        String attribute=notif.getObservedAttribute();
        String type = notification.getType();
        try {
        	String msg = null,title = null;
            if (type.equals(MonitorNotification.THRESHOLD_VALUE_EXCEEDED)) {
            	//System.out.println(handback);
            	title="THRESHOLD_VALUE_EXCEEDED";
            	msg=attribute + " from " + object + " has reached the threshold";
                System.out.println("<<Remote>> " + attribute + " from " + object + " has reached the threshold\n en monitor "+handback);
            }
            else if (type.equals(MonitorNotification.THRESHOLD_HIGH_VALUE_EXCEEDED)) {
            	title="THRESHOLD_HIGH_VALUE_EXCEEDED";
            	msg=attribute + " from " + object + " has reached the High threshold";
                System.out.println("<<Remote>> " + attribute + " from " + object + " has reached the High threshold\n");
            }
            else if (type.equals(MonitorNotification.THRESHOLD_LOW_VALUE_EXCEEDED)) {
            	title="THRESHOLD_LOW_VALUE_EXCEEDED";
            	msg=attribute + " from " + object + " has reached the Low threshold";
                System.out.println("<<Remote>> " + attribute + " from " + object + " has reached the Low threshold\n");
            }
            else if (type.equals(MonitorNotification.STRING_TO_COMPARE_VALUE_MATCHED)) {
            	title="STRING_TO_COMPARE_VALUE_MATCHED";
            	msg=attribute + " from " + object + " matches the compare value";
                System.out.println("<<Remote>> " + attribute + " from " + object + " matches the compare value\n");
            }     
            else if (type.equals(MonitorNotification.STRING_TO_COMPARE_VALUE_DIFFERED)) {
            	title="STRING_TO_COMPARE_VALUE_DIFFERED";
            	msg=attribute + " from " + object + " differs the compare value";
                System.out.println("<<Remote>> " + attribute + " from " + object + " differs the compare value\n");
            }
            mdbc.setColl("alrts");
        	//ObjectId objid = new ObjectId(); 
			BasicDBObject doc = new BasicDBObject("atr_id", attribute).append("tipo", "anmly").append("title", title).append("msg", msg);
			mdbc.insert_doc(doc);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}