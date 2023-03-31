package io.oneagent;

import java.lang.instrument.Instrumentation;

import io.oneagent.plugin.PluginManager;
import io.oneagent.service.ComponentManager;

/**
 * 
 * @author hengyunabc 2020-11-12
 *
 */
public interface Agent {

    /**
     * init agent
     *
     * @param args
     * @param inst
     * @param premain
     */
    void init(final String args, final Instrumentation inst, boolean premain);

    /**
     * destroy agent
     */
    void destroy();

    /**
     * get PluginManager
     * @return
     */
    PluginManager pluginManager();

    /**
     * get ComponentManager
     * @return
     */
    ComponentManager componentManager();

}
