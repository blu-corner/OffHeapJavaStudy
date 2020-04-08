package com.neueda.research.jep370.app;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ByteBufferOhlcGenerator implements OhlcStatsGenerator {

	private ByteBuffer bb;
	private int candleCount;

	public ByteBufferOhlcGenerator(boolean direct) {
		if(direct) {
			//4 x double + 2 * long = 48 bytes
			bb = ByteBuffer.allocateDirect(48 * CANDLE_SIZE).order(ByteOrder.nativeOrder());
		}else {
			bb = ByteBuffer.allocate(48 * CANDLE_SIZE).order(ByteOrder.nativeOrder());
		}
	}

	@Override
	public void addCandle(long d, double open, double high, double low, double close, long volume) {
		bb.putLong(d);
		bb.putDouble(open);
		bb.putDouble(high);
		bb.putDouble(low);
		bb.putDouble(close);
		bb.putLong(volume);
		candleCount++;
	}

	@Override
	public SummaryStats getSummaryStats() {
		SummaryStats ss = new SummaryStats();
		bb.rewind();
		for (int i = 0; i < candleCount; i++) {
			var d = bb.getLong();
			var o = bb.getDouble();
			var h = bb.getDouble();
			var l = bb.getDouble();
			var c = bb.getDouble();
			var v = bb.getLong();
			if (h > ss.getMaxPrice()) {
				ss.setMaxPrice(h);
				ss.setMaxPriceDate(d);
			}
			if (l  < ss.getMinPrice()) {
				ss.setMinPrice(l);
				ss.setMinPriceDate(d);
			}
			if (v > ss.getMaxVol()) {
				ss.setMaxVol(v);
				ss.setMaxVolDate(d);
			}
		}
		return ss;
	}

	@Override
	public void reset() {
		bb.rewind();
		candleCount = 0;
	}

	@Override
	public void close() {
		bb = null;
	}

}
