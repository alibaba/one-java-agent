package com.alibaba.oneagent.plugin;

import java.lang.instrument.Instrumentation;
import java.util.Properties;

import com.alibaba.oneagent.service.ComponentManager;
import com.alibaba.oneagent.service.TransformerManager;

/**
 *
 * @author hengyunabc 2019-03-01
 *
 */
public class PluginContextImpl implements PluginContext {

    private Plugin plugin;

    private Properties properties;

    private Instrumentation instrumentation;
    
    private ComponentManager componentManager;

    public PluginContextImpl(Plugin plugin, Instrumentation instrumentation, ComponentManager componentManager, Properties properties) {
        this.plugin = plugin;
        this.instrumentation = instrumentation;
        this.componentManager = componentManager;
        this.properties = properties;
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    @Override
    public ComponentManager getcomponentManager() {
        return componentManager;
    }

}
