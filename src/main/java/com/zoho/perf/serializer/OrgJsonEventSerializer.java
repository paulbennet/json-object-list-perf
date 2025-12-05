package com.zoho.perf.serializer;

import com.zoho.perf.model.CalendarEvent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serializes calendar events using org.json library (JSONArray and JSONObject).
 */
public class OrgJsonEventSerializer implements CalendarEventSerializer {

    public static final OrgJsonEventSerializer INSTANCE = new OrgJsonEventSerializer();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private OrgJsonEventSerializer() {
    }

    @Override
    public String getName() {
        return "org.json";
    }

    @Override
    public String serialize(List<CalendarEvent> events) {
        JSONArray jsonArray = new JSONArray();

        for (CalendarEvent event : events) {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put("id", event.getId());
            jsonObject.put("title", event.getTitle());
            jsonObject.put("description", event.getDescription());
            jsonObject.put("startTime", event.getStartTime().format(FORMATTER));
            jsonObject.put("endTime", event.getEndTime().format(FORMATTER));
            jsonObject.put("location", event.getLocation());

            JSONArray attendeesArray = new JSONArray();
            for (String attendee : event.getAttendees()) {
                attendeesArray.put(attendee);
            }
            jsonObject.put("attendees", attendeesArray);

            jsonObject.put("recurrenceRule", event.getRecurrenceRule().name());

            JSONArray remindersArray = new JSONArray();
            for (Integer reminder : event.getReminders()) {
                remindersArray.put(reminder);
            }
            jsonObject.put("reminders", remindersArray);

            jsonObject.put("timezone", event.getTimezone());
            jsonObject.put("organizerEmail", event.getOrganizerEmail());
            jsonObject.put("status", event.getStatus().name());

            jsonArray.put(jsonObject);
        }

        return jsonArray.toString();
    }
}
