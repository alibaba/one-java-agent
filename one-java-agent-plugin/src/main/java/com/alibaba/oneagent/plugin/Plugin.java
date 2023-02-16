package com.alibaba.oneagent.plugin;

import java.net.URL;

import com.alibaba.oneagent.plugin.config.PluginConfig;

/**
 * @author hengyunabc 2019-02-27
 */
public interface Plugin {

    /**
     * check if it is ready to start
     *
     * @return
     * @throws PluginException
     */
    boolean enabled() throws PluginException;

    /**
     * init plugin
     *
     * @throws PluginException
     */
    void init() throws PluginException;

    /**
     * start plugin
     *
     * @throws PluginException
     */
    void start() throws PluginException;

    /**
     * stop plugin
     *
     * @throws PluginException
     */
    void stop() throws PluginException;

    /**
     * get plugin start order
     *
     * @return
     */
    int order();

    /**
     * get plugin current state
     *
     * @return
     */
    PluginState state();

    /**
     * set plugin state
     *
     * @param state
     */
    void setState(PluginState state);

    /**
     * get plugin name
     *
     * @return
     */
    String name();

    /**
     * get plugin location
     *
     * @return
     */
    URL location();

    PluginConfig config();
}
