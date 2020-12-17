package com.alibaba.oneagent.plugin;

import java.lang.instrument.Instrumentation;

import com.alibaba.oneagent.service.ComponentManager;

/**
 *
 * @author hengyunabc 2019-03-01
 * 
 *
 */
public interface PluginContext {

    Plugin getPlugin();

    String getProperty(String key);

    Instrumentation getInstrumentation();

    ComponentManager getcomponentManager();
}
