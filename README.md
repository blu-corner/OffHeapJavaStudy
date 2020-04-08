# Running the Tests

First of all you will of course need Java 14 installed.

## Running the Throughput Tests

To run the throughput tests, you first need to build the benchmarks jar file containing the JMH tests. You can do this by running:

```mvn clean package```

This will run build the project jar, the benchmarks jar and the unit tests. 
Then to execute the benchmarks, execute: 

```java --add-modules jdk.incubator.foreign -jar target/benchmarks.jar```

Note you must explicitly add the jdk.incubator.foreign incubator package.

## Running the Continuous Usage Tests

These "tests" are not strictly tests, but rather application executions that allow you to monitor GC behavior.

If you have not already done so, build the application jar:

```mvn clean package```

To execute the array based continuous usage test:

```java --add-modules jdk.incubator.foreign -cp target/foreign_memory_access*.jar com.neueda.research.jep370.ContinuousUsageAnalysis_Array```

To execute the off-heap based  continuous usage test:

```java --add-modules jdk.incubator.foreign -cp target/foreign_memory_access*.jar com.neueda.research.jep370.ContinuousUsageAnalysis_OffHeap```
