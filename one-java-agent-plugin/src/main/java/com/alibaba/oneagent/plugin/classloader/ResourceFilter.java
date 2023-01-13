package com.alibaba.oneagent.plugin.classloader;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

/**
 * 
 * @author hengyunabc 2023-01-13
 *
 */
public interface ResourceFilter {

    boolean matched(String name);

    URL getResource(String name);

    Enumeration<URL> getResources(String name) throws IOException;
}