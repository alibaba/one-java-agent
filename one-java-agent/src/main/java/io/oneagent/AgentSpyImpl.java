package io.oneagent;

import java.oneagent.AgentSpy;
import java.oneagent.AgentSpyBridge;

import io.oneagent.service.ClassLoaderHandlerManager;
import io.oneagent.service.ComponentManager;

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
        if (name.startsWith("java.")) {
            return null;
        }
        return classLoaderHandlerManager.loadClass(name);
    }

    static void initAgentSpy(ComponentManager componentManager) {
        ClassLoaderHandlerManager classLoaderHandlerManager = componentManager
                .getComponent(ClassLoaderHandlerManager.class);
        AgentSpyBridge.spy(new AgentSpyImpl(classLoaderHandlerManager));
    }
}
