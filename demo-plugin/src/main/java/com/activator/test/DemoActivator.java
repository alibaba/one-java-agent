package com.activator.test;

import com.alibaba.oneagent.plugin.PluginActivator;
import com.alibaba.oneagent.plugin.PluginContext;

public class DemoActivator implements PluginActivator {

    @Override
    public boolean enabled(PluginContext context) {
        System.out.println("enabled TestActivator");
        return true;
    }

    @Override
    public void init(PluginContext context) throws Exception {
        System.out.println("init TestActivator");
    }

    @Override
    public void start(PluginContext context) throws Exception {
        System.out.println("start TestActivator");
    }

    @Override
    public void stop(PluginContext context) throws Exception {
        System.out.println("stop TestActivator");
    }

}
