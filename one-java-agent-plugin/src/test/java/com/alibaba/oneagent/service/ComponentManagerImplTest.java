package com.alibaba.oneagent.service;

import java.lang.instrument.Instrumentation;

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
        componentManagerImpl.initComponents();

        TransformerManager transformerManager = componentManagerImpl.getComponent(TransformerManager.class);

        Assertions.assertThat(transformerManager).isNotNull();
    }

}
