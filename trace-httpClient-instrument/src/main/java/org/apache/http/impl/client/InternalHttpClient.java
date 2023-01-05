package org.apache.http.impl.client;

import static io.opentelemetry.semconv.trace.attributes.SemanticAttributes.NetTransportValues.IP_TCP;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.HttpContext;

import com.alibaba.bytekit.agent.inst.Instrument;
import com.alibaba.bytekit.agent.inst.InstrumentApi;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapSetter;
import io.opentelemetry.oneagent.Java8BytecodeBridge;
import io.opentelemetry.oneagent.TraceConfiguration;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

@Instrument(Class = "org.apache.http.impl.client.InternalHttpClient")
public abstract class InternalHttpClient {

    protected CloseableHttpResponse doExecute(final HttpHost target, final HttpRequest request,
            final HttpContext context) throws Throwable {
        if (target == null || request == null) {
            // illegal args, can't trace. ignore.
            return InstrumentApi.invokeOrigin();
        }

        int port = target.getPort();
        if (port <= 0) {
            if ("https".equals(target.getSchemeName().toLowerCase())) {
                port = 443;
            } else {
                port = 80;
            }
        }
        String host = target.getHostName();

        // flavor
        String flavor = request.getProtocolVersion().toString();
        if (flavor != null) {
            String httpProtocolPrefix = "HTTP/";
            if (flavor.startsWith(httpProtocolPrefix)) {
                flavor = flavor.substring(httpProtocolPrefix.length());
            }
        }

        // url
        String uri;
        if (request instanceof HttpUriRequest) {
            uri = request.getRequestLine().getUri();
        } else {
            try {
                uri = new URI(target.toURI() + request.getRequestLine().getUri()).toString();
            } catch (URISyntaxException e) {
                uri = null;
            }
        }

        String url = null;
        if (uri != null) {
            boolean isUrl = uri.toLowerCase().startsWith("http");
            if (isUrl) {
                url = uri;
            } else {
                StringBuffer buff = new StringBuffer();
                buff.append(target.getSchemeName().toLowerCase());
                buff.append("://");
                buff.append(host + ":" + port);
                buff.append(uri);
                url = buff.toString();
            }
        }

        // 创建span
        Tracer tracer = TraceConfiguration.getTracer();
        Span span = tracer.spanBuilder(uri).setSpanKind(SpanKind.CLIENT).setParent(Java8BytecodeBridge.currentContext())
                .startSpan();

        // 设置attributes
        span.setAttribute("component", "httpClient");
        span.setAttribute(SemanticAttributes.NET_TRANSPORT, IP_TCP);
        span.setAttribute(SemanticAttributes.HTTP_METHOD, request.getRequestLine().getMethod());
        span.setAttribute(SemanticAttributes.HTTP_FLAVOR, flavor);
        span.setAttribute(SemanticAttributes.NET_PEER_NAME, host);
        span.setAttribute(SemanticAttributes.NET_PEER_PORT, port);
        if (url != null) {
            span.setAttribute(SemanticAttributes.HTTP_URL, url);
        }

        // Set the context with the current span
        Scope scope = null;
        try {
            scope = span.makeCurrent();

            // context propagation
            TextMapSetter<HttpRequest> setter = new TraceTextMapSetter();

            GlobalOpenTelemetry.getPropagators().getTextMapPropagator().inject(Java8BytecodeBridge.currentContext(),
                    request, setter);

            // invoke origin
            CloseableHttpResponse response = InstrumentApi.invokeOrigin();

            if (response != null) {
                StatusLine responseStatusLine = response.getStatusLine();

                if (responseStatusLine != null) {
                    Integer statusCode = responseStatusLine.getStatusCode();

                    if (statusCode != null) {
                        span.setAttribute(SemanticAttributes.HTTP_STATUS_CODE, (long) statusCode);

                        if (statusCode < 100 || statusCode >= 400) {
                            span.setStatus(StatusCode.ERROR, "STATUS_CODE: " + Integer.toString(statusCode));
                        }
                    }
                }
            }
            return response;
        } catch (Throwable e) {

            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw e;

        } finally {
            span.end();
            scope.close();
        }
    }

};