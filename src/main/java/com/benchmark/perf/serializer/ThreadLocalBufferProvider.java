package com.benchmark.perf.serializer;

import java.io.ByteArrayOutputStream;

/**
 * Provides reusable per-thread buffers to minimize allocations during
 * serialization hot paths.
 */
public final class ThreadLocalBufferProvider {

    private static final int DEFAULT_STRING_CAPACITY = 16_384;
    private static final int DEFAULT_BYTE_CAPACITY = 32_768;

    private static final ThreadLocal<StringBuilder> STRING_BUILDERS = ThreadLocal
            .withInitial(() -> new StringBuilder(DEFAULT_STRING_CAPACITY));

    private static final ThreadLocal<ByteArrayOutputStream> BYTE_STREAMS = ThreadLocal
            .withInitial(() -> new ByteArrayOutputStream(DEFAULT_BYTE_CAPACITY));

    private ThreadLocalBufferProvider() {
    }

    public static StringBuilder acquireStringBuilder() {
        StringBuilder builder = STRING_BUILDERS.get();
        builder.setLength(0);
        return builder;
    }

    public static void releaseStringBuilder(StringBuilder builder) {
        builder.setLength(0);
    }

    public static ByteArrayOutputStream acquireByteArrayOutputStream() {
        ByteArrayOutputStream stream = BYTE_STREAMS.get();
        stream.reset();
        return stream;
    }
}
