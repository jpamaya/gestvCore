package model;

import javax.management.monitor.Monitor;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class MyThresholdMonitor implements MyMonitor {


	@Element
	private String attribute;
	@Element
	private Long period;
	@Element
	private Double threshold;
	@Element
	private Double offset;
	@Attribute
	private String name;
	private String type;
	
	public MyThresholdMonitor() {

	}
	
	public String getAttribute() {
		return attribute;
	}

	public Long getPeriod() {
		return period;
	}
	public void setPeriod(Long period) {
		this.period = period;
	}
	public Double getThreshold() {
		return threshold;
	}
	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}
	public Double getOffset() {
		return offset;
	}
	public void setOffset(Double offset) {
		this.offset = offset;
	}
	
	public ThresholdMonitor getThresholdMonitor(){
		ThresholdMonitor tm = new ThresholdMonitor();
		tm.setObservedAttribute(getAttribute());
		return tm;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getName() {
		return name;
	}

	public Monitor getMonitor() {
		return getThresholdMonitor();
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setType(String type) {
		this.type=type;
	}
}
