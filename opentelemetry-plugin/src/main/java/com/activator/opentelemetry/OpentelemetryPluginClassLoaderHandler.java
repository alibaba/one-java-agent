package com.activator.opentelemetry;

import io.oneagent.service.ClassLoaderHandler;

/**
 * 
 * @author hengyunabc 2021-09-13
 *
 */
public class OpentelemetryPluginClassLoaderHandler implements ClassLoaderHandler {

    @Override
    public Class<?> loadClass(String name) {
        if (name.startsWith("io.opentelemetry.")) {
            try {
                Class<?> clazz = this.getClass().getClassLoader().loadClass(name);
                return clazz;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
