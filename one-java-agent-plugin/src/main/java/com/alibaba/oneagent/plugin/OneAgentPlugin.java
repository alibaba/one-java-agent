package com.alibaba.oneagent.plugin;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.oneagent.plugin.properties.PropertiesInjectUtils;
import com.alibaba.oneagent.utils.IOUtils;
import com.alibaba.oneagent.utils.PropertiesUtils;

/**
 *
 * @author hengyunabc 2019-02-28
 *
 */
public class OneAgentPlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(PluginManagerImpl.class);

    public static final int DEFAULT_ORDER = 1000;

    private URL location;

    private ClassLoader parentClassLoader;
    private PlguinClassLoader classLoader;

    private PluginConfig pluginConfig;

    private volatile PluginState state;

    private PluginActivator pluginActivator;

    private PluginContext pluginContext;

    public OneAgentPlugin(URL location, Instrumentation instrumentation, ClassLoader parentClassLoader,
            Properties gobalProperties) throws PluginException {
        this(location, Collections.<URL>emptySet(), instrumentation, parentClassLoader, gobalProperties);
    }

    public OneAgentPlugin(URL location, Set<URL> extraURLs, Instrumentation instrumentation,
            ClassLoader parentClassLoader, Properties gobalProperties) throws PluginException {

        this.location = location;
        this.parentClassLoader = parentClassLoader;
        this.state = PluginState.NONE;

        File pluginPropertiesFile = new File(location.getFile(), "plugin.properties");
        Properties properties = PropertiesUtils.loadOrNull(pluginPropertiesFile);
        if (properties == null) {
            throw new PluginException("load plugin properties error, path: " + pluginPropertiesFile.getAbsolutePath());
        }

        Properties tmp = new Properties();
        tmp.putAll(gobalProperties);
        tmp.putAll(properties);
        properties = tmp;

        pluginConfig = new PluginConfig();
        PropertiesInjectUtils.inject(properties, pluginConfig);

        String classpath = pluginConfig.getClasspath();

        List<URL> urls = new ArrayList<URL>();
        urls.addAll(extraURLs);

        urls.addAll(scanPluginUrls(classpath));

        classLoader = new PlguinClassLoader(urls.toArray(new URL[0]), parentClassLoader);

        this.pluginContext = new PluginContextImpl(this, instrumentation, properties);
    }

    @Override
    public boolean enabled() throws PluginException {
        boolean enabled = false;
        try {
            Class<?> activatorClass = classLoader.loadClass(pluginConfig.getPluginActivator());
            pluginActivator = (PluginActivator) activatorClass.newInstance();
            enabled = pluginActivator.enabled(pluginContext);
            if (enabled) {
                this.state = PluginState.ENABLED;
            } else {
                this.state = PluginState.DISABLED;
                logger.info("plugin {} disabled.", this.pluginConfig.getName());
            }

        } catch (Throwable e) {
            this.state = PluginState.ERROR;
            throw new PluginException("check enabled plugin error, plugin name: " + pluginConfig.getName(), e);
        }
        return enabled;
    }

    @Override
    public void init() throws PluginException {
        try {
            pluginActivator.init(pluginContext);
        } catch (Throwable e) {
            this.state = PluginState.ERROR;
            throw new PluginException("init plugin error, plugin name: " + pluginConfig.getName(), e);
        }
    }

    @Override
    public void start() throws PluginException {
        try {
            pluginActivator.start(pluginContext);
        } catch (Throwable e) {
            this.state = PluginState.ERROR;
            throw new PluginException("start plugin error, plugin name: " + pluginConfig.getName(), e);
        }
    }

    @Override
    public void stop() throws PluginException {
        try {
            pluginActivator.stop(pluginContext);
        } catch (Throwable e) {
            this.state = PluginState.ERROR;
            throw new PluginException("stop plugin error, plugin name: " + pluginConfig.getName(), e);
        }
    }

    public void uninstall() {
        // close classloader, 清理资源
        IOUtils.close(this.classLoader);
        this.classLoader = null;
    }

    @Override
    public String name() {
        return this.pluginConfig.getName();
    }

    @Override
    public PluginState state() {
        return this.state;
    }

    public void setState(PluginState state) {
        this.state = state;
    }

    @Override
    public URL location() {
        return location;
    }

    @Override
    public int order() {
        return pluginConfig.getOrder();
    }

    private List<URL> scanPluginUrls(String classpath) throws PluginException {
        List<URL> urls = new ArrayList<URL>();

        try {
            String pluginDir = location.getFile();

            String[] strings = classpath.split(":");
            for (String path : strings) {
                if (path.endsWith(".jar")) {
                    File file = new File(pluginDir, path);
                    urls.add(file.toURI().toURL());
                } else {
                    urls.addAll(scanDir(path));
                }
            }
        } catch (MalformedURLException e) {
            throw new PluginException("scan error, classpath: " + classpath, e);
        }
        return urls;
    }

    private List<URL> scanDir(String path) throws MalformedURLException {
        List<URL> urls = new ArrayList<URL>();
        File libDir = new File(location.getFile(), path);
        if (!libDir.exists()) {
            return urls;
        }
        File[] listFiles = libDir.listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.getName().endsWith(".jar")) {
                    urls.add(file.toURI().toURL());
                }
            }
        }

        return urls;
    }

}
