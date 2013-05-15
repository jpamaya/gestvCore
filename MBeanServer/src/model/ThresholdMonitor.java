package model;

import javax.management.MBeanAttributeInfo;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.MBeanNotificationInfo;
import javax.management.monitor.CounterMonitorMBean;
import javax.management.monitor.Monitor;
import javax.management.monitor.MonitorNotification;

import static javax.management.monitor.MonitorNotification.*;

public class ThresholdMonitor extends Monitor implements CounterMonitorMBean {

    private Number threshold = new Double(0);
    private Number derivedGauge = new Double(0);
    private long derivedGaugeTimeStamp = 0;
    boolean notify = false;
    private Number lastValue = null;
    boolean differenceMode = false;
    Number offset = new Double(0);
    Number initialThreshold = new Double(0);
    Number modulus = new Double(0);
    int THRESHOLD_EXCEEDED_NOTIFIED = 16;
    int THRESHOLD_ERROR_NOTIFIED = 32;
    
    private static final String[] types = {
        RUNTIME_ERROR,
        OBSERVED_OBJECT_ERROR,
        OBSERVED_ATTRIBUTE_ERROR,
        OBSERVED_ATTRIBUTE_TYPE_ERROR,
        THRESHOLD_ERROR,
        THRESHOLD_VALUE_EXCEEDED
    };

    private static final MBeanNotificationInfo[] notifsInfo = {
        new MBeanNotificationInfo(
            types,
            "javax.management.monitor.MonitorNotification",
            "Notifications sent by the ThresholdMonitor MBean")
    };

    public ThresholdMonitor() {
    	dbgTag = "ThresholdMonitor";
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
	@Deprecated
	public Number getDerivedGauge() {
		return null;
	}

	@Override
	public Number getDerivedGauge(ObjectName object) {
		return null;
	}

	@Override
	@Deprecated
	public long getDerivedGaugeTimeStamp() {
		return 0;
	}

	@Override
	public long getDerivedGaugeTimeStamp(ObjectName object) {
		return 0;
	}

	@Override
	public boolean getDifferenceMode() {
		return false;
	}

	@Override
	public Number getInitThreshold() {
		return threshold;
	}

	@Override
	public Number getModulus() {
		return null;
	}

	@Override
	public boolean getNotify() {
		return false;
	}

	@Override
	public Number getOffset() {
		return null;
	}

	@Override
	@Deprecated
	public Number getThreshold() {
		return threshold;
	}

	@Override
	public Number getThreshold(ObjectName object) {
		return threshold;
	}

	@Override
	public void setDifferenceMode(boolean value) {
		
	}

	@Override
	public void setInitThreshold(Number value) throws IllegalArgumentException {
		threshold=value;
	}

	@Override
	public void setModulus(Number value) throws IllegalArgumentException {
		
	}

	@Override
	public void setNotify(boolean value) {
		
	}

	@Override
	public void setOffset(Number value) throws IllegalArgumentException {
		
	}

	@Override
	@Deprecated
	public void setThreshold(Number value) throws IllegalArgumentException {
		threshold=value;
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
		//this.stop();
	}
	
	// Package protected ---------------------------------------------

	  void monitor(MBeanAttributeInfo attributeInfo, Object value)
	    throws Exception
	  {
	    // Wrong type of attribute
	    if (!(value instanceof Byte) && !(value instanceof Integer) &&
	        !(value instanceof Short) && !(value instanceof Long))
	    {
	       //sendAttributeTypeErrorNotification("Attribute is not an integer type");
	       return;
	    }

	    // Cast the counter to a Number
	    Number number = (Number) value;

	    // Get the gauge and record when we got it.
	    derivedGauge = number;
	    derivedGaugeTimeStamp = System.currentTimeMillis();

	    // Fire the event if the threshold has been exceeded
	    if (derivedGauge.longValue() >= threshold.longValue())
	    {
	      if ((alreadyNotified & THRESHOLD_EXCEEDED_NOTIFIED) == 0)
	      {
	        sendThresholdExceededNotification(derivedGauge);
	        alreadyNotified |= THRESHOLD_EXCEEDED_NOTIFIED;

	        // Add any offsets required to get a new threshold
	        if (offset.longValue() != 0)
	        {
	          while(threshold.longValue() <= derivedGauge.longValue())
	            threshold = add(threshold, offset);
	          alreadyNotified &= ~THRESHOLD_EXCEEDED_NOTIFIED;
	        }
	      }
	    }
	    else
	    {
	      // Reset notfication when it becomes less than threshold
	      if (derivedGauge.longValue() < threshold.longValue() 
	          && offset.longValue() == 0)
	        alreadyNotified &= ~THRESHOLD_EXCEEDED_NOTIFIED;
	    }

	    // For difference mode, restart when the counter decreases
	    if (differenceMode == true && lastValue !=null && 
	        lastValue.longValue() > number.longValue())
	    {
	      threshold = initialThreshold;
	      alreadyNotified &= ~THRESHOLD_EXCEEDED_NOTIFIED;
	    }

	    // For normal mode, restart when modulus exceeded
	    if (differenceMode == false && modulus.longValue() != 0 &&
	        number.longValue() >= modulus.longValue())
	    {
	      threshold = initialThreshold;
	      alreadyNotified &= ~THRESHOLD_EXCEEDED_NOTIFIED;
	    }

	    // Remember the last value
	    lastValue = number;
	  }

	  /**
	   * Get zero for the type passed.
	   * 
	   * @param the reference object
	   * @return zero for the correct type
	   */
	  Number getZero(Number value)
	  {
	     if (value instanceof Byte)
	       return new Byte((byte) 0);
	     if (value instanceof Integer)
	       return new Integer(0);
	     if (value instanceof Short)
	       return new Short((short) 0);
	     return new Long(0);
	  }

	  /**
	   * Add two numbers together.
	   * @param value1 the first value.
	   * @param value2 the second value.
	   * @return value1 + value2 of the correct type
	   */
	  Number add(Number value1, Number value2)
	  {
	     if (value1 instanceof Byte)
	       return new Byte((byte) (value1.byteValue() + value2.byteValue()));
	     if (value1 instanceof Integer)
	       return new Integer(value1.intValue() + value2.intValue());
	     if (value1 instanceof Short)
	       return new Short((short) (value1.shortValue() + value2.shortValue()));
	     return new Long(value1.longValue() + value2.longValue());
	  }

	  /**
	   * Subtract two numbers.
	   * @param value1 the first value.
	   * @param value2 the second value.
	   * @return value1 - value2 of the correct type
	   */
	  Number sub(Number value1, Number value2)
	  {
	     if (value1 instanceof Byte)
	       return new Byte((byte) (value1.byteValue() - value2.byteValue()));
	     if (value1 instanceof Integer)
	       return new Integer(value1.intValue() - value2.intValue());
	     if (value1 instanceof Short)
	       return new Short((short) (value1.shortValue() - value2.shortValue()));
	     return new Long(value1.longValue() - value2.longValue());
	  }

	  /**
	   * Send a threshold exceeded event.<p>
	   *
	   * This is only performed when requested and it has not already been sent.
	   *
	   * @param value the attribute value.
	   */
	  void sendThresholdExceededNotification(Object value)
	  {
	    if (notify)
	    {
	    	Notification notif = null;
	    	notif= new Notification(THRESHOLD_VALUE_EXCEEDED, getObservedAttribute(), 0, 0, "threshold exceeded");
	    	sendNotification(notif);
	      //sendNotification(MonitorNotification.THRESHOLD_VALUE_EXCEEDED, derivedGaugeTimeStamp, "threshold exceeded", getObservedAttribute(), value, threshold);
	    }
	  }

	  /**
	   * Send a threshold error event.<p>
	   *
	   * This is only performed when requested and it has not already been sent.
	   *
	   * @param value the attribute value.
	   */
	  void sendThresholdErrorNotification(Object value)
	  {
	    /*if ((alreadyNotified & THRESHOLD_ERROR_NOTIFIED) == 0)
	    {
	      sendNotification(MonitorNotification.THRESHOLD_ERROR, derivedGaugeTimeStamp, "Threshold, offset or modulus not the correct type", getObservedAttribute(), null, null);
	      alreadyNotified |= THRESHOLD_ERROR_NOTIFIED;
	    }*/
	  }
	
}