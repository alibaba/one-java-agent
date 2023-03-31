package io.oneagent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.oneagent.AgentImpl;
import io.oneagent.utils.FeatureCodec;
import io.oneagent.utils.IOUtils;

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

        String version = readVersion(new File(file, "../../../pom.xml"));

        File fastjsonDemoPluginDir = new File(file, "../../../fastjson-demo-plugin/target/fastjson-demo-plugin@" + version);
        File bytekitDemoPluginDir = new File(file, "../../../bytekit-demo-plugin/target/bytekit-demo-plugin@" + version);
        File demoPluginDir = new File(file, "../../../demo-plugin/target/demo-plugin@" + version);

        File demoAgentDir = new File(file, "../../../demo-agent/target/demo-agent@" + version);

        File dubboDemoPluginDir = new File(file, "../../../dubbo-test-plugin/target/dubbo-test-plugin@" + version);

        Map<String, String> map = new HashMap<String, String>();

        map.put(AgentImpl.ONEAGENT_EXT_PLUGINS, fastjsonDemoPluginDir.getCanonicalPath() + "," + 
                bytekitDemoPluginDir.getCanonicalPath() + "," + 
                demoPluginDir.getCanonicalPath() + "," + demoAgentDir.getCanonicalPath()
                + "," + dubboDemoPluginDir.getCanonicalPath());

        String args = FeatureCodec.DEFAULT_COMMANDLINE_CODEC.toString(map);

        return args;
    }

    private static String readVersion(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);

        try {
            String pomStr = IOUtils.toString(inputStream);

            for (String line : pomStr.split("\\r?\\n")) {
                if (line.contains("<revision>")) {
                    int startIndex = line.indexOf("<revision>") + "<revision>".length();
                    int endIndex = line.indexOf("</revision>");
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
