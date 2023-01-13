package com.alibaba.oneagent.inst;

import java.oneagent.AgentSpyBridge;

import com.alibaba.bytekit.agent.inst.Instrument;
import com.alibaba.bytekit.agent.inst.InstrumentApi;

/**
 * @see java.lang.ClassLoader#loadClass(String)
 * @author hengyunabc 2020-11-30
 *
 */
@Instrument(Class = "java.lang.ClassLoader")
public abstract class ClassLoader_Instrument {
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz = null;

        boolean isOneAgentClass = false;
        if (name != null && name.startsWith("com.alibaba.oneagent.")) {
            isOneAgentClass = true;
        }

        // 对于one agent自身的类，要尝试用one agent classloader来加载
        if (isOneAgentClass) {
            try {
                ClassLoader classLoader;
                Object agentClassLoader = classLoader = AgentSpyBridge.spy().getClass().getClassLoader();
                if (this != agentClassLoader) {
                    clazz = classLoader.loadClass(name);
                }
            } catch (Throwable e) {
                // TODO: handle exception
            }
        }
        if (clazz != null) {
            return clazz;
        }

        /**
         * <pre>
         * 因为ClassLoaderHandler是由 PluginClassLoader 加载，为了避免死循环，
         * 因此如果当前ClassLoader是 PluginClassLoader ，则跳过 ClassLoaderHandlerManager 的处理
         * </pre>
         */
        boolean isPluginClassLoader = false;
        isPluginClassLoader = this.getClass().getName().equals("com.alibaba.oneagent.plugin.classloader.PluginClassLoader");

        if (!isPluginClassLoader) {
            clazz = AgentSpyBridge.spy().loadClass(name);
            if (clazz != null) {
                return clazz;
            }
        }

        clazz = InstrumentApi.invokeOrigin();
        return clazz;
    }
}
