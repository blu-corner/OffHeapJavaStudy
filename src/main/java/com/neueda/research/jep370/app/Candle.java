package com.neueda.research.jep370.app;
public class Candle {

	private long date;
	private double open;
	private double high;
	private double low;
	private double close;
	private long volume;

	public Candle(long d, double o, double h, double l, double c, long v) {
		this.date = d;
		this.open = o;
		this.high = h;
		this.low = l;
		this.close = c;
		this.volume = v;
	}

	public long getDate() {
		return date;
	}

	public double getOpen() {
		return open;
	}

	public double getHigh() {
		return high;
	}

	public double getLow() {
		return low;
	}

	public double getClose() {
		return close;
	}

	public long getVolume() {
		return volume;
	}
}
