package com.alibaba.oneagent.plugin;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hengyunabc 2019-02-27
 */
public class PlguinClassLoader extends URLClassLoader {

    private final ConcurrentHashMap<String, String> parallelLockMap = new ConcurrentHashMap<String, String>();

    public PlguinClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
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

    /**
     * return the lock of class loading
     *
     * @param className the class name
     * @return the lock object
     */
    protected Object getClassLoadingLock(String className) {
        Object lock = this;
        if (parallelLockMap != null) {
            String newLock = new String(className);
            lock = parallelLockMap.putIfAbsent(className, newLock);
            if (lock == null) {
                lock = newLock;
            }
        }
        return lock;
    }

}
