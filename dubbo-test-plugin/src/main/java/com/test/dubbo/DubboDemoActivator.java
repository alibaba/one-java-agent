package com.test.dubbo;

import com.alibaba.bytekit.asm.instrument.InstrumentParseResult;
import com.alibaba.bytekit.asm.instrument.InstrumentTemplate;
import com.alibaba.bytekit.asm.instrument.InstrumentTransformer;
import com.alibaba.oneagent.OneAgent;
import com.alibaba.oneagent.plugin.PluginActivator;
import com.alibaba.oneagent.plugin.PluginContext;

/**
 * 
 * @author hengyunabc
 *
 */
public class DubboDemoActivator implements PluginActivator {

    @Override
    public boolean enabled(PluginContext context) {
        System.out.println("enabled DubboDemoActivator");
        return true;
    }

    @Override
    public void init(PluginContext context) throws Exception {
        System.out.println("init DubboDemoActivator");
//        int order = context.getPlugin().order();
//        processInstrument(order);
    }

//    private void processInstrument(int order) {
//        InstrumentTemplate instrumentTemplate = new InstrumentTemplate();
//        instrumentTemplate.setTargetClassLoader(this.getClass().getClassLoader());
//        InstrumentParseResult instrumentParseResult = instrumentTemplate.build();
//
//        InstrumentTransformer instrumentTransformer = new InstrumentTransformer(instrumentParseResult);
//
//        OneAgent.getAgent().transformerManager().addTransformer(instrumentTransformer, true, order);
//
//    }

    @Override
    public void start(PluginContext context) throws Exception {
        System.out.println("start DubboDemoActivator");
    }

    @Override
    public void stop(PluginContext context) throws Exception {
        System.out.println("stop DubboDemoActivator");
    }

}
