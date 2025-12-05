package com.zoho.perf.serializer;

import com.zoho.perf.model.CalendarEvent;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Moshi-based serializer using a cached adapter per JVM.
 */
public class MoshiEventSerializer implements CalendarEventSerializer {

    public static final MoshiEventSerializer INSTANCE = new MoshiEventSerializer();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final Moshi MOSHI = new Moshi.Builder()
            .add(LocalDateTime.class, new LocalDateTimeJsonAdapter())
            .build();
    private static final Type LIST_TYPE = Types.newParameterizedType(List.class, CalendarEvent.class);
    private static final JsonAdapter<List<CalendarEvent>> ADAPTER = MOSHI.adapter(LIST_TYPE);

    private MoshiEventSerializer() {
    }

    @Override
    public String getName() {
        return "Moshi";
    }

    @Override
    public String serialize(List<CalendarEvent> events) {
        return ADAPTER.toJson(events);
    }

    private static final class LocalDateTimeJsonAdapter extends JsonAdapter<LocalDateTime> {

        @Override
        public LocalDateTime fromJson(JsonReader reader) throws IOException {
            if (reader.peek() == JsonReader.Token.NULL) {
                reader.nextNull();
                return null;
            }
            return LocalDateTime.parse(reader.nextString(), FORMATTER);
        }

        @Override
        public void toJson(JsonWriter writer, LocalDateTime value) throws IOException {
            if (value == null) {
                writer.nullValue();
            } else {
                writer.value(value.format(FORMATTER));
            }
        }
    }
}
