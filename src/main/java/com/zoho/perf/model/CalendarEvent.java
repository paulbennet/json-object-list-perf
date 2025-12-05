package com.zoho.perf.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Represents a calendar event with comprehensive fields for realistic
 * benchmarking.
 */
public class CalendarEvent {
    private String id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private List<String> attendees;
    private RecurrenceRule recurrenceRule;
    private List<Integer> reminders; // minutes before event
    private String timezone;
    private String organizerEmail;
    private EventStatus status;

    public CalendarEvent() {
    }

    public CalendarEvent(String id, String title, String description, LocalDateTime startTime,
            LocalDateTime endTime, String location, List<String> attendees,
            RecurrenceRule recurrenceRule, List<Integer> reminders,
            String timezone, String organizerEmail, EventStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.attendees = attendees;
        this.recurrenceRule = recurrenceRule;
        this.reminders = reminders;
        this.timezone = timezone;
        this.organizerEmail = organizerEmail;
        this.status = status;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<String> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<String> attendees) {
        this.attendees = attendees;
    }

    public RecurrenceRule getRecurrenceRule() {
        return recurrenceRule;
    }

    public void setRecurrenceRule(RecurrenceRule recurrenceRule) {
        this.recurrenceRule = recurrenceRule;
    }

    public List<Integer> getReminders() {
        return reminders;
    }

    public void setReminders(List<Integer> reminders) {
        this.reminders = reminders;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getOrganizerEmail() {
        return organizerEmail;
    }

    public void setOrganizerEmail(String organizerEmail) {
        this.organizerEmail = organizerEmail;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CalendarEvent that = (CalendarEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CalendarEvent{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }

    public enum RecurrenceRule {
        NONE,
        DAILY,
        WEEKLY,
        MONTHLY,
        YEARLY
    }

    public enum EventStatus {
        CONFIRMED,
        TENTATIVE,
        CANCELLED
    }
}
