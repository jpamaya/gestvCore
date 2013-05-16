package mbean;


import java.net.UnknownHostException;

import javax.management.*;
import javax.management.monitor.*;

import org.bson.types.ObjectId;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

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
    	String msg = "",title = "";
        try {
            if (type.equals(MonitorNotification.THRESHOLD_VALUE_EXCEEDED)) {
            	//System.out.println(handback);
            	title="THRESHOLD_VALUE_EXCEEDED";
            	msg=attribute + " from " + object + " has reached the threshold";
            	publicar(title, msg, (String) handback);
                System.out.println("<<Remote>> " + attribute + " from " + object + " has reached the threshold\n en monitor "+handback);
            }
            else if (type.equals(MonitorNotification.THRESHOLD_HIGH_VALUE_EXCEEDED)) {
            	title="THRESHOLD_HIGH_VALUE_EXCEEDED";
            	msg=attribute + " from " + object + " has reached the High threshold";
            	publicar(title, msg, (String) handback);            	
                System.out.println("<<Remote>> " + attribute + " from " + object + " has reached the High threshold\n");
            }
            else if (type.equals(MonitorNotification.THRESHOLD_LOW_VALUE_EXCEEDED)) {
            	title="THRESHOLD_LOW_VALUE_EXCEEDED";
            	msg=attribute + " from " + object + " has reached the Low threshold";
            	publicar(title, msg, (String) handback);            	
                System.out.println("<<Remote>> " + attribute + " from " + object + " has reached the Low threshold\n");
            }
            else if (type.equals(MonitorNotification.STRING_TO_COMPARE_VALUE_MATCHED)) {
            	title="STRING_TO_COMPARE_VALUE_MATCHED";
            	msg=attribute + " from " + object + " matches the compare value";
            	publicar(title, msg, (String) handback);            	
                System.out.println("<<Remote>> " + attribute + " from " + object + " matches the compare value\n");
            }     
            else if (type.equals(MonitorNotification.STRING_TO_COMPARE_VALUE_DIFFERED)) {
            	title="STRING_TO_COMPARE_VALUE_DIFFERED";
            	msg=attribute + " from " + object + " differs the compare value";
            	publicar(title, msg, (String) handback);            	
                System.out.println("<<Remote>> " + attribute + " from " + object + " differs the compare value\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    public void publicar(String title, String msg, String handback){
        mdbc.setColl("alrts");
        JsonElement jelement = new JsonParser().parse((String) handback);
        JsonObject  jobject = jelement.getAsJsonObject();
        String mt=jobject.get("montype").toString();
        String montype="anmly";
        if (mt.equals("qos"))
        	montype="anmly";
        else
        	montype="alarm";
        String atrid=jobject.get("atrid").toString();
        atrid=atrid.replace("\"", "");
		BasicDBObject doc = new BasicDBObject("atr_id", new ObjectId(atrid)).append("tipo", montype).append("title", title).append("msg", msg);
		mdbc.insert_doc(doc);
    }
}