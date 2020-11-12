package com.alibaba.oneagent;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.rule.OutputCapture;

import com.alibaba.oneagent.utils.FeatureCodec;

import net.bytebuddy.agent.ByteBuddyAgent;

/**
 * 
 * @author hengyunabc 2020-09-17
 *
 */
public class OneAgentTest {
    @Rule
    public OutputCapture capture = new OutputCapture();

    @Test
    public void test() {

        URL location = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        System.err.println(location);

        String file = location.getFile();

        File demoPluginDir = new File(file, "../../../demo-plugin/target/demo-plugin@0.0.1-SNAPSHOT");

        File demoAgentDir = new File(file, "../../../demo-agent/target/demo-agent@0.0.1-SNAPSHOT");

        Instrumentation instrumentation = ByteBuddyAgent.install();

        Map<String, String> map = new HashMap<String, String>();

        map.put("oneagent.extPlugins", demoPluginDir.getAbsolutePath() + "," + demoAgentDir.getAbsolutePath());

        String args = FeatureCodec.DEFAULT_COMMANDLINE_CODEC.toString(map);

        System.err.println("args: " + args);
        OneAgent.agentmain(args, instrumentation);

        OneAgent.destory();
        
        assertThat(capture.toString()).contains("enabled TestActivator").contains("init TestActivator")
        .contains("start TestActivator").contains("DemoAgent started.")
        .contains("stop TestActivator");
    }

}
