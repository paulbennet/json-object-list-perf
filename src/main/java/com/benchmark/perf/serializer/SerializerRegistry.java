package com.benchmark.perf.serializer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry to keep serializer lists in sync across benchmarks, tests,
 * and reporting.
 */
public final class SerializerRegistry {

    public static final List<CalendarEventSerializer> ALL_SERIALIZERS = List.of(
            OrgJsonEventSerializer.INSTANCE,
            StringBuilderEventSerializer.INSTANCE,
            JacksonDatabindEventSerializer.INSTANCE,
            JacksonStreamingEventSerializer.INSTANCE,
            GsonEventSerializer.INSTANCE,
            MoshiEventSerializer.INSTANCE);

    private static final Map<String, CalendarEventSerializer> BY_NAME = new ConcurrentHashMap<>();

    static {
        for (CalendarEventSerializer serializer : ALL_SERIALIZERS) {
            BY_NAME.put(serializer.getName(), serializer);
        }
    }

    private SerializerRegistry() {
    }

    public static CalendarEventSerializer getByName(String name) {
        CalendarEventSerializer serializer = BY_NAME.get(name);
        if (serializer == null) {
            throw new IllegalArgumentException("Unknown serializer: " + name);
        }
        return serializer;
    }
}
