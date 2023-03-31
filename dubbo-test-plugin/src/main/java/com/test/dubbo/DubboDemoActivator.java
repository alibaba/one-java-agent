package com.test.dubbo;

import io.oneagent.plugin.PluginActivator;
import io.oneagent.plugin.PluginContext;
import io.oneagent.service.ClassLoaderHandlerManager;

/**
 * 
 * @author hengyunabc
 *
 */
public class DubboDemoActivator implements PluginActivator {

    @Override
    public boolean enabled(PluginContext context) {
        System.out.println("enabled " + this.getClass().getName());
        return true;
    }

    @Override
    public void init(PluginContext context) throws Exception {
        // 注册自定义的ClassLoaderHandler，让被增强的类可以加载到指定的类
        ClassLoaderHandlerManager loaderHandlerManager = context.getComponentManager().getComponent(ClassLoaderHandlerManager.class);
        loaderHandlerManager.addHandler(new DubboPluginClassLoaderHandler());
        System.out.println("init " + this.getClass().getName());
    }


    @Override
    public void start(PluginContext context) throws Exception {
        System.out.println("start " + this.getClass().getName());
    }

    @Override
    public void stop(PluginContext context) throws Exception {
        System.out.println("stop " + this.getClass().getName());
    }

}
