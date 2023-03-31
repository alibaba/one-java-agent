package io.oneagent.utils;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * This delegating wrapper of an {@link Instrumentation} instance.
 */
public class InstrumentationWrapper implements Instrumentation {
    protected final Instrumentation delegate;

    public InstrumentationWrapper(Instrumentation delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public void addTransformer(ClassFileTransformer transformer, boolean canRetransform) {
        delegate.addTransformer(transformer, canRetransform);
    }

    @Override
    public void addTransformer(ClassFileTransformer transformer) {
        delegate.addTransformer(transformer);
    }

    @Override
    public boolean removeTransformer(ClassFileTransformer transformer) {
        return delegate.removeTransformer(transformer);
    }

    @Override
    public boolean isRetransformClassesSupported() {
        return delegate.isRetransformClassesSupported();
    }

    @Override
    public void retransformClasses(Class<?>... classes) throws UnmodifiableClassException {
        delegate.retransformClasses(classes);
    }

    @Override
    public boolean isRedefineClassesSupported() {
        return delegate.isRedefineClassesSupported();
    }

    @Override
    public void redefineClasses(ClassDefinition... definitions)
            throws ClassNotFoundException, UnmodifiableClassException {
        delegate.redefineClasses(definitions);
    }

    @Override
    public boolean isModifiableClass(Class<?> theClass) {
        return delegate.isModifiableClass(theClass);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class[] getAllLoadedClasses() {
        return delegate.getAllLoadedClasses();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class[] getInitiatedClasses(ClassLoader loader) {
        return delegate.getInitiatedClasses(loader);
    }

    @Override
    public long getObjectSize(Object objectToSize) {
        return delegate.getObjectSize(objectToSize);
    }

    @Override
    synchronized public void appendToBootstrapClassLoaderSearch(JarFile jarfile) {
        delegate.appendToBootstrapClassLoaderSearch(jarfile);
    }

    @Override
    synchronized public void appendToSystemClassLoaderSearch(JarFile jarfile) {
        delegate.appendToSystemClassLoaderSearch(jarfile);
    }

    @Override
    public boolean isNativeMethodPrefixSupported() {
        return delegate.isNativeMethodPrefixSupported();
    }

    @Override
    public void setNativeMethodPrefix(ClassFileTransformer transformer, String prefix) {
        delegate.setNativeMethodPrefix(transformer, prefix);
    }

}