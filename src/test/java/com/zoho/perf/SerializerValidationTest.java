package com.zoho.perf;

import com.zoho.perf.generator.EventDataGenerator;
import com.zoho.perf.model.CalendarEvent;
import com.zoho.perf.serializer.OrgJsonEventSerializer;
import com.zoho.perf.serializer.StringBuilderEventSerializer;
import com.zoho.perf.util.JsonUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validation tests to ensure both serializers produce valid, parseable JSON.
 */
class SerializerValidationTest {

    @Test
    void testOrgJsonSerializerProducesValidJson() {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(10);
        String json = OrgJsonEventSerializer.serialize(events);

        assertTrue(JsonUtils.validateJson(json), "org.json serializer should produce valid JSON");
    }

    @Test
    void testStringBuilderSerializerProducesValidJson() {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(10);
        String json = StringBuilderEventSerializer.serialize(events);

        assertTrue(JsonUtils.validateJson(json), "StringBuilder serializer should produce valid JSON");
    }

    @Test
    void testBothSerializersProduceSameArrayLength() {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(50);

        String orgJson = OrgJsonEventSerializer.serialize(events);
        String sbJson = StringBuilderEventSerializer.serialize(events);

        assertEquals(50, JsonUtils.getArrayLength(orgJson), "org.json should have 50 events");
        assertEquals(50, JsonUtils.getArrayLength(sbJson), "StringBuilder should have 50 events");
    }

    @Test
    void testOrgJsonFirstEventFields() throws JSONException {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(5);
        CalendarEvent firstEvent = events.get(0);

        String json = OrgJsonEventSerializer.serialize(events);
        JSONArray jsonArray = new JSONArray(json);
        JSONObject firstJsonEvent = jsonArray.getJSONObject(0);

        assertEquals(firstEvent.getId(), firstJsonEvent.getString("id"));
        assertEquals(firstEvent.getTitle(), firstJsonEvent.getString("title"));
        assertEquals(firstEvent.getLocation(), firstJsonEvent.getString("location"));
        assertEquals(firstEvent.getRecurrenceRule().name(), firstJsonEvent.getString("recurrenceRule"));
        assertEquals(firstEvent.getStatus().name(), firstJsonEvent.getString("status"));
        assertEquals(firstEvent.getAttendees().size(), firstJsonEvent.getJSONArray("attendees").length());
        assertEquals(firstEvent.getReminders().size(), firstJsonEvent.getJSONArray("reminders").length());
    }

    @Test
    void testStringBuilderFirstEventFields() throws JSONException {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(5);
        CalendarEvent firstEvent = events.get(0);

        String json = StringBuilderEventSerializer.serialize(events);
        JSONArray jsonArray = new JSONArray(json);
        JSONObject firstJsonEvent = jsonArray.getJSONObject(0);

        assertEquals(firstEvent.getId(), firstJsonEvent.getString("id"));
        assertEquals(firstEvent.getTitle(), firstJsonEvent.getString("title"));
        assertEquals(firstEvent.getLocation(), firstJsonEvent.getString("location"));
        assertEquals(firstEvent.getRecurrenceRule().name(), firstJsonEvent.getString("recurrenceRule"));
        assertEquals(firstEvent.getStatus().name(), firstJsonEvent.getString("status"));
        assertEquals(firstEvent.getAttendees().size(), firstJsonEvent.getJSONArray("attendees").length());
        assertEquals(firstEvent.getReminders().size(), firstJsonEvent.getJSONArray("reminders").length());
    }

    @Test
    void testOrgJsonLastEventFields() throws JSONException {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(20);
        CalendarEvent lastEvent = events.get(events.size() - 1);

        String json = OrgJsonEventSerializer.serialize(events);
        JSONArray jsonArray = new JSONArray(json);
        JSONObject lastJsonEvent = jsonArray.getJSONObject(jsonArray.length() - 1);

        assertEquals(lastEvent.getId(), lastJsonEvent.getString("id"));
        assertEquals(lastEvent.getTitle(), lastJsonEvent.getString("title"));
        assertEquals(lastEvent.getOrganizerEmail(), lastJsonEvent.getString("organizerEmail"));
        assertEquals(lastEvent.getTimezone(), lastJsonEvent.getString("timezone"));
    }

    @Test
    void testStringBuilderLastEventFields() throws JSONException {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(20);
        CalendarEvent lastEvent = events.get(events.size() - 1);

        String json = StringBuilderEventSerializer.serialize(events);
        JSONArray jsonArray = new JSONArray(json);
        JSONObject lastJsonEvent = jsonArray.getJSONObject(jsonArray.length() - 1);

        assertEquals(lastEvent.getId(), lastJsonEvent.getString("id"));
        assertEquals(lastEvent.getTitle(), lastJsonEvent.getString("title"));
        assertEquals(lastEvent.getOrganizerEmail(), lastJsonEvent.getString("organizerEmail"));
        assertEquals(lastEvent.getTimezone(), lastJsonEvent.getString("timezone"));
    }

    @Test
    void testSpecialCharactersInDescription() throws JSONException {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(1);
        CalendarEvent event = events.get(0);

        // Modify description to include special characters
        String specialDesc = "Meeting with \"quotes\", backslash \\, newline \n, and tab \t characters";
        event.setDescription(specialDesc);

        String orgJson = OrgJsonEventSerializer.serialize(events);
        String sbJson = StringBuilderEventSerializer.serialize(events);

        // Both should produce valid JSON
        assertTrue(JsonUtils.validateJson(orgJson), "org.json should handle special characters");
        assertTrue(JsonUtils.validateJson(sbJson), "StringBuilder should handle special characters");

        // Verify content is preserved (org.json handles escaping internally)
        JSONArray orgArray = new JSONArray(orgJson);
        JSONArray sbArray = new JSONArray(sbJson);

        assertNotNull(orgArray.getJSONObject(0).getString("description"));
        assertNotNull(sbArray.getJSONObject(0).getString("description"));
    }

    @Test
    void testEmptyEventList() {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(0);

        String orgJson = OrgJsonEventSerializer.serialize(events);
        String sbJson = StringBuilderEventSerializer.serialize(events);

        assertEquals("[]", orgJson);
        assertEquals("[]", sbJson);

        assertTrue(JsonUtils.validateJson(orgJson));
        assertTrue(JsonUtils.validateJson(sbJson));
    }

    @Test
    void testLargeEventSet() {
        List<CalendarEvent> events = EventDataGenerator.generateEvents(1000);

        String orgJson = OrgJsonEventSerializer.serialize(events);
        String sbJson = StringBuilderEventSerializer.serialize(events);

        assertTrue(JsonUtils.validateJson(orgJson), "org.json should handle 1000 events");
        assertTrue(JsonUtils.validateJson(sbJson), "StringBuilder should handle 1000 events");

        assertEquals(1000, JsonUtils.getArrayLength(orgJson));
        assertEquals(1000, JsonUtils.getArrayLength(sbJson));
    }
}
