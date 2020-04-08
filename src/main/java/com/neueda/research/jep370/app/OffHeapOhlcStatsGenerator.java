package com.neueda.research.jep370.app;

import java.lang.invoke.VarHandle;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayout.PathElement;
import jdk.incubator.foreign.MemoryLayouts;
import jdk.incubator.foreign.MemorySegment;

public class OffHeapOhlcStatsGenerator implements OhlcStatsGenerator {

	private MemorySegment segment;
	private static MemoryLayout layout = MemoryLayout.ofSequence(CANDLE_SIZE,
			MemoryLayout.ofStruct(
					MemoryLayouts.JAVA_LONG.withName("Date"), 
					MemoryLayouts.JAVA_DOUBLE.withName("Open"),
					MemoryLayouts.JAVA_DOUBLE.withName("High"), 
					MemoryLayouts.JAVA_DOUBLE.withName("Low"),
					MemoryLayouts.JAVA_DOUBLE.withName("Close"), 
					MemoryLayouts.JAVA_LONG.withName("Volume")));
	
	MemoryAddress baseAddress;
	
	private static VarHandle dHandle = layout.varHandle(long.class, PathElement.sequenceElement(),
			PathElement.groupElement("Date"));
	private static VarHandle oHandle = layout.varHandle(double.class, PathElement.sequenceElement(),
			PathElement.groupElement("Open"));
	private static VarHandle hHandle = layout.varHandle(double.class, PathElement.sequenceElement(),
			PathElement.groupElement("High"));
	private static VarHandle lHandle = layout.varHandle(double.class, PathElement.sequenceElement(),
			PathElement.groupElement("Low"));
	private static VarHandle cHandle = layout.varHandle(double.class, PathElement.sequenceElement(),
			PathElement.groupElement("Close"));
	private static VarHandle vHandle = layout.varHandle(long.class, PathElement.sequenceElement(),
			PathElement.groupElement("Volume"));
	private long candleCount;

	public OffHeapOhlcStatsGenerator() {
		this.segment = MemorySegment.allocateNative(layout.byteSize());
		//NB The result of segment.baseAddress() should be cached as 
		//it creates a MemoryAddress object on heap. Repeated calls creates garbage 
		this.baseAddress = segment.baseAddress();
	}

	@Override
	public void addCandle(long d, double o, double h, double l, double c, long v) {
		dHandle.set(baseAddress, candleCount, d);
		oHandle.set(baseAddress, candleCount, o);
		hHandle.set(baseAddress, candleCount, h);
		lHandle.set(baseAddress, candleCount, l);
		cHandle.set(baseAddress, candleCount, c);
		vHandle.set(baseAddress, candleCount, v);
		candleCount++;
	}

	@Override
	public SummaryStats getSummaryStats() {
		SummaryStats ss = new SummaryStats();
		for (int i = 0; i < candleCount; i++) {
			if ((double) hHandle.get(baseAddress, i) > ss.getMaxPrice()) {
				ss.setMaxPrice((double) hHandle.get(baseAddress, i));
				ss.setMaxPriceDate((long) dHandle.get(baseAddress, i));
			}
			if ((double) lHandle.get(baseAddress, i) < ss.getMinPrice()) {
				ss.setMinPrice((double) lHandle.get(baseAddress, i));
				ss.setMinPriceDate((long) dHandle.get(baseAddress, i));
			}
			if ((long) vHandle.get(baseAddress, i) > ss.getMaxVol()) {
				ss.setMaxVol((long) vHandle.get(baseAddress, i));
				ss.setMaxVolDate((long) dHandle.get(baseAddress, i));
			}
		}
		return ss;
	}

	@Override
	public void reset() {
		candleCount = 0;
	}

	@Override
	public void close() {
		segment.close();
		segment = null;
	}
}
