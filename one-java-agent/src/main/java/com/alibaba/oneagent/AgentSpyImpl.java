package com.alibaba.oneagent;

import java.oneagent.AgentSpy;
import java.oneagent.AgentSpyBridge;

import com.alibaba.oneagent.service.ClassLoaderHandlerManager;
import com.alibaba.oneagent.service.ComponentManager;

/**
 * 
 * @author hengyunabc 2020-12-18
 *
 */
public class AgentSpyImpl implements AgentSpy {
    private ClassLoaderHandlerManager classLoaderHandlerManager;

    public AgentSpyImpl(ClassLoaderHandlerManager classLoaderHandlerManager) {
        this.classLoaderHandlerManager = classLoaderHandlerManager;
    }

    @Override
    public Class<?> loadClass(String name) {
        return classLoaderHandlerManager.loadClass(name);
    }

    static void initAgentSpy(ComponentManager componentManager) {
        ClassLoaderHandlerManager classLoaderHandlerManager = componentManager
                .getComponent(ClassLoaderHandlerManager.class);
        AgentSpyBridge.spy(new AgentSpyImpl(classLoaderHandlerManager));
    }
}
