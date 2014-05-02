package javax.management.monitor;

import javax.management.ObjectName;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.ReflectionException;

public class CounterMonitor1 extends Monitor implements CounterMonitorMBean1
{
    /**
     * Derived gauge.
     */
  private Number derivedGauge = null;

    /**
     * Counter offset.
     * <BR>The default value is a null Integer object.
     */
  private Number offset = null;

    /**
     * Counter threshold.
     * <BR>The default value is a null Integer object.
     */
  public Number  threshold = null;

    /**
     * Flag indicating if the counter difference mode is used. If the counter
   * difference mode is used, the derived gauge is the difference between
   * two consecutive observed values. Otherwise, the derived gauge is
   * directly the value of the observed attribute.
     * <BR>The default value is set to <CODE>false</CODE>.
     */
  private boolean differenceMode = false;

    /**
     * Flag indicating if the counter monitor notifies when exceeding the threshold.
     * <BR>The default value is set to <CODE>true</CODE>.
     */
  private boolean notifyFlag = true;

    /**
     * Counter modulus.
     * <BR>The default value is a null Integer object.
     */
  private Number modulus = null;

    /**
     * Previous derived gauge value.
     */
  private Number prevDerivedGauge = null;

  private Thread counterThread        = null;
  private Comparable comparableDerivedGauge    = null;
  private Notification notif                  = null;
  private Number thresholdCopy                = null;
  private Comparable comparableThreshold      = null;
    private Comparable comparableThresholdExt    = null;
  private long sequenceNumber                 = 1;
    private long startDate                      = 0;
    ObjectName objName                  = null;
    private boolean prev_notified = false;
    ObjectName observedObject;
		String observedAttribute;
	private boolean isActive;
	MBeanServer server;

    /**
     * Default Constructor.
     */
    public CounterMonitor1()
  {
      createLogger();
    }

    /**
     * This method gets the value of the derived gauge. The derived gauge is
   * either the exact value of the observed attribute , or the difference
   * between the two consecutive observed values of the attribute.
     *
     * @return An instance of java.lang.Number giving the value of the derived gauge.
     */
    public java.lang.Number getDerivedGauge()
  {
      return derivedGauge;
    }

    /**
     * This method gets the value of the derived gauge time stamp.The derived
   * gauge time stamp is the value(in the nearest miliseconds) when the
   * notification was triggered.
     *
     * @return long value representing the time the notification was triggered.
     */
    public long getDerivedGaugeTimeStamp()
  {
      return (Long) derivedGauge;
    }

    /**
     * This method gets the value of the difference mode. If the difference mode
   * is true, the difference mode option is set to calculate the value of
   * the derived gauge.
     *
     * @return boolean value indicating whether the difference mode option is set.
     */
    public boolean getDifferenceMode()
  {
        return differenceMode;
    }

    /**
     * This method sets the state of the difference mode.
     *
     * @param value boolean value representing the state of the difference mode.
     */
    public void setDifferenceMode(boolean value)
  {
      this.differenceMode = value;
    }

    /**
     * This method gets the modulus value of monitor. Modulus is the value at
   * which the counter is reset to zero.
   *
     * @return An instance of java.lang.Number giving the modulus value.
     */
    public java.lang.Number getModulus()
  {
      return modulus;
    }

    /**
     * This method sets the modulus value of the monitor. Modulus is the value
   * at which the counter is reset to zero.
   *
     * @param value An instance of java.lang.Number representing the modulus
     *         value for the derived gauge .
     * @exception java.lang.IllegalArgumentException - The specified modulus is
   *         null or the modulus value is less than zero.
     */
    public void setModulus(java.lang.Number value) throws IllegalArgumentException
  {
        if(value == null || isLessThanZero(value))
                throw new IllegalArgumentException(
                  "Modulus cannot be null or less than Zero");

        this.modulus = value;
    }

    /**
     * This method gets  the notification's on/off switch value.
     *
     * @return a boolean value indicating the state of notification's On/Off switch.
     */
    public boolean getNotify()
  {
       return notifyFlag;
    }

    /**
     * This method sets the notification's on/off switch value.
     *
     * @param value boolean value to set the sate of the notifications On/Off Switch.
     */
    public void setNotify(boolean value)
  {
      this.notifyFlag = value;
    }

    /**
     * This method gets the offset value. Offset enables particular counting
   * intervals to be detected.
     *
     * @return An instance of java.lang.Number giving the value of the offset.
     */
    public Number getOffset()
  {
    return offset;
    }

    /**
     * This method sets the value of the offset.
     *
     * @param value An instance of java.lang.Number giving the value offset.
     *
     * @exception java.lang,IllegalArgumentException - The specified offset is
   *         null or the offset value is less than zero.
     */
    public void setOffset(java.lang.Number value) throws IllegalArgumentException
  {
        if(value == null || isLessThanZero(value))
                throw new IllegalArgumentException(
                  "Offset cannot be null or less than Zero");

        this.offset = value;
    }

    /**
     * This method gets the value of the threshold. Threshold is the maximum
   * value of that the attribute can reach. If the attribute reaches this
   * comparison level or is greater than this level , notification is triggered.
     *
     * @return An instance of java.lang.Number giving the value of the threshold.
     */
    public java.lang.Number getThreshold()
  {
      return threshold;
    }

    /**
     * This method sets the value of the threshold.Threshold is the maximum
   * value of that the attribute can reach. If the attribute reaches this
   * comparison level or is greater than this level , notification is triggered.
     *
     * @param value An instance of java.lang.Number which is the value of the threshold.
     */
    public void setThreshold(java.lang.Number value) throws IllegalArgumentException
    {
        if(value == null || isLessThanZero(value))
                throw new IllegalArgumentException("Threshold cannot be null or zero");

        this.threshold = value;
        thresholdCopy = value;
        comparableThreshold = getComparableValue(value);
        comparableThresholdExt = comparableThreshold;
    }

    /**
     * This method starts the counter monitor.
     */
    public void start()
  {
        counterThread  = new Thread(new CounterThread1(this));
        isActive = true;
        counterThread.setName("CounterMonitorThread-" + counterThread.getName());
        startDate = System.currentTimeMillis();
        counterThread.start();
    }

    /**
     * This method stops the counter monitor.
     */
    public void stop()
  {
        isActive = false;
        counterThread.stop();
    }

    /**
     * Overriding the preRegister() of MBeanRegistration interface to store the
     * ObjectName locally.
     */
    public ObjectName preRegister(MBeanServer server, ObjectName name)
                                        throws Exception
  {
    	this.server=server;
      this.objName = name;
        super.preRegister(server,name);
        return name;
  }

    /**
     * This method allows the monitor MBean to perform any operations it needs
   * before being de-registered by the MBean server.
     * Stops the monitor.
     */
    public void preDeregister() throws Exception
  {
    }

    /**
     * This method allows the monitor MBean to perform any operations needed
   * after having been registered in the MBean server or after the
   * registration has failed.
     *
     * Not used in this context.
     *
     * @param registrationDone If registration is done it is true otherwise false
     */
    public void postRegister(Boolean registrationDone)
  {
    }

    /**
     * This method allows the monitor MBean to perform any operations needed
   * after having been de-registered by the MBean server.
     * Not used in this context.
     */
    public void postDeregister()
  {
    }

    /**
     * This method returns a NotificationInfo object containing the name of
   * the Java class of the notification and the notification types sent by
   * the counter monitor.
   *
     * @return An Array of MBeanNotificationInfo objects.
     */
    public MBeanNotificationInfo[] getNotificationInfo()
  {
    MBeanNotificationInfo[] notifInfo = super.getNotificationInfo();

    MBeanNotificationInfo[] newNotifInfo =
              new MBeanNotificationInfo[notifInfo.length+2];

    for(int i=0;i<notifInfo.length;i++)
    {
      newNotifInfo[i] = notifInfo[i];
    }

    String[] types = {"jmx.monitor.error.threshold"};
    newNotifInfo[newNotifInfo.length - 2] = new MBeanNotificationInfo(types,"Monitor Threshold Error Notification","Emimitted for a error case in Threshold");

    String[] types1 = {"jmx.monitor.counter.threshold"};
    newNotifInfo[newNotifInfo.length - 1] = new MBeanNotificationInfo(types1,"Monitor Threshold CrossOver Notification","Emimitted for a CrossOver in monitored attribute");

    return newNotifInfo;
  }

  //----------------------------- Private methods -------------------------//

    // This is a private method that determines if the observed Attribute is of a counter
    // type .(i.e) byte, short , int, long. returns true if the attribute is of counter
    // monitor type else returns false.
    private boolean isCounterDataType(Object object)
  {
        String type = object.getClass().getName();

        if(type.equals("java.lang.Byte"))
                return true;
        else if(type.equals("java.lang.Integer"))
                return true;
        else if(type.equals("java.lang.Short"))
                return true;
        else if(type.equals("java.lang.Long"))
                return true;
        else
                return false;
    }

  // This is a private method that takes a instance of java.lang.Number and
  // returns a instance of Comparable with which the Number can be played
  // with. Only CounterMonitor type has been considered.
  private Comparable getComparableValue(Object object)
  {
      Comparable toRet = null;
      String type = object.getClass().getName();

      if(type.equals("java.lang.Byte"))
              toRet = (Byte)object;
      else if(type.equals("java.lang.Integer"))
              toRet = (Integer)object;
      else if(type.equals("java.lang.Short"))
              toRet = (Short)object;
      else if(type.equals("java.lang.Long"))
              toRet = (Long)object;

      return toRet;
  }

  private boolean isLessThanZero(Object object)
  {
      String type = object.getClass().getName();

      if(type.equals("java.lang.Byte"))
      {
      if(((Byte)object).byteValue() < 0 )
          return true;
      }
      else if(type.equals("java.lang.Integer"))
      {
      if(((Integer)object).intValue() < 0 )
          return true;
      }
      else if(type.equals("java.lang.Short"))
      {
      if(((Short)object).shortValue() < 0 )
          return true;
      }
      else if(type.equals("java.lang.Long"))
      {
      if(((Long)object).longValue() < 0 )
          return true;
      }

      return false;
  }

    // This is  a private method that calculates the value of the derived gauge
    // in case the difference mode is set. Derived gauge is the difference between two
    // consecutive observed values. This returns an instance of Comparable so that
    // it can be compared with the Threshold values.(also converted to a Comparable).
  private Comparable calculateDerivedGauge(Object derived,Object prevDerived)
  {
      Comparable toRet = null;
      String type = derived.getClass().getName();

      if(type.equals("java.lang.Byte"))
    {
      /*
      if((startDate+2*granularityPeriod) < System.currentTimeMillis())
        return new Byte((byte)0);
      */
      byte b = (byte)((((Byte)derived).byteValue() ) - (((Byte)prevDerived).byteValue()));

      if(b < 0 )
      {
        if(modulus != null)
        {
          b = (byte)(b + ((Byte)getComparableValue(modulus)).byteValue());
        }
        else
        {
          b = 0;
        }
      }

      toRet = new Byte(b) ;
      }
      else if(type.equals("java.lang.Integer"))
    {
      /*
      if((startDate+2*granularityPeriod) < System.currentTimeMillis())
        return new Integer(0);
      */
      int i = (((Integer)derived).intValue() ) - (((Integer)prevDerived).intValue());
      if(i < 0)
      {
        if(modulus != null)
        {
          i = i + ((Integer)getComparableValue(modulus)).intValue();
        }
        else
        {
          i=0;
        }
      }

      toRet = new Integer(i) ;
      }
      else if(type.equals("java.lang.Short"))
    {
      /*
      if((startDate+2*granularityPeriod) < System.currentTimeMillis())
        return new Short((short)0);
      */
      short s = (short)((((Short)derived).shortValue() ) - (((Short)prevDerived).shortValue()));
      if(s < 0)
      {
        if(modulus != null)
        {
          s = (short)(s + ((Short)getComparableValue(modulus)).shortValue());
        }
        else
        {
          s = 0;
        }
      }
      toRet = new Short(s) ;
      }
      else if(type.equals("java.lang.Long"))
    {
      /*
      if((startDate+2*granularityPeriod) < System.currentTimeMillis())
        return new Long(0);
      */
      long l = (((Long)derived).longValue() ) - (((Long)prevDerived).longValue());
      if(l < 0 )
      {
        if(modulus != null)
        {
          l = l + ((Long)getComparableValue(modulus)).longValue();
        }
        else
        {
          l =0;
        }
      }
      toRet = new Long(l) ;
      }

      return toRet;
  }

    // This is a private method that adds the value of the threshold and offset,
  // in case offset is set.
    private Comparable addThresholdAndOffset(Object threshold,Object offset)
  {
        Comparable toRet = null;
        String type = threshold.getClass().getName();

        if(type.equals("java.lang.Byte")){
            toRet = new Byte((byte)((((Byte)threshold).byteValue() )+ (((Byte)offset).byteValue()))) ;
        }
        else if(type.equals("java.lang.Integer")){
            toRet = new Integer((((Integer)threshold).intValue() )+ (((Integer)offset).intValue())) ;
        }
        else if(type.equals("java.lang.Short")){
            toRet = new Short((short)((((Short)threshold).shortValue() )+ (((Short)offset).shortValue()))) ;
        }
        else if(type.equals("java.lang.Long")){
      toRet = new Long((((Long)threshold).longValue() )+ (((Long)offset).longValue())) ;
        }

        return toRet;
    }

    private void checkForRollOver(Number derivedGauge)
  {
            //To be done
    }

  private void createLogger()
  {
    try
    {
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }


  //------------------------------ Inner class ----------------------------//

    // This is an inner class that implements runnable and runs in a separate
  // thread when the counter is started. It polls the value of the observed
  // attribute for the time specified by the granularity  period.
  class CounterThread1 implements Runnable
  {
      Object obj = null;
      Number prevDerivedGauge = null;
      Number storedDerivedGauge = null;
      CounterMonitor1 monitor = null;
      boolean isRollOver = false;


      CounterThread1(CounterMonitor1 counterMonitor1)
    {
        this.monitor = counterMonitor1;
      }

      public void run()
    {
      while(true)
      {

		try
        {
			System.out.println("oa="+observedAttribute+"oo="+observedObject);
			System.out.println(server.getMBeanCount());
            obj = server.getAttribute(observedObject,observedAttribute);
        	System.out.println((Integer)obj);
        	System.out.println((Integer)threshold);
            if((Integer)obj>(Integer)threshold){
                sendNotification(new MonitorNotification(
                        MonitorNotification.THRESHOLD_VALUE_EXCEEDED,
                        monitor, sequenceNumber++, 123456789L,"message", observedObject, observedAttribute,observedAttribute
                        , null));
                
                //sendNotification(notification);
            }
            derivedGauge = System.currentTimeMillis();
        }
        catch(InstanceNotFoundException ine)
        {
            if(Monitor.OBSERVED_OBJECT_ERROR_NOTIFIED == 1)
            {
                sendNotification(new MonitorNotification(
                        MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR,
                        monitor, sequenceNumber++, 123456789L,"message", observedObject, observedAttribute,observedAttribute
                        , null));
            }
            sleep();
            continue;
        }
        catch(AttributeNotFoundException ane)
        {
          if(Monitor.OBSERVED_ATTRIBUTE_ERROR_NOTIFIED == 1)
          {
              sendNotification(new MonitorNotification(
                      MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR,
                      monitor, sequenceNumber++, 123456789L,"message", observedObject, observedAttribute,observedAttribute
                      , null));
          }
          sleep();
          continue;
        }
        catch(MBeanException mbe)
        {
            if(Monitor.RUNTIME_ERROR_NOTIFIED == 1)
          {
                sendNotification(new MonitorNotification(
                        MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR,
                        monitor, sequenceNumber++, 123456789L,"message", observedObject, observedAttribute,observedAttribute
                        , null));
          }
            sleep();
            continue;
        }
        catch(ReflectionException re)
        {
            if(Monitor.RUNTIME_ERROR_NOTIFIED == 1)
            {
                sendNotification(new MonitorNotification(
                        MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR,
                        monitor, sequenceNumber++, 123456789L,"message", observedObject, observedAttribute,observedAttribute
                        , null));
            }
            sleep();
            continue;
        }
        catch(Exception e)
        {
             if(Monitor.RUNTIME_ERROR_NOTIFIED == 1)
             {
                 sendNotification(new MonitorNotification(
                         MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR,
                         monitor, sequenceNumber++, 123456789L,"message", observedObject, observedAttribute,observedAttribute
                         , null));
             }
             sleep();
             continue;
        }

        // The following if loop checks if the observedAttribute type
        // is that of a counterMonitor Type. If it is not, sends a
        // notification and throws a monitorsettingexception.
        if(obj == null)
        {
            continue;
        }

        if(! isCounterDataType(obj))
        {
            sendNotification(new MonitorNotification(
              MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR,
              monitor, sequenceNumber++, 123456789L,"message", observedObject, observedAttribute,observedAttribute
              , null));
            //throw new MonitorSettingException();
        }
        else
        {

            derivedGauge = (Number)obj;
            comparableDerivedGauge = getComparableValue(derivedGauge);

            if(isRollOver)
          {
            if(getComparableValue(storedDerivedGauge).compareTo(comparableDerivedGauge) >0)
            {
                comparableThresholdExt =  getComparableValue(threshold);
                //derivedGauge = new Integer(0);
                isRollOver = false;
            }
            }

            if(differenceMode)
          {
            if(prevDerivedGauge == null)
            {
              prevDerivedGauge = derivedGauge;
            }

            if(derivedGauge.longValue() < prevDerivedGauge.longValue())
            {
                comparableThresholdExt = getComparableValue(threshold);
            }

            derivedGauge = (Number)(calculateDerivedGauge(derivedGauge,prevDerivedGauge));

            comparableDerivedGauge = getComparableValue(derivedGauge);
            checkForRollOver(derivedGauge);
            }

            if(comparableDerivedGauge.compareTo(comparableThresholdExt) >= 0)
          {
            notif =             new MonitorNotification(
                    MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR,
                    monitor, sequenceNumber++, 123456789L,"message", observedObject, observedAttribute,observedAttribute
                    , null);

                  if(notifyFlag && !prev_notified)
            {
                        sendNotification(notif);
                        prev_notified = true;
            }

            if(offset != null)
            {
              if(!(offset.getClass().getName().equals(obj.getClass().getName())))
              {
                  sendNotification(new MonitorNotification(
                          MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR,
                          monitor, sequenceNumber++, 123456789L,"message", observedObject, observedAttribute,observedAttribute
                          , null));
                throw new MonitorSettingException();
              }

              if(offset.intValue() >0)
              {
                while(comparableThresholdExt.compareTo(comparableDerivedGauge) <= 0)
                {
                  comparableThresholdExt = addThresholdAndOffset(comparableThresholdExt,offset);
                }
              }
            }

            if(modulus != null && modulus.intValue() >0)
            {
              if(!(offset.getClass().getName().equals(obj.getClass().getName())))
              {
                  sendNotification(new MonitorNotification(
                          MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR,
                          monitor, sequenceNumber++, 123456789L,"message", observedObject, observedAttribute,observedAttribute
                          , null));
                throw new MonitorSettingException();
              }

              Comparable comparableModulus = getComparableValue(modulus);

              if(modulus instanceof Byte)
              {
              }

              try
              {
                if(comparableThresholdExt.compareTo(comparableModulus) >0)
                {
                  //derivedGauge = new Integer(0);
                  isRollOver = true;
                  storedDerivedGauge = derivedGauge;
                  //comparableThresholdExt =  getComparableValue(threshold);
                }
              }
              catch(ClassCastException cce)
              {
              //do nothing
              //Should I throw a MonitorSettingException();
              }
            }
            }
            else
          {
            prev_notified = false;
          }

            try
          {
              prevDerivedGauge = (Number)obj;
                Thread.sleep(getGranularityPeriod());
            }
          catch(InterruptedException ie)
          {
              //ie.printStackTrace();
            }
        }
      }
      }

      private void sleep()
      {
      try
      {
          Thread.sleep(getGranularityPeriod());
      }
      catch(Exception e)
      {
      }
      }

  }//End the CounterMonitor Thread class.


public String getObservedAttribute() {
	return observedAttribute;
}

public void setObservedAttribute(String observedAttribute) {
	this.observedAttribute = observedAttribute;
}

public ObjectName getObservedObject() {
	return observedObject;
}

public void setObservedObject(ObjectName observedObject) {
	this.observedObject = observedObject;
}

}//End the CounterMonitor class.
