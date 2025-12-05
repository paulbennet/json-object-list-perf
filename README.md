# JSON Performance Profiling - Calendar Events

Memory and throughput performance comparison across **org.json**, **thread-local StringBuilder**, **Jackson databind**, **Jackson streaming**, **Gson**, and **Moshi** serializers for large calendar-event payloads in web server scenarios.

## Overview

This project benchmarks JSON serialization performance for calendar event data using six distinct strategies:

- **org.json library**: Convenient, type-safe `JSONArray`/`JSONObject` object model
- **Thread-local StringBuilder**: Manual JSON string construction with `JsonUtils` escaping and a reusable per-thread buffer to slash transient allocations
- **Jackson databind**: Cached `ObjectWriter` that converts directly from the event list
- **Jackson streaming**: `JsonGenerator` writing into a thread-local byte buffer for low-level control
- **Gson**: Lightweight adapter with a cached `TypeToken` for teams already standardized on Gson
- **Moshi**: Similar lightweight adapter showcasing another popular JSON stack

The benchmark simulates a web server sending large numbers of calendar events as JSON responses.

## Benchmark Type & Metrics

### Benchmark Type

- **Microbenchmark harness** built with [JMH](https://openjdk.org/projects/code-tools/jmh/) and defined in [CalendarEventBenchmark.java](src/main/java/com/zoho/perf/benchmark/CalendarEventBenchmark.java)
- **Serialization focus**: measures how quickly a batch of `CalendarEvent` objects can be turned into JSON strings via six discrete benchmark methods (org.json, thread-local StringBuilder, Jackson databind, Jackson streaming, Gson, Moshi)
- **Dataset-driven**: uses `EventDataGenerator` to synthesize realistic meetings sized from 100 to 50,000 events so the benchmark reflects production payloads
- **Dual modes**: each invocation runs in both `Mode.Throughput` (ops/sec) and `Mode.AverageTime` (ms/op) so engineers can compare latency and throughput under identical JVM settings

### Metrics Captured

- **Throughput (ops/sec)** and **Average Time (ms/op)** from the primary JMH metrics for each serializer/eventCount pair
- **Allocation rate (bytes/op) and MB/sec)** plus **GC count/time** captured through `-prof gc` and surfaced in [results/benchmark-results.json](results/benchmark-results.json) and the generated HTML report
- **Win/loss summaries, charts, and allocation tables** rendered by [HtmlReportGenerator](src/main/java/com/zoho/perf/report/HtmlReportGenerator.java) to highlight trend lines at a glance

### Processes & Automation

- **Full pipeline**: [run-benchmark.sh](run-benchmark.sh) cleans, packages, runs validation tests, executes the complete JMH suite with GC profiling, and produces HTML output
- **Progress monitoring**: [check-progress.sh](check-progress.sh) tells you whether the benchmark JVM is still running and how many result entries have been written so far
- **Manual workflows**: you can invoke `target/benchmarks.jar` with custom `@Param` values or JMH filters and regenerate reports directly via `HtmlReportGenerator` for previously captured JSON output

## Requirements

- **Java 21 LTS** or higher
- **Maven 3.6+**
- Minimum 8GB RAM for benchmarks with 50,000 events

## Project Structure

```
json-object-list-perf/
├── src/main/java/com/zoho/perf/
│   ├── model/              # CalendarEvent data model
│   ├── generator/          # Test data generation
│   ├── serializer/         # org.json, StringBuilder, Jackson, Gson, Moshi strategies + registry
│   ├── benchmark/          # JMH benchmark suite
│   ├── report/             # HTML report generator
│   └── util/               # JSON utilities (validation, escaping)
├── src/test/java/          # Validation tests
├── results/                # Benchmark results and HTML reports
├── pom.xml                 # Maven configuration
└── run-benchmark.sh        # Automated execution script
```

## Quick Start

### 1. Run All Benchmarks (Recommended)

```bash
chmod +x run-benchmark.sh
./run-benchmark.sh
```

This script will:

1. Compile the project
2. Run validation tests
3. Execute JMH benchmarks (15-30 minutes)
4. Generate HTML report with charts
5. Open the report in your browser

### 2. Run Specific Benchmarks

```bash
# Build the project
mvn clean package

# Run only 100 and 1000 event benchmarks
java -jar target/benchmarks.jar -p eventCount=100,1000

# Run with custom iterations
java -jar target/benchmarks.jar -p warmupIterations=3 -p measurementIterations=5

# Run only org.json benchmarks
java -jar target/benchmarks.jar benchmarkOrgJson

# Run a specific optimized strategy (similar filters exist for every method)
java -jar target/benchmarks.jar benchmarkStringBuilder
java -jar target/benchmarks.jar benchmarkJacksonDatabind
java -jar target/benchmarks.jar benchmarkJacksonStreaming
java -jar target/benchmarks.jar benchmarkGson
java -jar target/benchmarks.jar benchmarkMoshi
```

### 3. Run Validation Tests Only

```bash
mvn test
```

## Benchmark Configuration

### Dataset Sizes (Configurable via `@Param`)

- **100 events**: Small web response
- **1,000 events**: Medium dataset
- **10,000 events**: Large calendar export
- **50,000 events**: Enterprise-scale data

### Calendar Event Characteristics

- **Description**: 100-500 characters (realistic meeting notes)
- **Attendees**: 5-50 email addresses per event
- **Recurrence**: NONE, DAILY, WEEKLY, MONTHLY, YEARLY
- **Reminders**: 1-3 reminders per event
- **Special characters**: Quotes, newlines, backslashes in descriptions

### JMH Settings

- **Warmup**: 5 iterations, 1 second each (configurable)
- **Measurement**: 10 iterations, 1 second each (configurable)
- **Fork**: 2 separate JVMs
- **Heap**: 8GB (-Xmx8g -Xms8g)
- **Modes**: Throughput (ops/sec) and Average Time (ms/op)

## Understanding Results

### HTML Report Sections

1. **Executive Summary**

   - Overall performance winner
   - Average throughput improvement percentage
   - Win/loss count across scenarios

2. **Throughput Comparison Chart**

   - Operations per second (higher is better)
   - Bar chart comparing every serializer by dataset size, using the same discrete benchmark names you can filter via JMH

3. **Average Time Chart**

   - Milliseconds per operation (lower is better)
   - Line chart showing performance trends for all six strategies

4. **Memory Allocation Statistics**

   - Bytes allocated per operation
   - GC count during benchmark runs

5. **Detailed Results Table**
   - Complete benchmark scores with units

### Key Metrics

- **Throughput (ops/sec)**: How many serialization operations per second
- **Average Time (ms/op)**: Time to serialize one dataset
- **Allocation Rate (bytes/op)**: Memory allocated per operation
- **GC Count**: Number of garbage collections during test

## Customization

### Modify Warmup/Measurement Iterations

Edit [CalendarEventBenchmark.java](src/main/java/com/zoho/perf/benchmark/CalendarEventBenchmark.java):

```java
@Param({"3"})  // Change warmup iterations
private int warmupIterations;

@Param({"7"})  // Change measurement iterations
private int measurementIterations;
```

### Add Custom Dataset Sizes

```java
@Param({"100", "1000", "10000", "50000", "100000"})  // Add 100K
private int eventCount;
```

### Adjust JVM Heap Size

Edit [run-benchmark.sh](run-benchmark.sh) or modify `@Fork` annotation:

```java
@Fork(value = 2, jvmArgs = {"-Xmx16g", "-Xms16g"})  // 16GB heap
```

### Tune Thread-Local Buffer Capacity

- `ThreadLocalBufferProvider` (see [src/main/java/com/zoho/perf/serializer/ThreadLocalBufferProvider.java](src/main/java/com/zoho/perf/serializer/ThreadLocalBufferProvider.java)) seeds 16 KB `StringBuilder`s and 32 KB byte arrays per thread.
- Increase these constants if you benchmark payloads with extremely large descriptions or want to minimize growth operations.
- Decrease them if you run many benchmark threads concurrently and want to cap total thread-local memory.

## Validation Tests

The project includes parameterized validation tests to ensure every serializer (org.json, StringBuilder, Jackson databind/streaming, Gson, Moshi) produces valid, parseable JSON:

- ✅ Valid JSON syntax verification
- ✅ Array length comparison
- ✅ Field-by-field content validation
- ✅ Special character handling (quotes, newlines, backslashes)
- ✅ Empty list handling
- ✅ Large dataset (1000+ events) validation

Run tests: `mvn test`

## Expected Performance Characteristics

### Thread-local StringBuilder

- Reuses a per-thread `StringBuilder` via `ThreadLocalBufferProvider`, eliminating most temporary allocations
- Manual field-by-field rendering with `JsonUtils` escaping keeps GC pressure low on 10K+ payloads
- Most sensitive to schema changes because formatting logic is handwritten

### org.json

- Easiest to maintain thanks to `JSONObject` abstractions and built-in escaping
- Incurs extra allocations for each intermediate array/object, so it lags for enterprise payload sizes

### Jackson Databind

- Cached `ObjectWriter` offers strong ergonomics while keeping conversions fast
- Honors Java Time types through `jackson-datatype-jsr310` and benefits from thread-local byte buffers

### Jackson Streaming

- Uses `JsonGenerator` directly for maximal control and deterministic output
- Shares the same thread-local byte buffer optimization, making it the lowest-overhead Jackson variant

### Gson

- Lightweight dependency for apps already on Gson; adapter caching avoids reflection penalties per run
- No thread-local optimization is required because Gson internally pools writer buffers, but it still benefits from fewer transitive dependencies

### Moshi

- Similar footprint to Gson with an explicit `JsonAdapter` cached for the duration of the JVM
- Useful for Kotlin/Android-style stacks needing consistent behavior with Moshi-based clients

## Troubleshooting

### OutOfMemoryError

Increase heap size: `-Xmx16g` or reduce dataset sizes

### Benchmark Takes Too Long

- Reduce fork count: `@Fork(value = 1)`
- Reduce iterations: `-p warmupIterations=3 -p measurementIterations=5`
- Test fewer dataset sizes: `-p eventCount=100,1000`

### Report Not Generating

Check that `results/benchmark-results.json` exists and is valid JSON

## Advanced Usage

### Generate Report from Existing Results

```bash
java -cp target/benchmarks.jar com.zoho.perf.report.HtmlReportGenerator \
    results/benchmark-results.json \
    results/custom-report.html
```

### Export Results to CSV

```bash
java -jar target/benchmarks.jar -rf csv -rff results/benchmark.csv
```

### Compare Multiple Runs

Run benchmarks, save results with timestamps:

```bash
java -jar target/benchmarks.jar -rf json -rff results/benchmark-$(date +%Y%m%d-%H%M%S).json
```

## License

Internal Zoho project for calendar team performance analysis.

## Authors

Zoho Calendar Team - Performance Engineering

---

**Note**: Benchmark results are highly dependent on hardware, JVM version, and system load. Always run benchmarks on isolated, consistent hardware for accurate comparisons.
