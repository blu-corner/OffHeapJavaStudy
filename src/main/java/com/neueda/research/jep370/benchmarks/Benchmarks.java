package com.neueda.research.jep370.benchmarks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import com.neueda.research.jep370.app.ArrayOhlcStatsGenerator;
import com.neueda.research.jep370.app.ByteBufferOhlcGenerator;
import com.neueda.research.jep370.app.ListOhlcStatsGenerator;
import com.neueda.research.jep370.app.OffHeapOhlcStatsGenerator;
import com.neueda.research.jep370.app.OhlcStatsGenerator;
import com.neueda.research.jep370.app.SummaryStats;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemorySegment;

@Fork(value = 1, warmups = 2)
@State(Scope.Thread)
@Measurement(time = 5, iterations = 10)
@Warmup(time = 2, iterations = 5)
public class Benchmarks {
	
	private static String DATA_FILE = "/GBPUSD_H4.csv";

	private List<Long> dates = new ArrayList<>();
	private List<Double> opens = new ArrayList<>();
	private List<Double> highs = new ArrayList<>();
	private List<Double> lows = new ArrayList<>();
	private List<Double> closes = new ArrayList<>();
	private List<Long> volumes = new ArrayList<>();

	// Not interested in allocation time in benchmark, so do this in setup
	private OhlcStatsGenerator arrayStatsGen;
	private OhlcStatsGenerator listStatsGen;
	private OhlcStatsGenerator byteBufferGen;
	private OhlcStatsGenerator directbyteBufferGen;
	private OhlcStatsGenerator offHeapStatsGen;

	@Setup
	// Slow IO should not be part of the benchmark. Leave the data in a raw but
	// ready to use format
	public void loadTestData() throws IOException {

		// Not interested in allocation times in benchmark, so do this in setup
		arrayStatsGen = new ArrayOhlcStatsGenerator();
		listStatsGen = new ListOhlcStatsGenerator();
		byteBufferGen = new ByteBufferOhlcGenerator(false);
		directbyteBufferGen = new ByteBufferOhlcGenerator(true);
		offHeapStatsGen = new OffHeapOhlcStatsGenerator();

		try (InputStream in = getClass().getResourceAsStream(DATA_FILE);
				BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] fields = line.split(",");
				dates.add(Instant.parse(fields[0]).toEpochMilli());
				opens.add(Double.parseDouble(fields[1]));
				highs.add(Double.parseDouble(fields[2]));
				lows.add(Double.parseDouble(fields[3]));
				closes.add(Double.parseDouble(fields[4]));
				volumes.add(Long.parseLong(fields[5]));
			}
		}
	}
	
	@TearDown
	public void tearDown() {
		arrayStatsGen.close();
		listStatsGen.close();
		byteBufferGen.close();
		directbyteBufferGen.close();
		offHeapStatsGen.close();
	}

//	static final MemoryAddress ma = MemorySegment.allocateNative(4 * 10).baseAddress();
//	static final VarHandle INT_VH = MemoryHandles.varHandle(int.class, ByteOrder.nativeOrder());
//	
//	private int ELEMS = 10;
//	
//	static final ByteBuffer bb = ByteBuffer.allocateDirect(4 * 10).order(ByteOrder.nativeOrder());
//
//	@Setup
//	public void setup() {
//		for (int i = 0; i <ELEMS; i++) {
//			bb.putInt(i);
//			INT_VH.set(ma.offset(i * 4), i);
//		}
//	}
//	
//	@Benchmark
//	public int testBufferAbsoluteNativeGet() {
//		int sum = 0;
//		for(int i = 0 ; i < ELEMS ;i++ ) {
//			sum += bb.getInt(i * 4);
//		}
//		return sum;
//	}
//	
//	@Benchmark
//	public int testMemoryAddressGet() {
//		int sum = 0;
//		for(int i = 0 ; i < ELEMS ;i++ ) {
//			sum += (int)INT_VH.get(ma.offset(i *4));
//		}
//		return sum;
//	}

	@Benchmark
	public void onHeapArrayBenchmark() throws Exception {
		arrayStatsGen.reset();
		getStats(arrayStatsGen);
	}

	@Benchmark
	public void onHeapListBenchmark() throws Exception {
		listStatsGen.reset();
		getStats(listStatsGen);
	}

	@Benchmark
	public void onHeapByteBufferBenchmark() throws Exception {
		byteBufferGen.reset();
		getStats(byteBufferGen);
	}

	@Benchmark
	public void offHeapByteBufferBenchmark() throws Exception {
		directbyteBufferGen.reset();
		getStats(directbyteBufferGen);
	}

	@Benchmark
	public void offHeapApiBenchmark() throws Exception {
		offHeapStatsGen.reset();
		getStats(offHeapStatsGen);
	}

	private SummaryStats getStats(OhlcStatsGenerator gen) throws Exception {
		for (int i = 0; i < dates.size(); i++) {
			gen.addCandle(dates.get(i), opens.get(i), highs.get(i), lows.get(i), closes.get(i), volumes.get(i));
		}
		
		return gen.getSummaryStats();
	}

}
