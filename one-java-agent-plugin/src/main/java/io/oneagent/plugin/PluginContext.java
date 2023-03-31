package io.oneagent.plugin;

import java.lang.instrument.Instrumentation;

import io.oneagent.service.ComponentManager;

/**
 * @author hengyunabc 2019-03-01
 */
public interface PluginContext {

    /**
     * get this plugin
     *
     * @return
     */
    Plugin getPlugin();

    /**
     * get plugin key  property
     *
     * @param key
     * @return
     */
    String getProperty(String key);

    /**
     * get Instrumentation
     *
     * @return
     */
    Instrumentation getInstrumentation();

    /**
     * get ComponentManager
     *
     * @return
     */
    ComponentManager getComponentManager();
}
