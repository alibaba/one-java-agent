package com.alibaba.profiler;

import com.alibaba.oneagent.plugin.PluginActivator;
import com.alibaba.oneagent.plugin.PluginContext;

public class ProfilerActivator implements PluginActivator {

    @Override
    public boolean enabled(PluginContext context) {
        System.out.println("enabled " + this.getClass().getName());
        return true;
    }

    @Override
    public void init(PluginContext context) throws Exception {
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
