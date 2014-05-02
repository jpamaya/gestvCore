package model;

import javax.management.MBeanNotificationInfo;
import javax.management.monitor.Monitor;
import javax.management.monitor.MonitorMBean;
import javax.management.monitor.MonitorNotification;

public class ThresholdMonitor extends Monitor implements MonitorMBean {

    public boolean active=true;

    public synchronized boolean isActive(){
    	return active;
    }
    
    public MBeanNotificationInfo[] getNotificationInfo()
    {
      MBeanNotificationInfo[] result = new MBeanNotificationInfo[1];
      String[] types = new String[]
      {
        MonitorNotification.RUNTIME_ERROR,
        MonitorNotification.OBSERVED_OBJECT_ERROR,
        MonitorNotification.OBSERVED_ATTRIBUTE_ERROR,
        MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR,
        MonitorNotification.THRESHOLD_ERROR,
        MonitorNotification.THRESHOLD_VALUE_EXCEEDED
      };
      result[0] = new MBeanNotificationInfo(types,
        "javax.management.monitor.MonitorNotification",
        "Notifications sent by the Threshold Monitor Service MBean");
      return result;
    }

	@Override
	public synchronized void start() {
		
		while(active){
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("TIMEOUT 555555555555555 !");
		}
	}

	@Override
	public synchronized void stop() {
		active=false;
	}
	
}