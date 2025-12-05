package com.zoho.perf.generator;

import com.zoho.perf.model.CalendarEvent;
import com.zoho.perf.model.CalendarEvent.EventStatus;
import com.zoho.perf.model.CalendarEvent.RecurrenceRule;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Generates realistic calendar event data for benchmarking.
 */
public class EventDataGenerator {
    private static final Random RANDOM = new Random(42); // Fixed seed for reproducibility

    private static final String[] MEETING_TYPES = {
            "Team Standup", "Sprint Planning", "Sprint Review", "Sprint Retrospective",
            "Client Meeting", "Design Review", "Code Review", "Architecture Discussion",
            "Quarterly Planning", "One-on-One", "All Hands Meeting", "Training Session",
            "Workshop", "Brainstorming Session", "Demo", "Town Hall", "Project Kickoff"
    };

    private static final String[] LOCATIONS = {
            "Conference Room A", "Conference Room B", "Meeting Room 1", "Meeting Room 2",
            "Zoom Meeting", "Google Meet", "Microsoft Teams", "Board Room",
            "Building 1 - Floor 3", "Building 2 - Floor 5", "Cafeteria", "Open Space Area",
            "Virtual - Teams", "Virtual - Zoom", "Executive Suite", "Training Room"
    };

    private static final String[] TIMEZONES = {
            "America/Los_Angeles", "America/New_York", "Europe/London", "Europe/Paris",
            "Asia/Tokyo", "Asia/Kolkata", "Australia/Sydney", "America/Chicago"
    };

    private static final String[] DOMAINS = {
            "zoho.com", "company.com", "enterprise.org", "tech.io", "startup.ai"
    };

    private static final String[] DESCRIPTION_TEMPLATES = {
            "This meeting is scheduled to discuss the progress on %s project. We will review the current status, address any blockers, and plan the next steps. Please come prepared with your updates and questions.",
            "Join us for an important discussion about %s. The agenda includes reviewing deliverables, aligning on priorities, and making key decisions. Your input and participation are crucial.",
            "We are organizing this session to cover %s topics. This is a great opportunity to collaborate, share insights, and move forward together. Looking forward to productive conversations.",
            "Quick sync on %s matters. We'll touch base on recent developments, clarify expectations, and ensure everyone is aligned. Please review the shared documents before the meeting.",
            "Comprehensive review of %s initiatives. We will deep dive into technical details, discuss implementation strategies, and resolve outstanding issues. Prepare your questions and feedback."
    };

    private static final String[] PROJECT_NAMES = {
            "Q4 2025 Roadmap", "Mobile App Redesign", "API Gateway Migration", "Data Pipeline Optimization",
            "Customer Portal Enhancement", "Security Compliance", "Cloud Infrastructure", "ML Model Training",
            "Performance Tuning", "User Experience Improvements", "Backend Refactoring", "Frontend Modernization"
    };

    /**
     * Generates a list of calendar events with realistic data.
     *
     * @param count number of events to generate
     * @return list of calendar events
     */
    public static List<CalendarEvent> generateEvents(int count) {
        List<CalendarEvent> events = new ArrayList<>(count);
        LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 9, 0);

        for (int i = 0; i < count; i++) {
            events.add(generateEvent(i, baseTime.plusDays(i / 10).plusHours(i % 24)));
        }

        return events;
    }

    /**
     * Generates a single calendar event with realistic data.
     */
    private static CalendarEvent generateEvent(int index, LocalDateTime startTime) {
        String id = UUID.randomUUID().toString();
        String title = MEETING_TYPES[RANDOM.nextInt(MEETING_TYPES.length)] + " - " + index;

        // Generate description with 100-500 characters
        String projectName = PROJECT_NAMES[RANDOM.nextInt(PROJECT_NAMES.length)];
        String descriptionTemplate = DESCRIPTION_TEMPLATES[RANDOM.nextInt(DESCRIPTION_TEMPLATES.length)];
        String description = String.format(descriptionTemplate, projectName);

        // Add extra padding to reach desired length
        while (description.length() < 100) {
            description += " Additional notes and context for this meeting.";
        }
        if (description.length() > 500) {
            description = description.substring(0, 500);
        }

        LocalDateTime endTime = startTime.plusHours(1);
        String location = LOCATIONS[RANDOM.nextInt(LOCATIONS.length)];

        // Generate 5-50 attendees
        int attendeeCount = 5 + RANDOM.nextInt(46);
        List<String> attendees = generateAttendees(attendeeCount);

        // Recurrence rule
        RecurrenceRule recurrenceRule = RecurrenceRule.values()[RANDOM.nextInt(RecurrenceRule.values().length)];

        // Generate 1-3 reminders
        int reminderCount = 1 + RANDOM.nextInt(3);
        List<Integer> reminders = new ArrayList<>();
        for (int i = 0; i < reminderCount; i++) {
            reminders.add(new int[] { 5, 10, 15, 30, 60 }[RANDOM.nextInt(5)]);
        }

        String timezone = TIMEZONES[RANDOM.nextInt(TIMEZONES.length)];
        String organizerEmail = "organizer" + (index % 100) + "@" + DOMAINS[RANDOM.nextInt(DOMAINS.length)];
        EventStatus status = EventStatus.values()[RANDOM.nextInt(EventStatus.values().length)];

        return new CalendarEvent(
                id, title, description, startTime, endTime, location,
                attendees, recurrenceRule, reminders, timezone, organizerEmail, status);
    }

    /**
     * Generates a list of attendee email addresses.
     */
    private static List<String> generateAttendees(int count) {
        List<String> attendees = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String firstName = "user" + RANDOM.nextInt(1000);
            String domain = DOMAINS[RANDOM.nextInt(DOMAINS.length)];
            attendees.add(firstName + "@" + domain);
        }
        return attendees;
    }
}
