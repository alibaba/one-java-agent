package org.apache.http.impl.client;

import org.apache.http.HttpRequest;

import io.opentelemetry.context.propagation.TextMapSetter;

public class TraceTextMapSetter implements TextMapSetter<HttpRequest> {

    @Override
    public void set(HttpRequest carrier, String key, String value) {
        // Insert the context as Header
        if (carrier != null) {
            carrier.setHeader(key, value);
        }
    }

}
