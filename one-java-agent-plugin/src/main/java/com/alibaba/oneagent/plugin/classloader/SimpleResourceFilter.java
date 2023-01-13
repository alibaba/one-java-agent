package com.alibaba.oneagent.plugin.classloader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 
 * @author hengyunabc 2023-01-13
 *
 */
public class SimpleResourceFilter implements ResourceFilter {
    private ClassLoader classLoader = SimpleResourceFilter.class.getClassLoader();

    private List<String> packages = new ArrayList<String>();
    private Set<String> resources = new HashSet<String>();

    public SimpleResourceFilter(Collection<String> packages, Collection<String> resources) {
        if (packages != null) {
            for (String p : packages) {
                p = p.replace('.', '/');
                if (!p.endsWith("/")) {
                    p = p + "/";
                }
                this.packages.add(p);
            }
        }
        if (resources != null) {
            this.resources.addAll(resources);
        }
    }

    @Override
    public boolean matched(String name) {
        if (resources.contains(name)) {
            return true;
        }

        for (String p : packages) {
            if (name.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public URL getResource(String name) {
        return classLoader.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return classLoader.getResources(name);
    }

}
