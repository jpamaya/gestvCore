package mbean;


import java.text.SimpleDateFormat;
import java.util.Properties;
import javax.management.*;

public class AttributeHistory implements NotificationListener {
	
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd - H:mm:ss");

    public AttributeHistory() {
        super();
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
	            String domain = (String)props.get("domain");
	            String type = (String)props.get("type");
	            String name = (String)props.get("name");
	        	System.out.println(notification.getType() + " number "+ notification.getSequenceNumber() + " in MBean " + domain + ":type=" + type + ",name="+name+ " with attribute = "+ response + " value = "+response2 + " at "+ formatter.format(notification.getTimeStamp()));
	        } catch (Exception e) {
	        	trace(e.toString());
	        }
    	}
    }
}