package com.alibaba.oneagent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.oneagent.utils.FeatureCodec;
import com.alibaba.oneagent.utils.IOUtils;

/**
 * 
 * @author hengyunabc 2020-12-18
 *
 */
public class AgentArgsUtils {

    public static String agentArgs() throws IOException {
        URL location = AgentArgsUtils.class.getProtectionDomain().getCodeSource().getLocation();
        System.err.println(location);

        String file = location.getFile();

        String version = readVersion(new File(file, "../../pom.xml"));

        File demoPluginDir = new File(file, "../../../demo-plugin/target/demo-plugin@" + version);

        File demoAgentDir = new File(file, "../../../demo-agent/target/demo-agent@" + version);

        File dubboDemoPluginDir = new File(file, "../../../dubbo-test-plugin/target/dubbo-test-plugin@" + version);

        Map<String, String> map = new HashMap<String, String>();

        map.put(AgentImpl.ONEAGENT_EXTPLUGINS, demoPluginDir.getAbsolutePath() + "," + demoAgentDir.getAbsolutePath()
                + "," + dubboDemoPluginDir.getAbsolutePath());

        String args = FeatureCodec.DEFAULT_COMMANDLINE_CODEC.toString(map);

        return args;
    }

    private static String readVersion(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);

        try {
            String pomStr = IOUtils.toString(inputStream);

            for (String line : pomStr.split("\\r?\\n")) {
                if (line.contains("<version>")) {
                    int startIndex = line.indexOf("<version>") + "<version>".length();
                    int endIndex = line.indexOf("</version>");
                    String version = line.substring(startIndex, endIndex);
                    return version;
                }
            }

            throw new IllegalStateException("can not read version from pom.xml");
        } finally {
            IOUtils.close(inputStream);
        }

    }
}
