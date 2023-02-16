package com.activator.test;

import com.alibaba.oneagent.plugin.config.Config;
import com.alibaba.oneagent.plugin.config.NestedConfig;

@Config
public class DemoConfig {

    private String testConfig;

    @NestedConfig
    private TestNestConfig nest;

    public String getTestConfig() {
        return testConfig;
    }

    public void setTestConfig(String testConfig) {
        this.testConfig = testConfig;
    }

    public TestNestConfig getNest() {
        return nest;
    }

    public void setNest(TestNestConfig nest) {
        this.nest = nest;
    }

}
