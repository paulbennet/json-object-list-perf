package com.benchmark.perf.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.benchmark.perf.model.CalendarEvent;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Gson-based serializer using a cached TypeToken.
 */
public class GsonEventSerializer implements CalendarEventSerializer {

    public static final GsonEventSerializer INSTANCE = new GsonEventSerializer();

    private static final Type LIST_TYPE = new TypeToken<List<CalendarEvent>>() {
    }.getType();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final JsonSerializer<LocalDateTime> LOCAL_DATE_TIME_SERIALIZER = (src, typeOfSrc,
            context) -> src == null ? null : context.serialize(src.format(FORMATTER));

    private static final JsonDeserializer<LocalDateTime> LOCAL_DATE_TIME_DESERIALIZER = (json, typeOfT,
            context) -> json == null ? null : LocalDateTime.parse(json.getAsString(), FORMATTER);

    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_SERIALIZER)
            .registerTypeAdapter(LocalDateTime.class, LOCAL_DATE_TIME_DESERIALIZER)
            .create();

    private GsonEventSerializer() {
    }

    @Override
    public String getName() {
        return "Gson";
    }

    @Override
    public String serialize(List<CalendarEvent> events) {
        return GSON.toJson(events, LIST_TYPE);
    }
}
