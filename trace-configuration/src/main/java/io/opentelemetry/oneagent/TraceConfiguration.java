package io.opentelemetry.oneagent;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public class TraceConfiguration {

    private static Tracer tracer = null;

    private static Span parentSpan = null;

    private static Scope scope = null;

    public static synchronized Tracer getTracer() throws IOException {
        if (tracer == null) {
            // 初始化openTelemetry
            Properties prop = new Properties();
            prop.load(TraceConfiguration.class.getResourceAsStream("/trace.properties"));

            String jaegerEndpoint = System.getenv("JAEGER_ENDPOINT");
            if (jaegerEndpoint == null) {
                jaegerEndpoint = "http://127.0.0.1:14250";
            }
                
            String appName = prop.getProperty("appName").trim();

            OpenTelemetry openTelemetry = initOpenTelemetry(jaegerEndpoint, appName);
            GlobalOpenTelemetry.set(openTelemetry);

            // 设置tracer和parentSpan
            tracer = GlobalOpenTelemetry.getTracer("com.alibaba.oneagent.trace");
            try {
                parentSpan = tracer.spanBuilder("/").startSpan();
                scope = parentSpan.makeCurrent();
            } catch (Throwable t) {
                // 创建parentSpan失败
                parentSpan = null;
                scope = null;
            }
        }
        return tracer;
    }
    
    /**
     * Initialize an OpenTelemetry SDK with a Jaeger exporter and a SimpleSpanProcessor.
     *
     * @param jaegerEndpoint The endpoint of your Jaeger instance.
     * @return A ready-to-use {@link OpenTelemetry} instance.
     */
    public static OpenTelemetry initOpenTelemetry(String jaegerEndpoint, String appName) {
      // Export traces to Jaeger
      JaegerGrpcSpanExporter jaegerExporter =
          JaegerGrpcSpanExporter.builder()
              .setEndpoint(jaegerEndpoint)
              .setTimeout(30, TimeUnit.SECONDS)
              .build();
      Resource serviceNameResource =
          Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "bean-" + appName));

      // Set to process the spans by the Jaeger Exporter
      SdkTracerProvider tracerProvider =
          SdkTracerProvider.builder()
              .addSpanProcessor(SimpleSpanProcessor.create(jaegerExporter))
              .setResource(Resource.getDefault().merge(serviceNameResource))
              .build();
      OpenTelemetrySdk openTelemetry =
          OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();

      // it's always a good idea to shut down the SDK cleanly at JVM exit.
      Runtime.getRuntime().addShutdownHook(new Thread(tracerProvider::close));

      return openTelemetry;
    }
}