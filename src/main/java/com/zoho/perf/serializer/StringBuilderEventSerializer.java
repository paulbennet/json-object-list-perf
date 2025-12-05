package com.zoho.perf.serializer;

import com.zoho.perf.model.CalendarEvent;
import org.json.JSONObject;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serializes calendar events using JSONObject with streaming StringBuilder
 * approach.
 * Uses a single temporary JSONObject per event, serializes it using
 * JSONObject's built-in toString(),
 * and appends to StringBuilder to avoid holding large arrays in memory.
 */
public class StringBuilderEventSerializer {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Serializes a list of calendar events to JSON string using StringBuilder with
     * JSONObject.
     *
     * @param events list of calendar events
     * @return JSON string representation
     */
    public static String serialize(List<CalendarEvent> events) {
        StringBuilder sb = new StringBuilder(events.size() * 1024); // Estimate initial capacity

        sb.append('[');

        for (int i = 0; i < events.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }

            CalendarEvent event = events.get(i);

            // Create a temporary JSONObject for this event
            JSONObject jsonEvent = new JSONObject();

            jsonEvent.put("id", event.getId());
            jsonEvent.put("title", event.getTitle());
            jsonEvent.put("description", event.getDescription());
            jsonEvent.put("startTime", event.getStartTime().format(FORMATTER));
            jsonEvent.put("endTime", event.getEndTime().format(FORMATTER));
            jsonEvent.put("location", event.getLocation());
            jsonEvent.put("attendees", event.getAttendees());
            jsonEvent.put("recurrenceRule", event.getRecurrenceRule().name());
            jsonEvent.put("reminders", event.getReminders());
            jsonEvent.put("timezone", event.getTimezone());
            jsonEvent.put("organizerEmail", event.getOrganizerEmail());
            jsonEvent.put("status", event.getStatus().name());

            // Serialize using JSONObject's built-in toString() and append to StringBuilder
            sb.append(jsonEvent.toString());
        }

        sb.append(']');

        return sb.toString();
    }
}
