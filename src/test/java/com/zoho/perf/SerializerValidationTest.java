package com.zoho.perf;

import com.zoho.perf.generator.EventDataGenerator;
import com.zoho.perf.model.CalendarEvent;
import com.zoho.perf.serializer.CalendarEventSerializer;
import com.zoho.perf.serializer.SerializerRegistry;
import com.zoho.perf.util.JsonUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validation tests to ensure every serializer produces valid, parseable JSON.
 */
class SerializerValidationTest {

    static Stream<Arguments> serializers() {
        return SerializerRegistry.ALL_SERIALIZERS.stream()
                .map(serializer -> Arguments.of(serializer.getName(), serializer));
    }

    @ParameterizedTest(name = "{0} produces valid JSON")
    @MethodSource("serializers")
    void serializerProducesValidJson(String name, CalendarEventSerializer serializer) {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(10);
        String json = serializer.serialize(events);
        assertTrue(JsonUtils.validateJson(json), name + " should produce valid JSON");
    }

    @ParameterizedTest(name = "{0} preserves event count")
    @MethodSource("serializers")
    void serializerProducesCorrectEventCount(String name, CalendarEventSerializer serializer) {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(50);
        String json = serializer.serialize(events);
        assertEquals(50, JsonUtils.getArrayLength(json), name + " should emit 50 events");
    }

    @ParameterizedTest(name = "{0} preserves first event fields")
    @MethodSource("serializers")
    void serializerPreservesFirstEventFields(String name, CalendarEventSerializer serializer) throws JSONException {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(5);
        CalendarEvent firstEvent = events.get(0);

        String json = serializer.serialize(events);
        JSONArray jsonArray = new JSONArray(json);
        JSONObject firstJsonEvent = jsonArray.getJSONObject(0);

        assertEquals(firstEvent.getId(), firstJsonEvent.getString("id"), name + " id mismatch");
        assertEquals(firstEvent.getTitle(), firstJsonEvent.getString("title"), name + " title mismatch");
        assertEquals(firstEvent.getLocation(), firstJsonEvent.getString("location"), name + " location mismatch");
        assertEquals(firstEvent.getRecurrenceRule().name(), firstJsonEvent.getString("recurrenceRule"),
                name + " recurrence mismatch");
        assertEquals(firstEvent.getStatus().name(), firstJsonEvent.getString("status"), name + " status mismatch");
        assertEquals(firstEvent.getAttendees().size(), firstJsonEvent.getJSONArray("attendees").length(),
                name + " attendees length mismatch");
        assertEquals(firstEvent.getReminders().size(), firstJsonEvent.getJSONArray("reminders").length(),
                name + " reminders length mismatch");
    }

    @ParameterizedTest(name = "{0} preserves last event fields")
    @MethodSource("serializers")
    void serializerPreservesLastEventFields(String name, CalendarEventSerializer serializer) throws JSONException {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(20);
        CalendarEvent lastEvent = events.get(events.size() - 1);

        String json = serializer.serialize(events);
        JSONArray jsonArray = new JSONArray(json);
        JSONObject lastJsonEvent = jsonArray.getJSONObject(jsonArray.length() - 1);

        assertEquals(lastEvent.getId(), lastJsonEvent.getString("id"), name + " id mismatch");
        assertEquals(lastEvent.getTitle(), lastJsonEvent.getString("title"), name + " title mismatch");
        assertEquals(lastEvent.getOrganizerEmail(), lastJsonEvent.getString("organizerEmail"),
                name + " organizer mismatch");
        assertEquals(lastEvent.getTimezone(), lastJsonEvent.getString("timezone"), name + " timezone mismatch");
    }

    @ParameterizedTest(name = "{0} handles special characters")
    @MethodSource("serializers")
    void serializerHandlesSpecialCharacters(String name, CalendarEventSerializer serializer) {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(1);
        CalendarEvent event = events.get(0);
        event.setDescription("Meeting with \"quotes\", backslash \\, newline \n, and tab \t characters");

        String json = serializer.serialize(events);
        assertTrue(JsonUtils.validateJson(json), name + " should handle special characters");
        JSONArray jsonArray = new JSONArray(json);
        assertNotNull(jsonArray.getJSONObject(0).getString("description"));
    }

    @ParameterizedTest(name = "{0} handles empty lists")
    @MethodSource("serializers")
    void serializerHandlesEmptyList(String name, CalendarEventSerializer serializer) {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(0);
        String json = serializer.serialize(events);
        assertEquals("[]", json, name + " should emit empty array");
        assertTrue(JsonUtils.validateJson(json));
    }

    @ParameterizedTest(name = "{0} handles large datasets")
    @MethodSource("serializers")
    void serializerHandlesLargeEventSet(String name, CalendarEventSerializer serializer) {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(1000);
        String json = serializer.serialize(events);
        assertTrue(JsonUtils.validateJson(json), name + " should handle 1000 events");
        assertEquals(1000, JsonUtils.getArrayLength(json));
    }
}
