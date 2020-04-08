package com.neueda.research.jep370.app;
import java.time.Instant;
import java.time.ZoneOffset;

public class SummaryStats {

	private double maxPrice;
	private long maxPriceDate;
	private double minPrice = Double.MAX_VALUE;
	private long minPriceDate;
	private long maxVol;
	private long maxVolDate;

	public double getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(double maxPrice) {
		this.maxPrice = maxPrice;
	}

	public long getMaxPriceDate() {
		return maxPriceDate;
	}

	public void setMaxPriceDate(long maxPriceDate) {
		this.maxPriceDate = maxPriceDate;
	}

	public double getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(double minPrice) {
		this.minPrice = minPrice;
	}

	public long getMinPriceDate() {
		return minPriceDate;
	}

	public void setMinPriceDate(long minPriceDate) {
		this.minPriceDate = minPriceDate;
	}

	public long getMaxVol() {
		return maxVol;
	}

	public void setMaxVol(long maxVol) {
		this.maxVol = maxVol;
	}

	public long getMaxVolDate() {
		return maxVolDate;
	}

	public void setMaxVolDate(long maxVolDate) {
		this.maxVolDate = maxVolDate;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("===================").append("\n");
		sb.append("Max Price : " + maxPrice).append(" occoured at date " + formatDate(maxPriceDate)).append("\n");
		sb.append("Min Price : " + minPrice).append(" occoured at date " + formatDate(minPriceDate)).append("\n");
		sb.append("Max Volume : " + maxVol).append(" occoured at date " + formatDate(maxVolDate)).append("\n");
		sb.append("===================").append("\n");

		return sb.toString();
	}

	public static String formatDate(long date) {
		return Instant.ofEpochMilli(date).atZone(ZoneOffset.UTC).toString();
	}
}
