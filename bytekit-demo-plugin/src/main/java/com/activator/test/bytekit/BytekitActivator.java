package com.activator.test.bytekit;

import com.alibaba.bytekit.ByteKit;
import com.alibaba.oneagent.plugin.PluginActivator;
import com.alibaba.oneagent.plugin.PluginContext;

/**
 * 测试基它插件 import com.alibaba.bytekit 时，使用的是否本插件里的类
 * @author hengyunabc 2021-06-09
 *
 */
public class BytekitActivator implements PluginActivator {

    @Override
    public boolean enabled(PluginContext context) {
        System.out.println("enabled " + this.getClass().getName());
        ByteKit bytekit = new ByteKit();
        System.out.println("bytekit: " + bytekit.getClass().getProtectionDomain().getCodeSource().getLocation());
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
