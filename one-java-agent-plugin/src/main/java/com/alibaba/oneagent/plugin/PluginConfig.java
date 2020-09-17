package com.alibaba.oneagent.plugin;

/**
 *
 * @author hengyunabc 2019-02-27
 *
 */
public class PluginConfig {

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getPluginActivator() {
        return pluginActivator;
    }

    public void setPluginActivator(String pluginActivator) {
        this.pluginActivator = pluginActivator;
    }

    public String getClasspath() {
        return classpath;
    }

    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

}
