package com.alibaba.oneagent.plugin.classloader;

/**
 * 
 * @author hengyunabc 2023-01-13
 *
 */
public interface ClassFilter {

    boolean matched(String className);

    Class<?> loadClass(String className) throws ClassNotFoundException;

}