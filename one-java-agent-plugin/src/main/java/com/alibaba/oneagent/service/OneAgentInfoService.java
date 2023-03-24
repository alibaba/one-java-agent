package com.alibaba.oneagent.service;

import java.util.Properties;

public interface OneAgentInfoService {
    public static final String APPNAME_KEY = "oneagent.appname";
    public static final String VERSION_KEY = "oneagent.version";

    String appName();

    String version();

    Properties config();

}
