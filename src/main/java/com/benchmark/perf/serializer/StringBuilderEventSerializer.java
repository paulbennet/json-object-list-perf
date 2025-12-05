package com.benchmark.perf.serializer;

import com.benchmark.perf.model.CalendarEvent;
import com.benchmark.perf.util.JsonUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serializes calendar events using a manually managed StringBuilder that is
 * reused per thread to limit garbage.
 */
public class StringBuilderEventSerializer implements CalendarEventSerializer {

    public static final StringBuilderEventSerializer INSTANCE = new StringBuilderEventSerializer();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private StringBuilderEventSerializer() {
    }

    @Override
    public String getName() {
        return "StringBuilder";
    }

    @Override
    public String serialize(List<CalendarEvent> events) {
        StringBuilder sb = ThreadLocalBufferProvider.acquireStringBuilder();
        sb.append('[');

        for (int i = 0; i < events.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            appendEvent(sb, events.get(i));
        }

        sb.append(']');
        String json = sb.toString();
        ThreadLocalBufferProvider.releaseStringBuilder(sb);
        return json;
    }

    private void appendEvent(StringBuilder sb, CalendarEvent event) {
        sb.append('{');
        appendStringField(sb, "id", event.getId());
        appendStringField(sb, "title", event.getTitle());
        appendStringField(sb, "description", event.getDescription());
        appendStringField(sb, "startTime", event.getStartTime().format(FORMATTER));
        appendStringField(sb, "endTime", event.getEndTime().format(FORMATTER));
        appendStringField(sb, "location", event.getLocation());
        appendArray(sb, "attendees", event.getAttendees());
        appendStringField(sb, "recurrenceRule", event.getRecurrenceRule().name());
        appendIntegerArray(sb, "reminders", event.getReminders());
        appendStringField(sb, "timezone", event.getTimezone());
        appendStringField(sb, "organizerEmail", event.getOrganizerEmail());
        appendStringField(sb, "status", event.getStatus().name());
        trimTrailingComma(sb);
        sb.append('}');
    }

    private void appendStringField(StringBuilder sb, String field, String value) {
        sb.append('"').append(field).append('"').append(':');
        if (value == null) {
            sb.append("null");
        } else {
            sb.append('"').append(JsonUtils.escapeJson(value)).append('"');
        }
        sb.append(',');
    }

    private void appendArray(StringBuilder sb, String field, List<String> values) {
        sb.append('"').append(field).append('"').append(':');
        sb.append('[');
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            String value = values.get(i);
            if (value == null) {
                sb.append("null");
            } else {
                sb.append('"').append(JsonUtils.escapeJson(value)).append('"');
            }
        }
        sb.append(']').append(',');
    }

    private void appendIntegerArray(StringBuilder sb, String field, List<Integer> values) {
        sb.append('"').append(field).append('"').append(':');
        sb.append('[');
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(values.get(i));
        }
        sb.append(']').append(',');
    }

    private void trimTrailingComma(StringBuilder sb) {
        int lastIndex = sb.length() - 1;
        if (lastIndex >= 0 && sb.charAt(lastIndex) == ',') {
            sb.setLength(lastIndex);
        }
    }
}
