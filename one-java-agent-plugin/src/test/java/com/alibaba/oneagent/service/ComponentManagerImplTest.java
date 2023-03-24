package com.alibaba.oneagent.service;

import java.lang.instrument.Instrumentation;
import java.util.Properties;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import net.bytebuddy.agent.ByteBuddyAgent;

/**
 * 
 * @author hengyunabc 2020-12-17
 *
 */
public class ComponentManagerImplTest {

    @Test
    public void test() {
        Instrumentation instrumentation = ByteBuddyAgent.install();
        ComponentManagerImpl componentManagerImpl = new ComponentManagerImpl(instrumentation);
        Properties properties = new Properties();
        properties.put(OneAgentInfoService.APPNAME_KEY, "helloApp");
        properties.put(OneAgentInfoService.VERSION_KEY, "0.0.1");
        componentManagerImpl.initComponents(properties);

        TransformerManager transformerManager = componentManagerImpl.getComponent(TransformerManager.class);
        Assertions.assertThat(transformerManager).isNotNull();

        OneAgentInfoService agentInfoService = componentManagerImpl.getComponent(OneAgentInfoService.class);
        Assertions.assertThat(agentInfoService.appName()).isEqualTo("helloApp");
        Assertions.assertThat(agentInfoService.version()).isEqualTo("0.0.1");
    }

}
