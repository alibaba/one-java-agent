package com.alibaba.oneagent.plugin;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.oneagent.service.ComponentManager;
import com.alibaba.oneagent.utils.PropertiesUtils;

/**
 *
 * @author hengyunabc 2019-02-27
 *
 */
public class PluginManagerImpl implements PluginManager {
    private static final Logger logger = LoggerFactory.getLogger(PluginManagerImpl.class);

    private ClassLoader parentClassLoader = PluginManagerImpl.class.getClassLoader();
    private List<Plugin> plugins = new ArrayList<Plugin>();

    private Instrumentation instrumentation;

    private ComponentManager componentManager;

    private Properties properties;

    private List<URL> scanPluginLocations = new ArrayList<URL>();

    private List<URL> extPluginLocations = new ArrayList<URL>();

    public PluginManagerImpl(Instrumentation instrumentation, ComponentManager componentManager, Properties properties,
            URL scanPluginLocation) {
        this(instrumentation, componentManager, properties, scanPluginLocation, Collections.<URL>emptyList());
    }

    public PluginManagerImpl(Instrumentation instrumentation, ComponentManager componentManager, Properties properties,
            URL scanPluginLocation, List<URL> extPluginLocations) {
        this.instrumentation = instrumentation;
        this.componentManager = componentManager;
        this.properties = properties;
        this.scanPluginLocations.add(scanPluginLocation);
        this.extPluginLocations = extPluginLocations;
    }

    // 可能会执行多次
    @Override
    synchronized public void scanPlugins() throws PluginException {

        // 通过外部参数，可以只启动指定的插件
        Set<String> includePlugins = new HashSet<String>();
        String pluginsStr = properties.getProperty(PluginConstants.ONEAGENT_PLUGINS);
        if (pluginsStr != null) {
            String[] plugins = pluginsStr.split(",");
            for (String plugin : plugins) {
                plugin = plugin.trim();
                if (!plugin.isEmpty()) {
                    includePlugins.add(plugin);
                }
            }
        }
        try {
            for (URL scanLocation : scanPluginLocations) {
                File dir = new File(scanLocation.getFile());

                File[] files = dir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (!file.isHidden() && file.isDirectory()) {
                            // 判断是否在指定的插件里
                            if (!includePlugins.isEmpty() && !includePlugins.contains(file.getName())) {
                                // skip
                                continue;
                            }
                            loadPlugin(file);
                        }
                    }
                }
            }

            // 加载 ext 指定的插件
            for (URL pluginLocation : this.extPluginLocations) {
                loadPlugin(new File(pluginLocation.getFile()));
            }
        } catch (Throwable e) {
            throw new PluginException("scan plugins error.", e);
        }

        Collections.sort(plugins, new PluginComparator());
    }

    private void loadPlugin(File location) throws MalformedURLException, PluginException {
        // 判断插件的类型
        Plugin plugin = null;
        Properties pluginProperties = PropertiesUtils.loadOrNull(new File(location, PluginConstants.PLUGIN_PROPERTIES));

        if (pluginProperties == null) {
            logger.error("can not find {} in location: {}", PluginConstants.PLUGIN_PROPERTIES,
                    location.getAbsolutePath());
            return;
        }
        if (PluginConstants.TRADITIONAL_PLUGIN_TYPE.equalsIgnoreCase(pluginProperties.getProperty("type"))) {
            plugin = new TraditionalPlugin(location.toURI().toURL(), instrumentation, parentClassLoader, properties);

        } else {
            plugin = new OneAgentPlugin(location.toURI().toURL(), instrumentation, componentManager, parentClassLoader,
                    properties);
        }
        if (!containsPlugin(plugin.name())) {
            plugins.add(plugin);
        }
    }

    @Override
    synchronized public boolean containsPlugin(String name) {
        for (Plugin plugin : plugins) {
            if (plugin.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Plugin findPlugin(String name) {
        for (Plugin plugin : plugins) {
            if (plugin.name().equals(name)) {
                return plugin;
            }
        }
        return null;
    }

    @Override
    public void startPlugin(String name) throws PluginException {
        Plugin plugin = findPlugin(name);
        if (plugin != null) {
            if (plugin.state() == PluginState.NONE || plugin.state() == PluginState.STOPED) {
                plugin.enabled();
            }
            if (plugin.state() == PluginState.ENABLED) {
                updateState(plugin, PluginState.INITING);
                logger.info("Init plugin, name: {}", plugin.name());
                plugin.init();
                logger.info("Init plugin success, name: {}", plugin.name());
                updateState(plugin, PluginState.INITED);
            }

            if (plugin.state() == PluginState.INITED) {
                updateState(plugin, PluginState.STARTING);
                logger.info("Start plugin, name: {}", plugin.name());
                plugin.start();
                logger.info("Start plugin success, name: {}", plugin.name());
                updateState(plugin, PluginState.STARTED);
            }
        }
    }

    @Override
    public void uninstallPlugin(String name) {
        Plugin plugin = findPlugin(name);
        if (plugin != null && plugin.state() == PluginState.STOPED) {
            if (plugin instanceof OneAgentPlugin) {
                ((OneAgentPlugin) plugin).uninstall();
                this.plugins.remove(plugin);
            }
        }
    }

    @Override
    public void stopPlugin(String name) throws PluginException {
        Plugin plugin = findPlugin(name);
        if (plugin != null && plugin.state() == PluginState.STARTED) {
            updateState(plugin, PluginState.STOPPING);
            logger.info("Stop plugin, name: {}", plugin.name());
            plugin.stop();
            logger.info("Stop plugin success, name: {}", plugin.name());
            updateState(plugin, PluginState.STOPED);
        }
    }

    @Override
    public void enablePlugin(String name) {
        Plugin plugin = findPlugin(name);
        if (plugin != null && (plugin.state() == PluginState.DISABLED || plugin.state() == PluginState.NONE
                || plugin.state() == PluginState.STOPED)) {
            updateState(plugin, PluginState.ENABLED);
        }
    }

    private void updateState(Plugin plugin, PluginState state) {
        plugin.setState(state);
    }

    @Override
    synchronized public List<Plugin> allPlugins() {
        ArrayList<Plugin> result = new ArrayList<Plugin>(plugins.size());
        result.addAll(plugins);
        return result;
    }

    @Override
    synchronized public void enablePlugins() {
        for (Plugin plugin : plugins) {
            try {
                plugin.enabled();
            } catch (Throwable e) {
                logger.error("enabled plugin {} error.", plugin.name(), e);
            }
        }
    }

    @Override
    synchronized public void initPlugins() throws PluginException {
        logger.info("Init available plugins");
        parallelInitPlugins();
    }

    private void initOnePlugin(Plugin plugin) throws PluginException {
        if (plugin.state() == PluginState.ENABLED) {
            updateState(plugin, PluginState.INITING);
            logger.info("Init plugin, name: {}", plugin.name());
            plugin.init();
            logger.info("Init plugin success, name: {}", plugin.name());
            updateState(plugin, PluginState.INITED);
        } else {
            logger.debug("skip init plugin, name: {}, state: {}", plugin.name(), plugin.state());
        }
    }

    private void parallelInitPlugins() throws PluginException {
        // plugins本身已是有序
        Map<Integer, List<Plugin>> orderToPlugins = new LinkedHashMap<Integer, List<Plugin>>();
        for (Plugin plugin : plugins) {
            List<Plugin> list = orderToPlugins.get(plugin.order());
            if (list == null) {
                list = new ArrayList<Plugin>();
                orderToPlugins.put(plugin.order(), list);
            }
            list.add(plugin);
        }

        for (Entry<Integer, List<Plugin>> entry : orderToPlugins.entrySet()) {
            List<Plugin> sameOrderPlugins = entry.getValue();

            // init plugins
            final CountDownLatch latch = new CountDownLatch(sameOrderPlugins.size());
            final AtomicInteger successCounter = new AtomicInteger(0);
            for (final Plugin plugin : sameOrderPlugins) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            initOnePlugin(plugin);
                            successCounter.incrementAndGet();
                        } catch (Throwable e) {
                            logger.error("init plugin error, name: {}", plugin.name(), e);
                        } finally {
                            latch.countDown();
                        }
                    }
                }, "oneagent plugin " + plugin.name() + " init");
                thread.setDaemon(true);
                thread.start();
            }

            try {
                latch.await();
                if (successCounter.get() != sameOrderPlugins.size()) {
                    throw new PluginException("init plugin error, please check oneagent log");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new PluginException("init oneagent plugin error", e);
            }
        }
    }

    @Override
    synchronized public void startPlugins() throws PluginException {
        logger.info("Starting available plugins");
        parallelStartPlugins();
    }

    private void startOnePlugin(Plugin plugin) throws PluginException {
        if (plugin.state() == PluginState.INITED) {
            updateState(plugin, PluginState.STARTING);
            logger.info("Start plugin, name: {}", plugin.name());
            plugin.start();
            logger.info("Start plugin success, name: {}", plugin.name());
            updateState(plugin, PluginState.STARTED);
        } else {
            logger.debug("skip start plugin, name: {}, state: {}", plugin.name(), plugin.state());
        }
    }

    private void parallelStartPlugins() throws PluginException {
        // plugins本身已是有序
        Map<Integer, List<Plugin>> orderToPlugins = new LinkedHashMap<Integer, List<Plugin>>();
        for (Plugin plugin : plugins) {
            List<Plugin> list = orderToPlugins.get(plugin.order());
            if (list == null) {
                list = new ArrayList<Plugin>();
                orderToPlugins.put(plugin.order(), list);
            }
            list.add(plugin);
        }

        for (Entry<Integer, List<Plugin>> entry : orderToPlugins.entrySet()) {
            List<Plugin> sameOrderPlugins = entry.getValue();

            // start plugins
            final CountDownLatch latch = new CountDownLatch(sameOrderPlugins.size());
            final AtomicInteger successCounter = new AtomicInteger(0);
            for (final Plugin plugin : sameOrderPlugins) {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            startOnePlugin(plugin);
                            successCounter.incrementAndGet();
                        } catch (Throwable e) {
                            logger.error("start plugin error, name: {}", plugin.name(), e);
                        } finally {
                            latch.countDown();
                        }
                    }
                }, "oneagent plugin " + plugin.name() + " start");
                thread.setDaemon(true);
                thread.start();
            }

            try {
                latch.await();
                if (successCounter.get() != sameOrderPlugins.size()) {
                    throw new PluginException("start plugin error, please check oneagent log");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new PluginException("start oneagent plugin error", e);
            }
        }
    }

    @Override
    synchronized public void stopPlugins() throws PluginException {
        logger.info("Stopping available plugins");
        for (Plugin plugin : plugins) {
            if (plugin.state() == PluginState.STARTED) {
                updateState(plugin, PluginState.STOPPING);
                logger.info("Stop plugin, name: {}", plugin.name());
                plugin.stop();
                logger.info("Stop plugin success, name: {}", plugin.name());
                updateState(plugin, PluginState.STOPED);
            } else {
                logger.debug("skip stop plugin, name: {}, state: {}", plugin.name(), plugin.state());
            }

        }
    }

    @Override
    public Properties properties() {
        return this.properties;
    }

    public static class PluginComparator implements Comparator<Plugin> {

        @Override
        public int compare(Plugin p1, Plugin p2) {
            return p1.order() - p2.order();
        }

    }
}