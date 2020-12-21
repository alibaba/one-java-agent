package com.alibaba.oneagent;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.oneagent.utils.FeatureCodec;

/**
 * 
 * @author hengyunabc 2020-12-18
 *
 */
public class AgentArgsUtils {

    public static String agentArgs() {
        URL location = AgentArgsUtils.class.getProtectionDomain().getCodeSource().getLocation();
        System.err.println(location);

        String file = location.getFile();

        File demoPluginDir = new File(file, "../../../demo-plugin/target/demo-plugin@0.0.1-SNAPSHOT");

        File demoAgentDir = new File(file, "../../../demo-agent/target/demo-agent@0.0.1-SNAPSHOT");

        File dubboDemoPluginDir = new File(file, "../../../dubbo-test-plugin/target/dubbo-test-plugin@0.0.1-SNAPSHOT");

        Map<String, String> map = new HashMap<String, String>();

        map.put(AgentImpl.ONEAGENT_EXTPLUGINS, demoPluginDir.getAbsolutePath() + "," + demoAgentDir.getAbsolutePath() + ","
                + dubboDemoPluginDir.getAbsolutePath());

        String args = FeatureCodec.DEFAULT_COMMANDLINE_CODEC.toString(map);

        return args;
    }
}
