package javax.management.monitor;


/**
 * This interface exposes the remote management interface of the counter monitor MBean.
 */
public abstract interface CounterMonitorMBean1 extends MonitorMBean
{
  /**
   * This method gets the value of the derived gauge.
   *
   * @return An instance of java.lang.Number giving the value of the derived gauge.
   */
  public Number getDerivedGauge();

  /**
   * This method gets the value of the derived gauge time stamp.It is the
   * time when the notification is triggered nearest to the milliseconds.
   *
   * @return The value of the derived gauge time Stamp.
   */
  public long getDerivedGaugeTimeStamp();

  /**
   * This method gets the difference mode flag value.
   *
   * @return true if the flag is on , false otherwise.
   */
  public boolean getDifferenceMode();

  /**
   * This method sets the difference mode flag value.
   *
   * @param true if the difference mode is used, false otherwise.
   */
  public void setDifferenceMode(boolean value);

  /**
   * This method gets the modulus value.
   *
   * @return An instance of java.lang.Number giving the modulus value.
   */
  public Number getModulus();

  /**
   * This method sets the modulus value .
   *
   * @param value An instance of java.lang.Number which is the modulus value.
   *
   * @exception java.lang.IllegalArgumentException - The specified modulus is
   *         null or the modulus value is less than zero.
   */
  public void setModulus(Number value) throws IllegalArgumentException;

  /**
   * This method gets the notification's on/off switch value.
   *
   * @return true if the counter monitor notifies when exceeding the
   *         threshold, false otherwise.
   */
  public boolean getNotify();

  /**
   * This method sets the notification's on/off switch value.
   *
   * @param value The notification's on/off switch value.
   */
  public void setNotify(boolean value);

  /**
   * This method gets offset value .
   *
   * @return An instance of java.lang.Number giving the offset value .
   */
  public Number getOffset();

  /**
   * This method sets the offset value .
   *
   * @param value The offset value.
   */
  public void setOffset(Number value) throws IllegalArgumentException;

  /**
   * This method gets the threshold value .
   *
   * @return An instance of java.lang.Number giving the threshold .
   */
  public Number getThreshold();

  /**
   * This method sets the threshold value .
   *
   * @param value The Threshold Value.
   *
   * @exception java.lang.IllegalArgumentException - The specified threshold
   *         is null or the threshold value is less than zero.
   */
  public void setThreshold(Number value) throws IllegalArgumentException;
}