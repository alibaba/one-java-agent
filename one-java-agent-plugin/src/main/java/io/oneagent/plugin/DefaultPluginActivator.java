package io.oneagent.plugin;

/**
 * 当插件没有配置 PluginActivator 时，默认值
 * 
 * @author hengyunabc
 *
 */
public class DefaultPluginActivator implements PluginActivator {

    @Override
    public boolean enabled(PluginContext context) {
        return true;
    }

    @Override
    public void init(PluginContext context) throws Exception {

    }

    @Override
    public void start(PluginContext context) throws Exception {

    }

    @Override
    public void stop(PluginContext context) throws Exception {

    }

}
