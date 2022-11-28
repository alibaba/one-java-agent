package com.alibaba.oneagent.inst;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class InstrumentationInvocationHandler implements InvocationHandler {
    private final Instrumentation instrumentation;

    private final Object lockForAppend = new Object();

    public InstrumentationInvocationHandler(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("appendToBootstrapClassLoaderSearch") ||
                method.getName().equals("appendToSystemClassLoaderSearch")
        ) {
            synchronized (lockForAppend) {
                return method.invoke(instrumentation, args);
            }
        } else {
            return method.invoke(instrumentation, args);
        }
    }
}
