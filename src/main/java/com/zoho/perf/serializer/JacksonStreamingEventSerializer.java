package com.zoho.perf.serializer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.zoho.perf.model.CalendarEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Jackson streaming serializer that writes directly with JsonGenerator and
 * reuses thread-local byte buffers.
 */
public class JacksonStreamingEventSerializer implements CalendarEventSerializer {

    public static final JacksonStreamingEventSerializer INSTANCE = new JacksonStreamingEventSerializer();

    private static final JsonFactory JSON_FACTORY = JsonFactory.builder().build();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private JacksonStreamingEventSerializer() {
    }

    @Override
    public String getName() {
        return "JacksonStreaming";
    }

    @Override
    public String serialize(List<CalendarEvent> events) {
        ByteArrayOutputStream baos = ThreadLocalBufferProvider.acquireByteArrayOutputStream();
        try (JsonGenerator generator = JSON_FACTORY.createGenerator(baos)) {
            generator.writeStartArray();
            for (CalendarEvent event : events) {
                generator.writeStartObject();
                generator.writeStringField("id", event.getId());
                generator.writeStringField("title", event.getTitle());
                generator.writeStringField("description", event.getDescription());
                generator.writeStringField("startTime", event.getStartTime().format(FORMATTER));
                generator.writeStringField("endTime", event.getEndTime().format(FORMATTER));
                generator.writeStringField("location", event.getLocation());

                generator.writeArrayFieldStart("attendees");
                for (String attendee : event.getAttendees()) {
                    if (attendee == null) {
                        generator.writeNull();
                    } else {
                        generator.writeString(attendee);
                    }
                }
                generator.writeEndArray();

                generator.writeStringField("recurrenceRule", event.getRecurrenceRule().name());

                generator.writeArrayFieldStart("reminders");
                for (Integer reminder : event.getReminders()) {
                    generator.writeNumber(reminder);
                }
                generator.writeEndArray();

                generator.writeStringField("timezone", event.getTimezone());
                generator.writeStringField("organizerEmail", event.getOrganizerEmail());
                generator.writeStringField("status", event.getStatus().name());
                generator.writeEndObject();
            }
            generator.writeEndArray();
            generator.flush();
            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Jackson streaming serialization failed", e);
        }
    }
}
