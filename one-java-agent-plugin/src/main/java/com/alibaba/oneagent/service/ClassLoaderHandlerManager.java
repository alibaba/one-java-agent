package com.alibaba.oneagent.service;

/**
 * 
 * @author hengyunabc 2020-12-09
 *
 */
public interface ClassLoaderHandlerManager {

    public void addHandler(ClassLoaderHandler handler);

    public void removeHandler(ClassLoaderHandler handler);

    public ClassLoaderHandler[] handlers();

    public Class<?> loadClass(String name);
}
