package com.alibaba.oneagent.plugin;

import java.util.List;
import java.util.Properties;

/**
 * 
 * @author hengyunabc 2019-02-27
 *
 */
public interface PluginManager {

	public void scanPlugins() throws PluginException;

	public boolean containsPlugin(String name);

	public Plugin findPlugin(String name);

	public void startPlugin(String name) throws PluginException;

	public void uninstallPlugin(String name);

	public void stopPlugin(String name) throws PluginException;

	public void enablePlugin(String name);

	public List<Plugin> allPlugins();

	public void enablePlugins();

	public void initPlugins() throws PluginException;

	public void startPlugins() throws PluginException;

	public void stopPlugins() throws PluginException;

	public Properties properties();

}
