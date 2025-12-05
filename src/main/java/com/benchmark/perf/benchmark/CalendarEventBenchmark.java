package com.benchmark.perf.benchmark;

import com.benchmark.perf.generator.EventDataGenerator;
import com.benchmark.perf.model.CalendarEvent;
import com.benchmark.perf.serializer.GsonEventSerializer;
import com.benchmark.perf.serializer.JacksonDatabindEventSerializer;
import com.benchmark.perf.serializer.JacksonStreamingEventSerializer;
import com.benchmark.perf.serializer.MoshiEventSerializer;
import com.benchmark.perf.serializer.OrgJsonEventSerializer;
import com.benchmark.perf.serializer.StringBuilderEventSerializer;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JMH Benchmark comparing org.json library vs StringBuilder for calendar event
 * serialization.
 */
@State(Scope.Thread)
@BenchmarkMode({ Mode.Throughput, Mode.AverageTime })
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 2, jvmArgs = { "-Xmx8g", "-Xms8g" })
public class CalendarEventBenchmark {

    @Param({ "100", "1000", "10000", "50000" })
    private int eventCount;

    @Param({ "5" })
    private int warmupIterations;

    @Param({ "10" })
    private int measurementIterations;

    private List<CalendarEvent> events;

    @Setup(Level.Trial)
    public void setup() {
        System.out.println("Generating " + eventCount + " calendar events for benchmark...");
        events = EventDataGenerator.generateEvents(eventCount);
        System.out.println("Setup complete. Events generated: " + events.size());
    }

    @Benchmark
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
    public String benchmarkOrgJson(Blackhole bh) {
        String json = OrgJsonEventSerializer.INSTANCE.serialize(events);
        bh.consume(json);
        return json;
    }

    @Benchmark
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
    public String benchmarkStringBuilder(Blackhole bh) {
        String json = StringBuilderEventSerializer.INSTANCE.serialize(events);
        bh.consume(json);
        return json;
    }

    @Benchmark
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
    public String benchmarkJacksonDatabind(Blackhole bh) {
        String json = JacksonDatabindEventSerializer.INSTANCE.serialize(events);
        bh.consume(json);
        return json;
    }

    @Benchmark
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
    public String benchmarkJacksonStreaming(Blackhole bh) {
        String json = JacksonStreamingEventSerializer.INSTANCE.serialize(events);
        bh.consume(json);
        return json;
    }

    @Benchmark
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
    public String benchmarkGson(Blackhole bh) {
        String json = GsonEventSerializer.INSTANCE.serialize(events);
        bh.consume(json);
        return json;
    }

    @Benchmark
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
    public String benchmarkMoshi(Blackhole bh) {
        String json = MoshiEventSerializer.INSTANCE.serialize(events);
        bh.consume(json);
        return json;
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
