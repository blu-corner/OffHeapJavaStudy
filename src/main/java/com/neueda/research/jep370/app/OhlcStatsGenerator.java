package com.neueda.research.jep370.app;

public interface OhlcStatsGenerator {

	public static final int CANDLE_SIZE=20000;
	
	void addCandle(long d, double open, double high, double low, double close, long volume);

	SummaryStats getSummaryStats();
	
	void reset();
	
	void close();

}
