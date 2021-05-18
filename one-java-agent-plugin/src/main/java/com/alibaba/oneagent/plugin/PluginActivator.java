package com.alibaba.oneagent.plugin;

/**
 * @author hengyunabc 2019-03-01
 */
public interface PluginActivator {

    /**
     * 让插件本身判断是否要启动
     *
     * @param context
     * @return
     */
    boolean enabled(PluginContext context);

    /**
     * calling this method ,the {@link PluginState} is {@link PluginState#INITED}
     * {@linkplain PluginManager#initPlugins()}
     *
     * @param context
     * @throws Exception
     */
    void init(PluginContext context) throws Exception;

    /**
     * Before calling this method, the {@link PluginState} is
     * {@link PluginState#STARTING}, after calling, the {@link PluginState} is
     * {@link PluginState#STARTED}
     *
     * @param context
     */
    void start(PluginContext context) throws Exception;

    /**
     * Before calling this method, the {@link PluginState} is
     * {@link PluginState#STOPPING}, after calling, the {@link PluginState} is
     * {@link PluginState#STOPED}
     *
     * @param context
     */
    void stop(PluginContext context) throws Exception;
}
