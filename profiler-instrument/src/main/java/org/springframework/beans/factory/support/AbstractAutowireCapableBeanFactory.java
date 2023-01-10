package org.springframework.beans.factory.support;

import java.io.IOException;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.lang.Nullable;

import com.alibaba.bytekit.agent.inst.Instrument;
import com.alibaba.bytekit.agent.inst.InstrumentApi;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.oneagent.Java8BytecodeBridge;
import io.opentelemetry.oneagent.TraceConfiguration;

@Instrument(Class = "org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory")
public abstract class AbstractAutowireCapableBeanFactory extends AbstractBeanFactory
        implements AutowireCapableBeanFactory {

    public Object createBean(String beanName, RootBeanDefinition mbd, @Nullable Object[] args)
            throws BeanCreationException {

        Span span = null;

        Object o = null;

        Scope scope = null;
        try {
            Tracer tracer = TraceConfiguration.getTracer();
            System.err.println("tracer: " + tracer);
            Context context = Java8BytecodeBridge.currentContext();
            System.err.println("context: " + context);
            span = tracer.spanBuilder(beanName).setParent(context).startSpan();
            System.err.println("span: " + span);
            scope = span.makeCurrent();
            o = InstrumentApi.invokeOrigin();
            return o;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            span.end();
            scope.close();
        }

    }
}