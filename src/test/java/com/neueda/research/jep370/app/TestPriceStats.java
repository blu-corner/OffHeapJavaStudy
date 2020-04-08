package com.neueda.research.jep370.app;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;

import org.junit.Test;

public class TestPriceStats {

	private static String DATA_FILE = "/GBPUSD_H4.csv";

	@Test
	public void testOnHeapList() throws Exception {
		System.out.println("==== On Heap List Run ====");
		OhlcStatsGenerator gen = new ListOhlcStatsGenerator();
		test(gen);
	}

	@Test
	public void testOnHeapArray() throws Exception {
		System.out.println("==== On Heap Array Run ====");
		OhlcStatsGenerator gen = new ArrayOhlcStatsGenerator();
		test(gen);
	}
	
	@Test
	public void testOnHeapByteBuffer() throws Exception {
		System.out.println("==== On Heap ByteBuffer Run ====");
		OhlcStatsGenerator gen = new ByteBufferOhlcGenerator(false);
		test(gen);
	}
	
	@Test
	public void testOffHeapByteBuffer() throws Exception {
		System.out.println("==== Off Heap ByteBuffer Run ====");
		OhlcStatsGenerator gen = new ByteBufferOhlcGenerator(true);
		test(gen);
	}
	
	@Test
	public void testOffHeap() throws Exception {
		System.out.println("==== Off Heap Foreign Memory API Run ====");
		OhlcStatsGenerator gen = new OffHeapOhlcStatsGenerator();
		test(gen);
	}

	
	private void test(OhlcStatsGenerator gen) throws Exception {
		// price data to memory
		try (InputStream in = getClass().getResourceAsStream(DATA_FILE); 
				BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String line;
			while ((line = br.readLine()) != null) {
				
				String[] fields = line.split(",");
				long date = Instant.parse(fields[0]).toEpochMilli();
				double open = Double.parseDouble(fields[1]);
				double high = Double.parseDouble(fields[2]);
				double low = Double.parseDouble(fields[3]);
				double close = Double.parseDouble(fields[4]);
				long volume = Long.parseLong(fields[5]);
				
				gen.addCandle(date, open, high, low, close, volume);
			}
			assertSummaryStats(gen.getSummaryStats());
		}
	}

	private void assertSummaryStats(SummaryStats ss) {
		System.out.println(ss);
		assertEquals(ss.getMaxPrice(), 1.71924, 0);
		assertEquals(ss.getMinPrice(), 1.16496, 0);
		assertEquals(ss.getMaxVol(), 272254);
		assertEquals(SummaryStats.formatDate(ss.getMaxPriceDate()), "2014-07-15T13:00Z");
		assertEquals(SummaryStats.formatDate(ss.getMinPriceDate()), "2016-10-06T21:00Z");
		assertEquals(SummaryStats.formatDate(ss.getMaxVolDate()), "2016-06-24T01:00Z");
	}
}
