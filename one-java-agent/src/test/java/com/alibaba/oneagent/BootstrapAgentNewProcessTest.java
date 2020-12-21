package com.alibaba.oneagent;

import java.io.File;
import java.io.IOException;
import java.util.List;
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

    @Test
    public void test() throws InvalidExitValueException, IOException, InterruptedException, TimeoutException {
        File javaPath = ProcessUtils.findJava();

        String testClassesDir = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();

        System.err.println(testClassesDir);

        File oneagentDir = new File(testClassesDir, "../oneagent");

        File oneagentJarFile = new File(oneagentDir, "one-java-agent.jar");

        String args = AgentArgsUtils.agentArgs();

        System.err.println("args: " + args);

        // java -javaagent:xxx.jar[=option] -cp [test-classes] TTT

        String agentStr = "-javaagent:" + oneagentJarFile.getAbsolutePath() + "=" + args;

        ProcessExecutor processExecutor = new ProcessExecutor()
                .command(javaPath.getAbsolutePath(), agentStr, "-cp", testClassesDir, TTT.class.getName())
                .readOutput(true);

        List<String> command = processExecutor.getCommand();

        StringBuilder sb = new StringBuilder();
        for (String str : command) {
            sb.append(str).append(' ');
        }

        System.err.println(sb.toString());

        ProcessResult result = processExecutor.execute();

        String outputString = result.outputString();

        System.err.println(outputString);

        Assertions.assertThat(outputString).contains("enabled TestActivator").contains("init TestActivator")
                .contains("start TestActivator").contains(TTT.STR);
    }

}
