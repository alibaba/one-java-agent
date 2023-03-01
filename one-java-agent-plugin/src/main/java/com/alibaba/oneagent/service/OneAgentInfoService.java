package com.alibaba.oneagent.service;

import java.util.Properties;

public interface OneAgentInfoService {
    public static final String APPNAME_KEY = "oneagent.appname";

    String appName();

    Properties config();

}
