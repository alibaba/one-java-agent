package io.oneagent.plugin.share;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * TODO 是否需要支持类似 org.eclipse.osgi.internal.hookregistry.ClassLoaderHook 机制
 * 
 * @author hengyunabc 2021-05-31
 *
 */
public interface SharedService {
    
    public Class<?> loadClass(String className) throws ClassNotFoundException;

    public Enumeration<URL> getResources(String name) throws IOException;

    public URL getResource(String name);

    /**
     * 注册指定package 和 classloader关联
     * @param packageName
     * @param classLoader
     */
    public void registerClassLoader(String packageName, ClassLoader classLoader);

    /**
     * 清除指定 classloader的所有注册记录
     * @param classLoader
     */
    public void unRegisterClassLoader(ClassLoader classLoader);

}
