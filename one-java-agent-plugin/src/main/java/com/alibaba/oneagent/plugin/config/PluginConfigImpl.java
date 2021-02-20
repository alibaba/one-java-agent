package com.alibaba.oneagent.plugin.config;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import com.alibaba.oneagent.env.MutablePropertySources;
import com.alibaba.oneagent.env.PropertiesPropertySource;
import com.alibaba.oneagent.env.PropertySourcesPropertyResolver;
import com.alibaba.oneagent.plugin.OneAgentPlugin;

/**
 * 
 * @author hengyunabc 2021-02-19
 *
 */
public class PluginConfigImpl extends AbstractPluginConfig {
    private static final Logger logger = LoggerFactory.getLogger(PluginConfigImpl.class);
    /**
     * 从system properties里抽取出插件自身的配置
     */
    public static final String FROM_SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME = "fromSystemProperties";
    /**
     * 从oneagent本身的全局配置 properties里抽取出插件自身的配置
     */
    public static final String FROM_GLOBAL_PROPERTIES_PROPERTY_SOURCE_NAME = "fromGlobalProperties";

    /**
     * plugin.properties里的配置
     */
    public static final String PLUGIN_PROPERTIES_PROPERTY_SOURCE_NAME = "pluginlProperties";

    public static final String PLUGIN_CONFIG_PREFIX = "oneagent.plugin.";

    /**
     * 全局禁止启动插件的配置项。oneagent.plugin.disabled=aaa,bbb,ccc
     */
    public static final String PLUGIN_DISABLED = "oneagent.plugin.disabled";

    private final MutablePropertySources propertySources = new MutablePropertySources();

    private boolean enabled = true;
    /**
     * 插件规范版本
     */
    private String specification;

    private String version;
    private String name;
    private String pluginActivator;
    /**
     * 多个之间用 : 分隔，不配置则默认值为 lib。路径是plugin location的相对位置
     */
    private String classpath = "lib";

    private int order = OneAgentPlugin.DEFAULT_ORDER;

    public PluginConfigImpl(Properties globalProperties, Properties pluginProperties) {
        this.name = pluginProperties.getProperty("name");
        if (this.name == null) {
            throw new IllegalArgumentException("plugin name can not be null, please check plugin config.");
        }
        propertyResolver = new PropertySourcesPropertyResolver(this.propertySources);

        Properties formSystemProperties = extractPluginProperties(System.getProperties());
        Properties fromGlobalProperties = extractPluginProperties(globalProperties);
        propertySources.addLast(
                new PropertiesPropertySource(FROM_SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, formSystemProperties));
        propertySources.addLast(
                new PropertiesPropertySource(FROM_GLOBAL_PROPERTIES_PROPERTY_SOURCE_NAME, fromGlobalProperties));
        propertySources.addLast(new PropertiesPropertySource(PLUGIN_PROPERTIES_PROPERTY_SOURCE_NAME, pluginProperties));

        version = this.propertyResolver.getProperty("version");
        pluginActivator = this.propertyResolver.getProperty("pluginActivator");
        classpath = this.propertyResolver.getProperty("classpath");
        specification = this.propertyResolver.getProperty("specification");
        Integer configOrder = this.propertyResolver.getProperty("order", Integer.class);
        if (configOrder != null) {
            this.order = configOrder;
        }

        Boolean configEnabled = this.propertyResolver.getProperty("enabled", Boolean.class);
        if (configEnabled != null) {
            this.enabled = configEnabled;
        }
    }

    private Properties extractPluginProperties(Properties properties) {
        String pluginName = this.getName();

        String prefix = PLUGIN_CONFIG_PREFIX + pluginName + ".";

        Properties result = new Properties();
        for (Entry<Object, Object> entry : properties.entrySet()) {
            Object key = entry.getKey();
            if (key instanceof String) {
                String keyStr = (String) key;
                if (keyStr.startsWith(prefix)) {
                    String newKey = keyStr.substring(prefix.length());
                    result.put(newKey, entry.getValue());
                }
            }
        }

        // 处理全局的disabled配置
        String disabledPlugins = properties.getProperty(PLUGIN_DISABLED);
        if (disabledPlugins != null) {
            String[] plugins = disabledPlugins.split(",");
            for (String plugin : plugins) {
                if (pluginName.equals(plugin.trim())) {
                    result.put("enabled", "false");
                    logger.debug("plugin {} disabled by property: {}, value: {}", pluginName, PLUGIN_DISABLED,
                            disabledPlugins);
                }
            }
        }

        return result;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public String getPluginActivator() {
        return pluginActivator;
    }

    @Override
    public String getClasspath() {
        return classpath;
    }

    @Override
    public String getSpecification() {
        return specification;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

}
