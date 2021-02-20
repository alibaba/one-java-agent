package com.alibaba.oneagent.plugin.config;

import com.alibaba.oneagent.env.PropertyResolver;

/**
 * 
 * @author hengyunabc 2021-02-19
 *
 */
public interface PluginConfig extends PropertyResolver {

    public boolean isEnabled();

    public String getVersion();

    public String getName();

    public int getOrder();

    public String getPluginActivator();

    public String getClasspath();

    public String getSpecification();
}
