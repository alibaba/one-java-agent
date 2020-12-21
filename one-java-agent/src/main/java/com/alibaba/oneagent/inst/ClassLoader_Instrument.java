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
        Class<?> clazz = AgentSpyBridge.spy().loadClass(name);
        if (clazz != null) {
            return clazz;
        }

        clazz = InstrumentApi.invokeOrigin();
        return clazz;
    }
}
