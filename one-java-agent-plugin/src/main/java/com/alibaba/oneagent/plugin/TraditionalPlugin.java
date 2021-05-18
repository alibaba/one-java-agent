package com.alibaba.oneagent.plugin;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Properties;
import java.util.jar.JarFile;

import com.alibaba.oneagent.plugin.config.TraditionalPluginConfigImpl;
import com.alibaba.oneagent.utils.JarUtils;

/**
 * <pre>
 * 支持的配置项： 
 * agentJarPath=xxx.jar 
 * appendToSystemClassLoaderSearch=
 * agentClass=
 * premainClass=
 * agentArgs=
 * enabled=
 * </pre>
 * 
 * @author hengyunabc 2020-07-28
 *
 */
public class TraditionalPlugin implements Plugin {
    private URL location;

    private ClassLoader parentClassLoader;
    private PluginClassLoader classLoader;

    private volatile PluginState state;

    private Instrumentation instrumentation;

    private TraditionalPluginConfigImpl pluginConfig;

    public TraditionalPlugin(URL location, Instrumentation instrumentation, ClassLoader parentClassLoader,
            Properties gobalProperties) throws PluginException {
        this.location = location;
        this.parentClassLoader = parentClassLoader;
        this.state = PluginState.NONE;
        this.instrumentation = instrumentation;

        // 加载 plugin.properties
        File propertiesFile = new File(location.getFile(), "plugin.properties");
        if (!propertiesFile.exists()) {
            throw new PluginException("can not find plugin.properties in location:" + location);
        }

        Properties properties = new Properties();
        try {
            properties.load(propertiesFile.toURI().toURL().openStream());
        } catch (IOException e) {
            throw new PluginException("load plugin properties error, file: " + propertiesFile, e);
        }
        pluginConfig = new TraditionalPluginConfigImpl(gobalProperties, properties);
    }

    @Override
    public boolean enabled() throws PluginException {
        if (!this.pluginConfig.isEnabled()) {
            return false;
        }
        this.state = PluginState.ENABLED;
        return true;
    }

    @Override
    public void init() throws PluginException {

    }

    @Override
    public void start() throws PluginException {
        // 加载起来 agent jar，并反射调用启动

        File agentJarFile = new File(location.getFile(), pluginConfig.getAgentJarPath());
        if (!agentJarFile.exists()) {
            throw new PluginException("can not find agent jar::" + agentJarFile);
        }
        try {

            String className = null;
            String methodName = this.pluginConfig.getAgentInitMethod();
            if ("agentmain".equals(methodName)) {
                // 从jar包里读取出来
                className = JarUtils.read(agentJarFile).getValue("Agent-Class");
            } else {
                // 从jar包里读取出来
                className = JarUtils.read(agentJarFile).getValue("Premain-Class");
            }

            Class<?> clazz = null;
            if (this.pluginConfig.isAppendToSystemClassLoaderSearch()) {
                this.instrumentation.appendToSystemClassLoaderSearch(new JarFile(agentJarFile));
                this.parentClassLoader = ClassLoader.getSystemClassLoader();
            } else {
                this.parentClassLoader = new PluginClassLoader(new URL[] { agentJarFile.toURI().toURL() },
                        this.getClass().getClassLoader());
            }

            clazz = parentClassLoader.loadClass(className);

            // 反射调用启动
            Method method = clazz.getMethod(methodName, String.class, Instrumentation.class);
            method.invoke(null, this.pluginConfig.getAgentArgs(), this.instrumentation);
        } catch (Throwable e) {
            throw new PluginException("start error, agent jar::" + agentJarFile, e);
        }

    }

    @Override
    public void stop() throws PluginException {
        // TODO Auto-generated method stub

    }

    @Override
    public int order() {
        return this.pluginConfig.getOrder();
    }

    @Override
    public PluginState state() {
        return state;
    }

    @Override
    public void setState(PluginState state) {
        this.state = state;
    }

    @Override
    public String name() {
        return this.pluginConfig.getName();
    }

    @Override
    public URL location() {
        return this.location;
    }

}
