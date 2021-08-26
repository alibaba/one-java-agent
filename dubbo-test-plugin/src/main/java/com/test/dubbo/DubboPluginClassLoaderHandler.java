package com.test.dubbo;

import com.alibaba.oneagent.service.ClassLoaderHandler;

/**
 * 
 * @author hengyunabc 2021-08-26
 *
 */
public class DubboPluginClassLoaderHandler implements ClassLoaderHandler {

    @Override
    public Class<?> loadClass(String name) {
        if (name.startsWith("com.test.dubbo")) {
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
