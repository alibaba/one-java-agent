package com.alibaba.oneagent.service;

import java.util.Properties;

public class OneAgentInfoServiceImpl implements OneAgentInfoService, Component {

    private Properties properties;

    @Override
    public void init(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String appName() {
        Object value = properties.get(APPNAME_KEY);
        if (value != null) {
            return value.toString();
        }
        return "unknown";
    }

    @Override
    public Properties config() {
        return properties;
    }

    @Override
    public String version() {
        Object value = properties.get(VERSION_KEY);
        if (value != null) {
            return value.toString();
        }
        return "unknown";
    }

}
