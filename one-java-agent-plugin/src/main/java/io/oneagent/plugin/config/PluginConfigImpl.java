package io.oneagent.plugin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.oneagent.env.MutablePropertySources;
import io.oneagent.env.PropertiesPropertySource;
import io.oneagent.env.PropertySourcesPropertyResolver;
import io.oneagent.plugin.DefaultPluginActivator;
import io.oneagent.plugin.OneAgentPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * @author hengyunabc 2021-02-19
 */
public class PluginConfigImpl extends AbstractPluginConfig {
    private static final Logger logger = LoggerFactory.getLogger(PluginConfigImpl.class);
    /**
     * 从system properties里抽取出插件自身的配置
     */
    private static final String FROM_SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME = "fromSystemProperties";
    /**
     * 从oneagent本身的全局配置 properties里抽取出插件自身的配置
     */
    private static final String FROM_GLOBAL_PROPERTIES_PROPERTY_SOURCE_NAME = "fromGlobalProperties";

    /**
     * plugin.properties里的配置
     */
    private static final String PLUGIN_PROPERTIES_PROPERTY_SOURCE_NAME = "pluginProperties";

    public static final String PLUGIN_CONFIG_PREFIX = "oneagent.plugin.";

    /**
     * 全局禁止启动插件的配置项。oneagent.plugin.disabled=aaa,bbb,ccc
     */
    public static final String PLUGIN_DISABLED = "oneagent.plugin.disabled";

    private final MutablePropertySources propertySources = new MutablePropertySources();

    /**
     * default is true
     */
    private boolean enabled = true;
    /**
     * 插件规范版本
     */
    private String specification;

    private String version;

    private String name;

    /**
     * Plugin Activator Class Name  插件激活类的名称
     */
    private String pluginActivator;
    /**
     * 多个之间用 : 分隔，不配置则默认值为 lib。路径是plugin location的相对位置
     */
    private String classpath = "lib";

    /**
     * default is {@linkplain OneAgentPlugin.DEFAULT_ORDER}
     */
    private int order = OneAgentPlugin.DEFAULT_ORDER;

    private List<String> importPackages;
    private List<String> exportPackages;

    /**
     * 插件日志隔离，保证从自身classloader加载日志类和配置
     */
    private boolean logIsolation;

    public PluginConfigImpl(Properties globalProperties, Properties pluginProperties) {
        this.name = pluginProperties.getProperty("name");
        if (this.name == null) {
            throw new IllegalArgumentException("plugin name can not be null, please check plugin config.");
        }
        this.propertyResolver = new PropertySourcesPropertyResolver(this.propertySources);

        Properties formSystemProperties = extractPluginProperties(System.getProperties());
        Properties fromGlobalProperties = extractPluginProperties(globalProperties);
        propertySources.addLast(
                new PropertiesPropertySource(FROM_SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME, formSystemProperties));
        propertySources.addLast(
                new PropertiesPropertySource(FROM_GLOBAL_PROPERTIES_PROPERTY_SOURCE_NAME, fromGlobalProperties));
        propertySources.addLast(new PropertiesPropertySource(PLUGIN_PROPERTIES_PROPERTY_SOURCE_NAME, pluginProperties));

        version = this.propertyResolver.getProperty("version");
        pluginActivator = this.propertyResolver.getProperty("pluginActivator", DefaultPluginActivator.class.getName());
        classpath = this.propertyResolver.getProperty("classpath", "lib");
        specification = this.propertyResolver.getProperty("specification");
        this.order = this.propertyResolver.getProperty("order", Integer.class, OneAgentPlugin.DEFAULT_ORDER);
        this.enabled = this.propertyResolver.getProperty("enabled", Boolean.class, Boolean.TRUE);
        this.importPackages = Arrays.asList(this.propertyResolver.getProperty("importPackages", String[].class, new String[0]));
        this.exportPackages = Arrays.asList(this.propertyResolver.getProperty("exportPackages", String[].class, new String[0]));
        this.logIsolation = this.propertyResolver.getProperty("logIsolation", Boolean.class, Boolean.FALSE);
    }

    /**
     * <pre>
     * 比如插件 aaa ，它可以在全局配置自己的一些属性，并且优先级比 plugin.properties要高。比如在 system properties里配置
     * oneagent.plugin.aaa.enabled=false
     * 
     * 等同于在 plugin.properties 里配置
     * enabled=false
     * </pre>
     * @param properties
     * @return
     */
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

    @Override
    public List<String> exportPackages() {
        return exportPackages;
    }

    @Override
    public List<String> importPackages() {
        return importPackages;
    }

    @Override
    public boolean isLogIsolation() {
        return logIsolation;
    }
}
