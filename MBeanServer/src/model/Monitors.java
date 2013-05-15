package model;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class Monitors {

	@ElementList(required=false)
	public List<MyCounterMonitor> counterMonitors = new ArrayList<MyCounterMonitor>();
	@ElementList(required=false)
	public List<MyGaugeMonitor> gaugeMonitors = new ArrayList<MyGaugeMonitor>();
	@ElementList(required=false)
	public List<MyStringMonitor> stringMonitors = new ArrayList<MyStringMonitor>();
	@ElementList(required=false)
	public List<MyThresholdMonitor> thresholdMonitors = new ArrayList<MyThresholdMonitor>();
	
	
	public List<MyMonitor> monitors = new ArrayList<MyMonitor>();
	
	public Monitors() {
	}

	public List<MyCounterMonitor> getCounterMonitors() {
		return counterMonitors;
	}

	public void setCounterMonitors(List<MyCounterMonitor> counterMonitors) {
		this.counterMonitors = counterMonitors;
	}

	public List<MyGaugeMonitor> getGaugeMonitors() {
		return gaugeMonitors;
	}

	public void setGaugeMonitors(List<MyGaugeMonitor> gaugeMonitors) {
		this.gaugeMonitors = gaugeMonitors;
	}

	public List<MyStringMonitor> getStringMonitors() {
		return stringMonitors;
	}
	
	public void setStringMonitors(List<MyStringMonitor> stringMonitors) {
		this.stringMonitors = stringMonitors;
	}
	
	public List<MyThresholdMonitor> getThresholdMonitors() {
		return thresholdMonitors;
	}

	public void setThresholdMonitors(List<MyThresholdMonitor> thresholdMonitors) {
		this.thresholdMonitors = thresholdMonitors;
	}
	
	public List<MyMonitor> getMonitors(){
		monitors.clear();
		for (MyCounterMonitor cm : counterMonitors)
			monitors.add(cm);
		for (MyGaugeMonitor cm : gaugeMonitors)
			monitors.add(cm);
		for (MyStringMonitor cm : stringMonitors)
			monitors.add(cm);
		for (MyThresholdMonitor cm : thresholdMonitors)
			monitors.add(cm);		
		return this.monitors;
	}
}
