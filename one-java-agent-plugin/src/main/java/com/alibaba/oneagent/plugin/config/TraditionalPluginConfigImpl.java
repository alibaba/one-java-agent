package com.alibaba.oneagent.plugin.config;

import java.util.Properties;

/**
 * <pre>
 * 支持的配置项：
 * agentJarPath=xxx.jar
 * appendToSystemClassLoaderSearch=
 * agentInitMethod=premain/agentmain
 * agentArgs=
 * enabled=
 * </pre>
 *
 * @author hengyunabc 2020-07-28
 */

public class TraditionalPluginConfigImpl extends PluginConfigImpl {

    private String agentJarPath;

    private String agentInitMethod;

    private boolean appendToSystemClassLoaderSearch;

    private String agentArgs;


    public TraditionalPluginConfigImpl(Properties globalProperties, Properties pluginProperties) {
        super(globalProperties, pluginProperties);

        this.agentJarPath = this.propertyResolver.getProperty("agentJarPath");

        this.agentArgs = this.propertyResolver.getProperty("agentArgs");

        this.agentInitMethod = this.propertyResolver.getProperty("agentInitMethod", "premain");

        this.appendToSystemClassLoaderSearch = this.propertyResolver.getProperty("appendToSystemClassLoaderSearch", Boolean.class, Boolean.TRUE);

    }


    public String getAgentJarPath() {
        return agentJarPath;
    }

    public boolean isAppendToSystemClassLoaderSearch() {
        return appendToSystemClassLoaderSearch;
    }

    public String getAgentArgs() {
        return agentArgs;
    }

    public String getAgentInitMethod() {
        return agentInitMethod;
    }

    public void setAgentInitMethod(String agentInitMethod) {
        this.agentInitMethod = agentInitMethod;
    }
}
