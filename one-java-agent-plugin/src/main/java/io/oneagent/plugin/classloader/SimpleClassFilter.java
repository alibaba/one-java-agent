package io.oneagent.plugin.classloader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author hengyunabc 2023-01-13
 *
 */
public class SimpleClassFilter implements ClassFilter {

    private ClassLoader classLoader = SimpleClassFilter.class.getClassLoader();

    private List<String> packages = new ArrayList<String>();

    public SimpleClassFilter(Collection<String> packages) {
        for (String p : packages) {
            if (!p.endsWith(".")) {
                p = p + ".";
            }
            this.packages.add(p);
        }
    }

    @Override
    public boolean matched(String className) {
        for (String p : packages) {
            if (className.startsWith(p)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return classLoader.loadClass(className);
    }

}
