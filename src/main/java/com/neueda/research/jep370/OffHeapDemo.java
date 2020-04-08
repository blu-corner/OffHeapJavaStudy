package com.neueda.research.jep370;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;
import java.util.Random;
import java.util.Scanner;

import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryHandles;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemoryLayout.PathElement;
import jdk.incubator.foreign.MemorySegment;

/**
 * A series of demos of the Foreign Memory Access API. Building up from a simple
 * demo that allocates and accesses memory segments directly through to usage of
 * Memory Layouts for more controlled access.
 * 
 * As we are using an incubator module, in order to run this, ensure to add
 * --add-modules jdk.incubator.foreign to VM arguments
 * 
 * @author NiallMaguire
 * 
 */
public class OffHeapDemo extends Demo {

	public static void main(String[] args) {
		Demo demo = new OffHeapDemo();
		demo.runDemo();
	}

	@Override
	public void demo(int demoSelection) {
		switch (demoSelection) {
		case 0:
			System.out.println("Bye Bye!");
			System.exit(0);
			break;
		case 1:
			this.demoBasicReadWrite();
			break;
		case 2:
			this.demoReadEmptyMemoryArea();
			break;
		case 3:
			this.demoIndexBasedMemoryAccess();
			break;
		case 4:
			this.demoSimpleMemoryLayout();
			break;
		case 5:
			this.demoStructMemoryLayout();
			break;
		default:
			System.err.println("Unexpected value: " + demoSelection+". Please select again");
		}

	}

	/**
	 * Simple read and then write into and out of a native memory segment. Directly
	 * accessing values in memory based on offset position
	 */
	private void demoBasicReadWrite() {
		
		VarHandle intHandle = MemoryHandles.varHandle(int.class, ByteOrder.nativeOrder());
		
		//Allocating 100 bytes of native (i.e. off-heap) memory
		try (MemorySegment segment = MemorySegment.allocateNative(100)) {
			//Getting the base address of the MemorySegment
			MemoryAddress base = segment.baseAddress();

			//100 bytes allows us to store 25 int (each int takes up 4 bytes)
			for (int i = 0; i < 25; i++) {
				// Set the values in memory segment, each at a manually 
				//calculated offset (i.e. MemoryAddress)
				intHandle.set(base.addOffset(i * 4), getRandomNumberInRange(1, 100));
			}
			//Print the contents of the memory segment
			printSegmentOfInts(segment);
		}
	}

	/**
	 * What happens when we try to print ints from addresses that were never set?
	 */
	private void demoReadEmptyMemoryArea() {

		VarHandle intHandle = MemoryHandles.varHandle(int.class, ByteOrder.nativeOrder());

		try (MemorySegment segment = MemorySegment.allocateNative(100)) {

			MemoryAddress base = segment.baseAddress();

			// Only setting 10 ints. The printSegmentOfInts tries to access all addresses
			for (int i = 0; i < 10; i++) {
				intHandle.set(base.addOffset(i * 4), getRandomNumberInRange(1, 100));
			}
			printSegmentOfInts(segment);
		}
	}

	/**
	 * Use of strides for more natural feeling, index based access of values in the
	 * memory segment
	 */
	private void demoIndexBasedMemoryAccess() {

		VarHandle intHandle = MemoryHandles.varHandle(int.class, ByteOrder.nativeOrder());
		VarHandle intElemHandle = MemoryHandles.withStride(intHandle, 4);

		try (MemorySegment segment = MemorySegment.allocateNative(100)) {

			MemoryAddress base = segment.baseAddress();

			for (int i = 0; i < 25; i++) {
				// Values accessible using index position i
				intElemHandle.set(base, (long) i, getRandomNumberInRange(1, 100));
			}

			// Directly setting a value -1 at the 24th index
			intElemHandle.set(base, 24, -1);
			printSegmentOfInts(segment);
		}
	}

	/**
	 * Introducing MemoryLayout as a description of memory segment contents to allow
	 * more controlled access. Layout below simply enforces a simple array of
	 * integers layout within the memory segment
	 */
	private void demoSimpleMemoryLayout() {

		int seqLength = 25;

		MemoryLayout intArrayLayout = MemoryLayout.ofSequence(seqLength,
				MemoryLayout.ofValueBits(32, ByteOrder.nativeOrder()));

		VarHandle intElemHandle = intArrayLayout.varHandle(int.class, PathElement.sequenceElement());

		try (MemorySegment segment = MemorySegment.allocateNative(intArrayLayout)) {

			MemoryAddress base = segment.baseAddress();

			for (int i = 0; i < seqLength; i++) {
				intElemHandle.set(base, (long) i, getRandomNumberInRange(1, 100));
			}
			printSegmentOfInts(segment);
		}
	}

	/**
	 * Introducing a more complex MemoryLayout which is a sequence of structs, with
	 * each struct representing an x, y co-ordinate.
	 */
	private void demoStructMemoryLayout() {

		int seqLength = 25;

		//This MemoryLayout describes a sequence of 25 x,y coordinate pairs. 
		//Each x and y coordinte is of size 32 bits... just enought to store an int
		MemoryLayout seq = MemoryLayout.ofSequence(seqLength,
				MemoryLayout.ofStruct(MemoryLayout.ofValueBits(32, ByteOrder.nativeOrder()).withName("x"),
						MemoryLayout.ofValueBits(32, ByteOrder.nativeOrder()).withName("y")));

		var xHandle = seq.varHandle(int.class, PathElement.sequenceElement(), PathElement.groupElement("x"));
		var yHandle = seq.varHandle(int.class, PathElement.sequenceElement(), PathElement.groupElement("y"));

		try (MemorySegment segment = MemorySegment.allocateNative(200)) {

			MemoryAddress base = segment.baseAddress();

			for (long i = 0; i < seqLength; i++) {
				xHandle.set(base, i, (int) i);
				yHandle.set(base, i, getRandomNumberInRange(1, 100));
			}

			// Now we can access the x,y coordinates in an index based manner
			if (segment.isAlive()) {
				for (int i = 0; i < 25; i++) {
					System.out.printf("%d:%d ", xHandle.get(base, i) , yHandle.get(base, i));
				}
				System.out.println();
			}
		}
	}

	private void printSegmentOfInts(MemorySegment segment) {
		if (segment.isAlive()) {

			VarHandle intHandle = MemoryHandles.varHandle(int.class, ByteOrder.nativeOrder());

			MemoryAddress base = segment.baseAddress();

			for (int i = 0; i < segment.byteSize() / 4; i++) {
				System.out.printf("%d ",intHandle.get(base.addOffset(i * 4)));
			}
			System.out.println();
		}
	}

	private static int getRandomNumberInRange(int min, int max) {

		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}

		Random r = new Random();

		return r.nextInt((max - min) + 1) + min;
	}
}


abstract class Demo {

	public abstract void demo(int demoSelection);

	public void runDemo() {
		Scanner sn = new Scanner(System.in);
		try {
			while (true) {
				System.out.println("Pick a demo : ");
				int demoSelection = sn.nextInt();
				demo(demoSelection);
			}
		} finally {
			sn.close();
		}
	}
}