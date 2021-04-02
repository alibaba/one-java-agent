package com.alibaba.oneagent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;

/**
 * 
 * @author hengyunabc 2020-12-18
 *
 */
public class BootstrapAgentNewProcessTest {

    private String runProcess() throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        return runProcess(new Properties());
    }

    private String runProcess(Properties properties)
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        File javaPath = ProcessUtils.findJava();

        String testClassesDir = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();

        System.err.println(testClassesDir);

        File oneagentDir = new File(testClassesDir, "../oneagent");

        File oneagentJarFile = new File(oneagentDir, "one-java-agent.jar");

        String args = AgentArgsUtils.agentArgs();

        System.err.println("args: " + args);

        // java -javaagent:xxx.jar[=option] -cp [test-classes] TTT

        String agentStr = "-javaagent:" + oneagentJarFile.getAbsolutePath() + "=" + args;

        List<String> commands = new ArrayList<String>();
        commands.add(javaPath.getAbsolutePath());
        commands.add(agentStr);
        commands.add("-cp");
        commands.add(testClassesDir);

        for (Entry<Object, Object> entry : properties.entrySet()) {
            entry.getKey();
            commands.add("-D" + entry.getKey() + "=" + entry.getValue());
        }

        commands.add(TTT.class.getName());

        ProcessExecutor processExecutor = new ProcessExecutor().command(commands).readOutput(true);

        List<String> command = processExecutor.getCommand();

        StringBuilder sb = new StringBuilder();
        for (String str : command) {
            sb.append(str).append(' ');
        }

        System.err.println(sb.toString());

        ProcessResult result = processExecutor.execute();

        String outputString = result.outputString();
        return outputString;
    }

    @Test
    public void test() throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {

        String processOutput = runProcess();
        System.err.println(processOutput);

        String className = "com.activator.test.DemoActivator";

        Assertions.assertThat(processOutput).contains("enabled " + className).contains("init " + className)
                .contains("start " + className).contains(TTT.STR);

        Assertions.assertThat(processOutput).contains("DemoAgent started.");
    }

    @Test
    public void testDisablePlugin()
            throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        Properties properties = new Properties();
        properties.setProperty("oneagent.plugin." + "demo-plugin.enabled", "" + false);
        properties.setProperty("oneagent.plugin." + "demo-agent.enabled", "" + false);
        properties.setProperty("oneagent.verbose", "" + true);

        String processOutput = runProcess(properties);
        System.err.println(processOutput);

        String className = "com.activator.test.DemoActivator";

        Assertions.assertThat(processOutput).doesNotContain("enabled " + className).doesNotContain("init " + className)
                .doesNotContain("start " + className).contains(TTT.STR);
        Assertions.assertThat(processOutput).doesNotContain("DemoAgent started.");
    }
}
