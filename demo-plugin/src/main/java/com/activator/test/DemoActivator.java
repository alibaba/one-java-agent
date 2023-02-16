package com.activator.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.bytekit.ByteKit;
import com.alibaba.fastjson.JSON;
import com.alibaba.oneagent.plugin.PluginActivator;
import com.alibaba.oneagent.plugin.PluginContext;
import com.alibaba.oneagent.plugin.config.BinderUtils;

public class DemoActivator implements PluginActivator {
    private Logger logger = LoggerFactory.getLogger(DemoActivator.class);
    private String name = this.getClass().getSimpleName();

    @Override
    public boolean enabled(PluginContext context) {
        System.out.println("enabled " + this.getClass().getName());
        System.err.println(this.getClass().getSimpleName() + ": " + JSON.toJSONString(this));

        System.err.println("bytekit url: " + ByteKit.class.getProtectionDomain().getCodeSource().getLocation());

        System.err.println("logger url: " + logger.getClass().getProtectionDomain().getCodeSource().getLocation());

        DemoConfig demoConfig = new DemoConfig();
        BinderUtils.inject(context.getPlugin().config(), demoConfig);

        System.err.println("demoConfig: " + demoConfig.getTestConfig());
        System.err.println("nestConfig: " + demoConfig.getNest().getNestConfig());

        logger.info("demo plugin started");

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
