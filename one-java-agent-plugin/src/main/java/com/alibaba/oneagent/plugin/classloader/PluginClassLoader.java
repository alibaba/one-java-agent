package com.alibaba.oneagent.plugin.classloader;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
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
    private static final String[] LOG_PACKAGES = { "org.slf4j", "org.apache.log4j", "ch.qos.logback",
            "org.apache.logging.log4j" };
    private static final String[] LOG_RESOURCES = { "log4j.properties", "log4j.xml", "logback.configurationFile",
            "logback.xml", "logback-test.xml", "logback.groovy",
            // https://logging.apache.org/log4j/2.x/manual/configuration.html
            "log4j2-test.properties", "log4j2-test.yaml", "log4j2-test.yml", "log4j2-test.json", "log4j2-test.jsn",
            "log4j2-test.xml", "log4j2.properties", "log4j2.yaml", "log4j2.yml", "log4j2.json", "log4j2.jsn",
            "log4j2.xml", };

    private static final Logger logger = LoggerFactory.getLogger(PluginClassLoader.class);

    private static final ClassLoader EXTCLASSLOADER = ClassLoader.getSystemClassLoader().getParent();

    private List<String> importPackages;

    private PluginConfig pluginConfig;

    private SharedService sharedService;

    /**
     * 优先加载
     */
    private ClassFilter classFilter;

    /**
     * 优先加载的
     */
    private ResourceFilter resourceFilter;

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

        if (pluginConfig.isLogIsolation()) {
            this.classFilter = new SimpleClassFilter(Arrays.asList(LOG_PACKAGES));
            this.resourceFilter = new SimpleResourceFilter(Arrays.asList(LOG_PACKAGES), Arrays.asList(LOG_RESOURCES));
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

        // 1. loaded class
        final Class<?> loadedClass = findLoadedClass(name);
        if (loadedClass != null) {
            return loadedClass;
        }

        // 2. 优先从parent（SystemClassLoader）里加载系统类，避免抛出ClassNotFoundException
        if (name != null && (name.startsWith("sun.") || name.startsWith("java."))) {
            return EXTCLASSLOADER.loadClass(name);
        }

        // 3. 显式配置优先从自身加载的
        if (classFilter != null && classFilter.matched(name)) {
            return classFilter.loadClass(name);
        }

        // 4. import
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

        // 5. 从插件自身urls加载
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

    @Override
    public URL getResource(String name) {
        // 优先加载
        URL resource = null;
        if (resourceFilter != null) {
            resource = resourceFilter.getResource(name);
            if (resource != null) {
                return resource;
            }
        }

        if (this.importPackages != null && !importPackages.isEmpty()) {
            resource = this.sharedService.getResource(name);
            if (resource != null) {
                return resource;
            }
        }
        return super.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        // 优先加载
        if (resourceFilter != null) {
            Enumeration<URL> fromFilter = resourceFilter.getResources(name);
            // TODO 是否要合并加载
            return fromFilter;
        }

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

    public ClassFilter getClassFilter() {
        return classFilter;
    }

    public void setClassFilter(ClassFilter classFilter) {
        this.classFilter = classFilter;
    }

    public ResourceFilter getResourceFilter() {
        return resourceFilter;
    }

    public void setResourceFilter(ResourceFilter resourceFilter) {
        this.resourceFilter = resourceFilter;
    }
}
