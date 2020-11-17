package com.alibaba.oneagent.plugin;

import java.lang.instrument.Instrumentation;

import com.alibaba.oneagent.service.TransformerManager;

/**
 *
 * @author hengyunabc 2019-03-01
 * 
 * TODO 改造为用泛型来取对象？
 *
 */
public interface PluginContext {

    Plugin getPlugin();

    String getProperty(String key);

    Instrumentation getInstrumentation();

    TransformerManager getTransformerManager();
}
