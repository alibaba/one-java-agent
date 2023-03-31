package io.oneagent;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Properties;

import io.oneagent.plugin.Plugin;
import io.oneagent.plugin.PluginException;
import io.oneagent.plugin.PluginManager;
import io.oneagent.service.ComponentManager;

/**
 *
 * @author hengyunabc 2020-11-12
 *
 */
public class NopAgent implements Agent {

    static final long startTime = System.nanoTime();

    static final NopAgent INSTANCE = new NopAgent();

    static final ComponentManager COMPONENT_MANAGER = new ComponentManager() {

        @Override
        public <T> T getComponent(Class<T> clazz) {
            return null;
        }

        @Override
        public void initComponents(Properties properties) {

        }

        @Override
        public void startComponents() {

        }

        @Override
        public void stopComponents() {

        }

    };

    static final PluginManager PLUGIN_MANAGER = new PluginManager() {

        @Override
        public void scanPlugins() throws PluginException {

        }

        @Override
        public boolean containsPlugin(String name) {
            return false;
        }

        @Override
        public Plugin findPlugin(String name) {
            return null;
        }

        @Override
        public void startPlugin(String name) throws PluginException {

        }

        @Override
        public void uninstallPlugin(String name) {

        }

        @Override
        public void stopPlugin(String name) throws PluginException {

        }

        @Override
        public void enablePlugin(String name) {

        }

        @Override
        public List<Plugin> allPlugins() {
            return null;
        }

        @Override
        public void enablePlugins() {

        }

        @Override
        public void initPlugins() throws PluginException {

        }

        @Override
        public void startPlugins() throws PluginException {

        }

        @Override
        public void stopPlugins() throws PluginException {

        }

        @Override
        public Properties properties() {
            return null;
        }

    };

    @Override
    public PluginManager pluginManager() {
        return PLUGIN_MANAGER;
    }

    @Override
    public void init(String args, Instrumentation inst, boolean premain) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public ComponentManager componentManager() {
        return COMPONENT_MANAGER;
    }

}
