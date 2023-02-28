package com.alibaba.oneagent.plugin.share;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.oneagent.service.Component;

/**
 * TODO 并发时，能否保证清理干清记录
 * @author hengyunabc 2021-06-03
 *
 */
public class SharedServiceImpl implements SharedService, Component {
    private static final Logger logger = LoggerFactory.getLogger(SharedServiceImpl.class);
    private Map<String, ClassLoader> loaderMap = new ConcurrentHashMap<String, ClassLoader>();

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        String packageName = findClassPackage(className);
        if (packageName != null) {
            ClassLoader loader = loaderMap.get(packageName);
            if (loader != null) {
                return loader.loadClass(className);
            }

            for (Entry<String, ClassLoader> entry : loaderMap.entrySet()) {
                if (packageName.startsWith(entry.getKey())) {
                    // 增加 子package 对应的 classloader 的记录，下次不需要全匹配
                    loaderMap.put(packageName, entry.getValue());
                    return entry.getValue().loadClass(className);
                }
            }
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        String packageName = findResourcePackage(name);
        if (packageName != null) {
            ClassLoader loader = loaderMap.get(packageName);
            if (loader != null) {
                return loader.getResources(name);
            }

            for (Entry<String, ClassLoader> entry : loaderMap.entrySet()) {
                if (entry.getKey().startsWith(packageName)) {
                    // 增加 子package 对应的 classloader 的记录，下次不需要全匹配
                    loaderMap.put(packageName, entry.getValue());
                    return entry.getValue().getResources(name);
                }
            }
        }
        return null;

    }

    @Override
    public URL getResource(String name) {
        String packageName = findResourcePackage(name);
        if (packageName != null) {
            ClassLoader loader = loaderMap.get(packageName);
            if (loader != null) {
                return loader.getResource(name);
            }

            for (Entry<String, ClassLoader> entry : loaderMap.entrySet()) {
                if (entry.getKey().startsWith(packageName)) {
                    // 增加 子package 对应的 classloader 的记录，下次不需要全匹配
                    loaderMap.put(packageName, entry.getValue());
                    return entry.getValue().getResource(name);
                }
            }
        }
        return null;
    }

    @Override
    public void registerClassLoader(String packageName, ClassLoader classLoader) {
        if (!packageName.endsWith(".")) {
            packageName = packageName + ".";
        }
        loaderMap.put(packageName, classLoader);
        loaderMap.put(packageName.replace('.', '/'), classLoader);
    }

    @Override
    public void unRegisterClassLoader(ClassLoader classLoader) {
        List<Entry<String, ClassLoader>> toRemove = new ArrayList<Entry<String, ClassLoader>>();
        for (Entry<String, ClassLoader> entry : loaderMap.entrySet()) {
            if (entry.getValue().equals(classLoader)) {
                toRemove.add(entry);
            }
        }
        for (Entry<String, ClassLoader> entry : toRemove) {
            loaderMap.remove(entry.getKey());
        }
    }

    @Override
    public int order() {
        return 0;
    }

    @Override
    public void init() {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        loaderMap.clear();
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * com/test/abc return com/test/
     * @param name
     * @return
     */
    static String findResourcePackage(String name) {
        if (name == null) {
            return null;
        }
        int index = name.lastIndexOf('/');
        if (index != -1) {
            return name.substring(0, index + 1);
        } else {
            return null;
        }
    }

    /**
     * com.test.Hello return com.test.
     * @param className
     * @return
     */
    static String findClassPackage(String className) {
        if (className == null) {
            return null;
        }
        int index = className.lastIndexOf('.');
        if (index != -1) {
            return className.substring(0, index + 1);
        } else {
            return null;
        }
    }
}
