package io.openems.edge.solplanet.core;

public class SolplanetData {
	public Integer pvPower = null;
	public Integer essPower = null;
	public Integer essSoc = null;
	
	public void set(Integer pvPower, Integer essPower, Integer essSoc) {
		this.pvPower = pvPower;
		this.essPower = essPower;
		this.essSoc = essSoc;
	}
	
	public void reset() {
		this.pvPower = null;
		this.essPower = null;
		this.essSoc = null;
	}
}
