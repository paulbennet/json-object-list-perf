package com.zoho.perf.serializer;

import com.zoho.perf.model.CalendarEvent;

import java.util.List;

/**
 * Contract for serializer strategies so benchmarks/tests can share plumbing.
 */
public interface CalendarEventSerializer {
    /**
     * @return human-readable name for reports/logs.
     */
    String getName();

    /**
     * Serializes the provided events into JSON.
     */
    String serialize(List<CalendarEvent> events);
}
