package io.oneagent.plugin;

import java.util.List;
import java.util.Properties;

/**
 * @author hengyunabc 2019-02-27
 */
public interface PluginManager {
    /**
     * scan all plugins in oneagent path and ext plugin
     *
     * @throws PluginException
     */
    void scanPlugins() throws PluginException;

    /**
     * plugin name has been added to oneagent plugins
     *
     * @param name
     * @return
     */
    boolean containsPlugin(String name);

    /**
     * find plugin  by name
     *
     * @param name
     * @return
     */
    Plugin findPlugin(String name);

    /**
     * find plugin by name,and start plugin
     *
     * @param name
     * @throws PluginException
     */
    void startPlugin(String name) throws PluginException;

    /**
     * find plugin by name,and uninstall plugin
     *
     * @param name
     */
    void uninstallPlugin(String name);

    /**
     * find plugin by name,and stop plugin
     *
     * @param name
     * @throws PluginException
     */
    void stopPlugin(String name) throws PluginException;

    /**
     * find plugin by name,and enable plugin,but not start
     *
     * @param name
     */
    void enablePlugin(String name);

    /**
     * get all plugins
     *
     * @return
     */
    List<Plugin> allPlugins();

    /**
     * call all plugin enable method
     */
    void enablePlugins();

    /**
     * call all plugin init  method
     *
     * @throws PluginException
     */
    void initPlugins() throws PluginException;

    /**
     * call all plugin start  method
     *
     * @throws PluginException
     */
    void startPlugins() throws PluginException;

    /**
     * call all plugin stop  method
     *
     * @throws PluginException
     */
    void stopPlugins() throws PluginException;

    /**
     * get pluginManager config
     *
     * @return
     */
    Properties properties();

}
