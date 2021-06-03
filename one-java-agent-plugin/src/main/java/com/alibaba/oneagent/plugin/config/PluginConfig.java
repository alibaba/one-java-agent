package com.alibaba.oneagent.plugin.config;

import java.util.List;

import com.alibaba.oneagent.env.PropertyResolver;

/**
 * @author hengyunabc 2021-02-19
 */
public interface PluginConfig extends PropertyResolver {
    /**
     * check if plugin config is ready to start
     *
     * @return
     */
    boolean isEnabled();

    /**
     * get plugin config version
     *
     * @return
     */
    String getVersion();

    /**
     * get plugin config name
     *
     * @return
     */
    String getName();

    /**
     * get plugin config start order
     *
     * @return
     */
    int getOrder();

    /**
     * get start plugin activator class name  插件激活类的名称
     *
     * @return
     */
    String getPluginActivator();

    /**
     * get plugin lib class path
     *
     * @return
     */
    String getClasspath();

    /**
     * get plugin 插件规范版本
     *
     * @return
     */
    String getSpecification();

    List<String> exportPackages();

    List<String> importPackages();
}
