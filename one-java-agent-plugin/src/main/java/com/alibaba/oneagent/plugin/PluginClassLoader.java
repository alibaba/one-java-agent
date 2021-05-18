package com.alibaba.oneagent.plugin;

import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 * @author hengyunabc 2019-02-27
 *
 */
public class PluginClassLoader extends URLClassLoader {

    private static LockProvider LOCK_PROVIDER = setupLockProvider();

    public PluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (LOCK_PROVIDER.getLock(this, name)) {
            final Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass != null) {
                return loadedClass;
            }

            // 优先从parent（SystemClassLoader）里加载系统类，避免抛出ClassNotFoundException
            if (name != null && (name.startsWith("sun.") || name.startsWith("java."))) {
                return super.loadClass(name, resolve);
            }

            try {
                Class<?> aClass = findClass(name);
                if (resolve) {
                    resolveClass(aClass);
                }
                return aClass;
            } catch (Exception e) {
                // ignore
            }
            return super.loadClass(name, resolve);
        }
    }

    private static LockProvider setupLockProvider() {
        try {
            ClassLoader.registerAsParallelCapable();
            return new Java7LockProvider();
        }
        catch (Throwable ex) {
            return new LockProvider();
        }
    }

    /**
     * Strategy used to provide the synchronize lock object to use when loading classes.
     */
    private static class LockProvider {

        public Object getLock(PluginClassLoader classLoader, String className) {
            return classLoader;
        }

    }

    /**
     * Java 7 specific {@link LockProvider}.
     */
    private static class Java7LockProvider extends LockProvider {

        @Override
        public Object getLock(PluginClassLoader classLoader, String className) {
            return classLoader.getClassLoadingLock(className);
        }

    }
}
