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
 *
 */

public class TraditionalPluginConfigImpl extends PluginConfigImpl {
    public TraditionalPluginConfigImpl(Properties globalProperties, Properties pluginProperties) {
        super(globalProperties, pluginProperties);
        this.agentJarPath = this.propertyResolver.getProperty("agentJarPath");
        this.agentArgs = this.propertyResolver.getProperty("agentArgs");

        String configAgentInitMethod = this.propertyResolver.getProperty("agentInitMethod");
        if (configAgentInitMethod != null) {
            this.agentInitMethod = configAgentInitMethod;
        }

        Boolean configAppend = this.propertyResolver.getProperty("appendToSystemClassLoaderSearch", Boolean.class);
        if (configAppend != null) {
            this.appendToSystemClassLoaderSearch = configAppend;
        }
    }

    private String agentJarPath;
    private boolean appendToSystemClassLoaderSearch = true;
    private String agentArgs;

    private String agentInitMethod = "premain";

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
