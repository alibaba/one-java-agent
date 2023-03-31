package com.activator.test.json;

import com.alibaba.fastjson.JSON;

import io.oneagent.plugin.PluginActivator;
import io.oneagent.plugin.PluginContext;

public class FastjsonActivator implements PluginActivator {

    @Override
    public boolean enabled(PluginContext context) {
        System.out.println("enabled " + this.getClass().getName());
        String jsonString = JSON.toJSONString(this);
        System.err.println("jsonString: " + jsonString);
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
