package com.alibaba.oneagent.plugin;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.jar.JarFile;

public class InstrumentationWrapper implements Instrumentation{
    private final Instrumentation instrumentation;

    private final Object lockForAppend = new Object();

    public InstrumentationWrapper(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    public void addTransformer(ClassFileTransformer transformer, boolean canRetransform) {
        instrumentation.addTransformer(transformer, canRetransform);
    }

    @Override
    public void addTransformer(ClassFileTransformer transformer) {
        instrumentation.addTransformer(transformer);
    }

    @Override
    public boolean removeTransformer(ClassFileTransformer transformer) {
        return instrumentation.removeTransformer(transformer);
    }

    @Override
    public boolean isRetransformClassesSupported() {
        return instrumentation.isRetransformClassesSupported();
    }

    @Override
    public void retransformClasses(Class<?>... classes) throws UnmodifiableClassException {
        instrumentation.retransformClasses(classes);
    }

    @Override
    public boolean isRedefineClassesSupported() {
        return instrumentation.isRedefineClassesSupported();
    }

    @Override
    public void redefineClasses(ClassDefinition... definitions) throws ClassNotFoundException, UnmodifiableClassException {
        instrumentation.redefineClasses(definitions);
    }

    @Override
    public boolean isModifiableClass(Class<?> theClass) {
        return instrumentation.isModifiableClass(theClass);
    }

    @Override
    public Class[] getAllLoadedClasses() {
        return instrumentation.getAllLoadedClasses();
    }

    @Override
    public Class[] getInitiatedClasses(ClassLoader loader) {
        return instrumentation.getInitiatedClasses(loader);
    }

    @Override
    public long getObjectSize(Object objectToSize) {
        return instrumentation.getObjectSize(objectToSize);
    }

    @Override
    public void appendToBootstrapClassLoaderSearch(JarFile jarfile) {
        synchronized (lockForAppend) {
            appendToBootstrapClassLoaderSearch(jarfile);
        }
    }

    @Override
    public void appendToSystemClassLoaderSearch(JarFile jarfile) {
        synchronized (lockForAppend) {
            appendToSystemClassLoaderSearch(jarfile);
        }
    }

    @Override
    public boolean isNativeMethodPrefixSupported() {
        return instrumentation.isNativeMethodPrefixSupported();
    }

    @Override
    public void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix) {
        instrumentation.setNativeMethodPrefix(transformer, prefix);
    }
}
