package com.zoho.perf.serializer;

import com.zoho.perf.model.CalendarEvent;
import com.zoho.perf.util.JsonUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serializes calendar events using manual StringBuilder approach.
 */
public class StringBuilderEventSerializer {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Serializes a list of calendar events to JSON string using StringBuilder.
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

            sb.append('{');

            appendField(sb, "id", event.getId());
            sb.append(',');
            appendField(sb, "title", event.getTitle());
            sb.append(',');
            appendField(sb, "description", event.getDescription());
            sb.append(',');
            appendField(sb, "startTime", event.getStartTime().format(FORMATTER));
            sb.append(',');
            appendField(sb, "endTime", event.getEndTime().format(FORMATTER));
            sb.append(',');
            appendField(sb, "location", event.getLocation());
            sb.append(',');

            // Attendees array
            sb.append("\"attendees\":[");
            List<String> attendees = event.getAttendees();
            for (int j = 0; j < attendees.size(); j++) {
                if (j > 0) {
                    sb.append(',');
                }
                sb.append('"').append(JsonUtils.escapeJson(attendees.get(j))).append('"');
            }
            sb.append(']');
            sb.append(',');

            appendField(sb, "recurrenceRule", event.getRecurrenceRule().name());
            sb.append(',');

            // Reminders array
            sb.append("\"reminders\":[");
            List<Integer> reminders = event.getReminders();
            for (int j = 0; j < reminders.size(); j++) {
                if (j > 0) {
                    sb.append(',');
                }
                sb.append(reminders.get(j));
            }
            sb.append(']');
            sb.append(',');

            appendField(sb, "timezone", event.getTimezone());
            sb.append(',');
            appendField(sb, "organizerEmail", event.getOrganizerEmail());
            sb.append(',');
            appendField(sb, "status", event.getStatus().name());

            sb.append('}');
        }

        sb.append(']');

        return sb.toString();
    }

    /**
     * Appends a JSON field to the StringBuilder.
     *
     * @param sb    the StringBuilder
     * @param key   the field key
     * @param value the field value
     */
    private static void appendField(StringBuilder sb, String key, String value) {
        sb.append('"').append(key).append("\":\"")
                .append(JsonUtils.escapeJson(value))
                .append('"');
    }
}
