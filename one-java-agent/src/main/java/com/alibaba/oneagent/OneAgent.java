package com.alibaba.oneagent;

import java.lang.instrument.Instrumentation;

/**
 * 
 * @author hengyunabc 2020-07-28
 *
 */
public class OneAgent {

    private static volatile Agent AGENT = NopAgent.INSTANCE;

    public static void premain(String args, Instrumentation inst) {
        main(args, inst, true);
    }

    public static void agentmain(String args, Instrumentation inst) {
        main(args, inst, false);
    }

    public static void init(String args, Instrumentation inst) {
        main(args, inst, false);
    }

    public static void destory() {
        AGENT.destory();
    }

    private static synchronized void main(String args, Instrumentation inst, boolean premain) {
        if (NopAgent.INSTANCE == AGENT) {
            Agent agent = new AgentImpl();
            agent.init(args, inst, premain);
            AGENT = agent;
        }
    }

    public static Agent getAgent() {
        return AGENT;
    }

}
