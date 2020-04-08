package com.neueda.research.jep370.app;

public class ArrayOhlcStatsGenerator implements OhlcStatsGenerator{

		private Candle[] candles = new Candle[CANDLE_SIZE];
		int index = 0;
		
		@Override
		public void addCandle(long d, double open, double high, double low, double close, long volume) {
			candles[index] = new Candle(d, open, high, low, close, volume);
			index ++;
		}

		@Override
		public SummaryStats getSummaryStats() {
			SummaryStats ss = new SummaryStats();
			for (int i =0; i < index; i ++) {
				if (candles[i].getHigh() > ss.getMaxPrice()) {
					ss.setMaxPrice(candles[i].getHigh());
					ss.setMaxPriceDate(candles[i].getDate());
				}
				if (candles[i].getLow() < ss.getMinPrice()) {
					ss.setMinPrice(candles[i].getLow());
					ss.setMinPriceDate(candles[i].getDate());
				}
				if (candles[i].getVolume() > ss.getMaxVol()) {
					ss.setMaxVol(candles[i].getVolume());
					ss.setMaxVolDate(candles[i].getDate());
				}
			}
			return ss;
		}

		@Override
		public void reset() {
			index = 0;
		}

		@Override
		public void close() {
			candles = null;
		}

}
