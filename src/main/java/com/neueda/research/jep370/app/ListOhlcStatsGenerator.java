package com.neueda.research.jep370.app;
import java.util.ArrayList;
import java.util.List;

public class ListOhlcStatsGenerator implements OhlcStatsGenerator {

	private List<Candle> candles = new ArrayList<>(CANDLE_SIZE);

	@Override
	public void addCandle(long d, double open, double high, double low, double close, long volume) {
		candles.add(new Candle(d, open, high, low, close, volume));
	}

	@Override
	public SummaryStats getSummaryStats() {
		SummaryStats ss = new SummaryStats();
		for (Candle c : candles) {
			if (c.getHigh() > ss.getMaxPrice()) {
				ss.setMaxPrice(c.getHigh());
				ss.setMaxPriceDate(c.getDate());
			}
			if (c.getLow() < ss.getMinPrice()) {
				ss.setMinPrice(c.getLow());
				ss.setMinPriceDate(c.getDate());
			}
			if (c.getVolume() > ss.getMaxVol()) {
				ss.setMaxVol(c.getVolume());
				ss.setMaxVolDate(c.getDate());
			}
		}
		return ss;
	}

	@Override
	public void reset() {
		candles.clear();
	}

	@Override
	public void close() {
		candles = null;
	}
}
