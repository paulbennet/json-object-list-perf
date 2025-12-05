package com.benchmark.perf.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.benchmark.perf.model.CalendarEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Baseline Jackson databind serializer using cached ObjectWriter and
 * thread-local byte buffers.
 */
public class JacksonDatabindEventSerializer implements CalendarEventSerializer {

    public static final JacksonDatabindEventSerializer INSTANCE = new JacksonDatabindEventSerializer();

    private static final ObjectWriter WRITER;

    static {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        WRITER = mapper.writerFor(mapper.getTypeFactory().constructCollectionType(List.class, CalendarEvent.class));
    }

    private JacksonDatabindEventSerializer() {
    }

    @Override
    public String getName() {
        return "JacksonDatabind";
    }

    @Override
    public String serialize(List<CalendarEvent> events) {
        ByteArrayOutputStream baos = ThreadLocalBufferProvider.acquireByteArrayOutputStream();
        try {
            WRITER.writeValue(baos, events);
            return baos.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Jackson databind serialization failed", e);
        }
    }
}
