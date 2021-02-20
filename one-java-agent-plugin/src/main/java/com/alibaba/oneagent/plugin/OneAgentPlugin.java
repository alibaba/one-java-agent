package com.alibaba.oneagent.plugin;

import java.io.File;
import java.io.IOException;
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

import com.alibaba.bytekit.asm.instrument.InstrumentConfig;
import com.alibaba.bytekit.asm.instrument.InstrumentParseResult;
import com.alibaba.bytekit.asm.instrument.InstrumentTemplate;
import com.alibaba.bytekit.asm.instrument.InstrumentTransformer;
import com.alibaba.oneagent.plugin.config.PluginConfigImpl;
import com.alibaba.oneagent.service.ComponentManager;
import com.alibaba.oneagent.service.TransformerManager;
import com.alibaba.oneagent.utils.IOUtils;
import com.alibaba.oneagent.utils.PropertiesUtils;

/**
 *
 * @author hengyunabc 2019-02-28
 *
 */
public class OneAgentPlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(OneAgentPlugin.class);

    public static final int DEFAULT_ORDER = 1000;

    private URL location;

    private ClassLoader parentClassLoader;
    private PlguinClassLoader classLoader;

    private PluginConfigImpl pluginConfig;

    private volatile PluginState state;

    private PluginActivator pluginActivator;

    private PluginContext pluginContext;

    private ComponentManager componentManager;

    public OneAgentPlugin(URL location, Instrumentation instrumentation, ComponentManager componentManager,
            ClassLoader parentClassLoader, Properties gobalProperties) throws PluginException {
        this(location, Collections.<URL>emptySet(), instrumentation, componentManager, parentClassLoader,
                gobalProperties);
    }

    public OneAgentPlugin(URL location, Set<URL> extraURLs, Instrumentation instrumentation,
            ComponentManager componentManager, ClassLoader parentClassLoader, Properties gobalProperties)
            throws PluginException {

        this.location = location;
        this.parentClassLoader = parentClassLoader;
        this.state = PluginState.NONE;
        this.componentManager = componentManager;

        File pluginPropertiesFile = new File(location.getFile(), "plugin.properties");
        Properties properties = PropertiesUtils.loadOrNull(pluginPropertiesFile);
        if (properties == null) {
            throw new PluginException("load plugin properties error, path: " + pluginPropertiesFile.getAbsolutePath());
        }

        pluginConfig = new PluginConfigImpl(gobalProperties, properties);

        String classpath = pluginConfig.getClasspath();

        List<URL> urls = new ArrayList<URL>();
        urls.addAll(extraURLs);

        urls.addAll(scanPluginUrls(classpath));

        classLoader = new PlguinClassLoader(urls.toArray(new URL[0]), parentClassLoader);

        this.pluginContext = new PluginContextImpl(this, instrumentation, componentManager, properties);
    }

    @Override
    public boolean enabled() throws PluginException {
        if (this.pluginConfig.isEnabled() == false) {
            return false;
        }
        //检查全局配置

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

            processInstrument();
        } catch (Throwable e) {
            this.state = PluginState.ERROR;
            throw new PluginException("init plugin error, plugin name: " + pluginConfig.getName(), e);
        }
    }

    private void processInstrument() throws IOException {
        InstrumentTemplate instrumentTemplate = new InstrumentTemplate();

        File instrumentLibDir = new File(this.location.getFile(), PluginConstants.INSTRUMENT_LIB);
        if (instrumentLibDir.exists() && instrumentLibDir.isDirectory()) {
            for (File file : instrumentLibDir.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".jar")) {
                    instrumentTemplate.addJarFile(file);
                }
            }
        }

        InstrumentParseResult instrumentParseResult = instrumentTemplate.build();

        List<InstrumentConfig> instrumentConfigs = instrumentParseResult.getInstrumentConfigs();
        if (instrumentConfigs == null || instrumentConfigs.isEmpty()) {
            return;
        }

        InstrumentTransformer instrumentTransformer = new InstrumentTransformer(instrumentParseResult);
        int order = pluginContext.getPlugin().order();

        this.componentManager.getComponent(TransformerManager.class).addTransformer(instrumentTransformer, true,
                order);
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
