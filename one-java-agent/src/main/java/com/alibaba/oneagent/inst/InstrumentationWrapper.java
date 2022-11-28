package com.alibaba.oneagent.inst;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Proxy;

public class InstrumentationWrapper {
    public static Instrumentation newInstrumentationWrapper(Instrumentation instrumentation) {
        return (Instrumentation) Proxy.newProxyInstance(
                instrumentation.getClass().getClassLoader(),
                new Class[]{Instrumentation.class},
                new InstrumentationInvocationHandler(instrumentation));
    }
}
