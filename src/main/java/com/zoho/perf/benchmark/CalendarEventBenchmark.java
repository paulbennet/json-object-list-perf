package com.zoho.perf.benchmark;

import com.zoho.perf.generator.EventDataGenerator;
import com.zoho.perf.model.CalendarEvent;
import com.zoho.perf.serializer.OrgJsonEventSerializer;
import com.zoho.perf.serializer.StringBuilderEventSerializer;
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
        String json = OrgJsonEventSerializer.serialize(events);
        bh.consume(json);
        return json;
    }

    @Benchmark
    @Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
    @Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
    public String benchmarkStringBuilder(Blackhole bh) {
        String json = StringBuilderEventSerializer.serialize(events);
        bh.consume(json);
        return json;
    }

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }
}
