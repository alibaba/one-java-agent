package com.alibaba.oneagent;

import java.lang.instrument.Instrumentation;

import com.alibaba.oneagent.plugin.PluginManager;
import com.alibaba.oneagent.service.TransformerManager;

/**
 * 
 * @author hengyunabc 2020-11-12
 *
 */
public interface Agent {

    public void init(final String args, final Instrumentation inst, boolean premain);

    public void destory();

    public PluginManager pluginMaanger();

    public TransformerManager transformerManager();

}
