package com.alibaba.oneagent.plugin;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.oneagent.plugin.config.PluginConfig;
import com.alibaba.oneagent.plugin.share.SharedService;

/**
 * <pre>
 * 是否要支持可选导出？
 * </pre>
 * 
 * @author hengyunabc 2019-02-27
 *
 */
public class PluginClassLoader extends URLClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(PluginClassLoader.class);

    private static LockProvider LOCK_PROVIDER = setupLockProvider();

    private List<String> importPackages;

    private PluginConfig pluginConfig;

    private SharedService sharedService;

    public PluginClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public PluginClassLoader(URL[] urls, ClassLoader parent, SharedService sharedService, PluginConfig pluginConfig) {
        super(urls, parent);
        this.sharedService = sharedService;
        List<String> packages = pluginConfig.importPackages();
        if (packages != null && !packages.isEmpty()) {
            this.importPackages = new ArrayList<String>(packages.size());
            for (String packageName : packages) {
                // 增加后缀，用于判断是否子package
                importPackages.add(packageName + ".");
            }
        }
        this.pluginConfig = pluginConfig;
        List<String> exportPackages = pluginConfig.exportPackages();
        if (exportPackages != null && !exportPackages.isEmpty()) {
            for (String p : exportPackages) {
                sharedService.registerClassLoader(p, this);
            }
        }
    }

    @Override
    public String toString() {
        if (pluginConfig != null) {
            String name = pluginConfig.getName();
            return "oneagent-" + name + "@" + Integer.toHexString(hashCode());
        }
        return super.toString();
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (LOCK_PROVIDER.getLock(this, name)) {

            // 1. loaded class
            final Class<?> loadedClass = findLoadedClass(name);
            if (loadedClass != null) {
                return loadedClass;
            }

            // 2. 优先从parent（SystemClassLoader）里加载系统类，避免抛出ClassNotFoundException
            if (name != null && (name.startsWith("sun.") || name.startsWith("java."))) {
                return super.loadClass(name, resolve);
            }

            // 3. import
            if (this.importPackages != null && !importPackages.isEmpty()) {
                // TODO 用map + 缓存 取更快，还是判断数组快
                for (String importPackage : importPackages) {
                    if (name.startsWith(importPackage)) {
                        try {
                            Class<?> clazz = this.sharedService.loadClass(name);
                            if (clazz != null) {
                                return clazz;
                            }
                        } catch (ClassNotFoundException e) {
                            // ignore
                            if (logger.isDebugEnabled()) {
                                logger.info("plugin: {} can not load class: {} from import package: {}",
                                        pluginConfig.getName(), name, importPackage);
                            }
                        }
                    }
                }
            }

            // 4. 从插件自身urls加载
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

    @Override
    public URL getResource(String name) {
        if (this.importPackages != null && !importPackages.isEmpty()) {
            URL resource = this.sharedService.getResource(name);
            if (resource != null) {
                return resource;
            }
        }
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (this.importPackages != null && !importPackages.isEmpty()) {
            // TODO 是否要有选项，只查找import，忽略自身的？
            Enumeration<URL> resources = this.sharedService.getResources(name);
            if (resources != null) {
                Enumeration<URL>[] tmp = (Enumeration<URL>[]) new Enumeration<?>[2];
                tmp[0] = resources;
                tmp[1] = super.getResources(name);
                return new CompoundEnumeration<URL>(tmp);
            }

        }
        return super.getResources(name);
    }

    /*
     * A utility class that will enumerate over an array of enumerations.
     * 
     * @see java.lang.CompoundEnumeration
     */
    final class CompoundEnumeration<E> implements Enumeration<E> {
        private final Enumeration<E>[] enums;
        private int index;

        public CompoundEnumeration(Enumeration<E>[] enums) {
            this.enums = enums;
        }

        private boolean next() {
            while (index < enums.length) {
                if (enums[index] != null && enums[index].hasMoreElements()) {
                    return true;
                }
                index++;
            }
            return false;
        }

        public boolean hasMoreElements() {
            return next();
        }

        public E nextElement() {
            if (!next()) {
                throw new NoSuchElementException();
            }
            return enums[index].nextElement();
        }
    }

    private static LockProvider setupLockProvider() {
        try {
            ClassLoader.registerAsParallelCapable();
            return new Java7LockProvider();
        } catch (Throwable ex) {
            return new LockProvider();
        }
    }

    /**
     * Strategy used to provide the synchronize lock object to use when loading
     * classes.
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
