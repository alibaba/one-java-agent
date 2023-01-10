package io.opentelemetry.oneagent;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;

public final class Java8BytecodeBridge {
    public static Context currentContext() {
        return Context.current();
    }

    public static Span currentSpan() {
        return Span.current();
    }

    public static Span spanFromContext(Context context) {
        return Span.fromContext(context);
    }

    private Java8BytecodeBridge() {
    }
}