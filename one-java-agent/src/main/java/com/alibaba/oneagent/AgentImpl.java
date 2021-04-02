package com.alibaba.oneagent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.bytekit.asm.instrument.InstrumentConfig;
import com.alibaba.bytekit.asm.instrument.InstrumentParseResult;
import com.alibaba.bytekit.asm.instrument.InstrumentTransformer;
import com.alibaba.bytekit.asm.matcher.SimpleClassMatcher;
import com.alibaba.bytekit.utils.AsmUtils;
import com.alibaba.oneagent.inst.ClassLoader_Instrument;
import com.alibaba.oneagent.plugin.PluginException;
import com.alibaba.oneagent.plugin.PluginManager;
import com.alibaba.oneagent.plugin.PluginManagerImpl;
import com.alibaba.oneagent.service.ComponentManager;
import com.alibaba.oneagent.service.ComponentManagerImpl;
import com.alibaba.oneagent.utils.FeatureCodec;
import com.alibaba.oneagent.utils.IOUtils;
import com.alibaba.oneagent.utils.InstrumentationUtils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;

/**
 * 
 * @author hengyunabc 2020-11-12
 *
 */
public class AgentImpl implements Agent {
    /**
     * print more log to stdout
     */
    private static final String ONEAGENT_VERBOSE = "oneagent.verbose";

    private static final String ONEAGENT_HOME = "oneagent.home";
    public static final String ONEAGENT_EXTPLUGINS = "oneagent.extPlugins";
    public static final String ONEAGENT_ENHANCELOADERS = "oneagent.enhanceLoaders";

    private static final String ONE_JAVA_AGENT_SPY_JAR = "one-java-agent-spy.jar";
    private static final String PLUGINS = "plugins";

    private Instrumentation instrumentation;
    private InstrumentTransformer classLoaderInstrumentTransformer;

    private PluginManager pluginManager;
    private ComponentManager componentManager;
    private static Logger logger;

    @Override
    public void init(String args, final Instrumentation inst, boolean premain) {
        this.instrumentation = inst;
        args = decodeArg(args);
        Map<String, String> map = FeatureCodec.DEFAULT_COMMANDLINE_CODEC.toMap(args);

        initLogger(map);

        String oneagentHome = map.get(ONEAGENT_HOME);

        if (oneagentHome == null) {
            /**
             * <pre>
             * 查找oneagent home的逻辑：
             * 1. 从jar所在的目录向上找，如果有 plugins目录，则认为是 oneagent home
             * 2. 向上最多找两层目录
             * 3. 兼容多种部署形式，比如 ~/oneagent/core/oneagent@0.0.1-SNAPSHOT/one-java-agent.jar ，插件目录在
             *    ~/oneagent/plugins ；
             *    也可能是  one-java-agent.jar 和 plugins在同一目录下
             * </pre>
             */
            CodeSource codeSource = this.getClass().getProtectionDomain().getCodeSource();
            String agentJarFile = codeSource.getLocation().getFile();

            File agentJarDir = new File(agentJarFile).getParentFile();
            if (new File(agentJarDir, PLUGINS).exists()) {
                oneagentHome = agentJarDir.getAbsolutePath();
            } else if (new File(agentJarDir.getParentFile(), PLUGINS).exists()) {
                oneagentHome = agentJarDir.getParent();
            } else {
                // ~/oneagent/core/oneagent@0.0.1-SNAPSHOT/one-java-agent.jar
                // ~/oneagent/core
                oneagentHome = agentJarDir.getParentFile().getParent();
            }

            map.put(ONEAGENT_HOME, oneagentHome);
        }

        logger.info("oneagent home: " + map.get(ONEAGENT_HOME));

        // append spy jar
        InputStream spyJarInputStream = this.getClass().getClassLoader()
                .getResourceAsStream(ONE_JAVA_AGENT_SPY_JAR);
        FileOutputStream out = null;
        if (spyJarInputStream != null) {
            try {
                // TODO 是否要避免多次 append，没找到是否直接启动失败
                File tempFile = File.createTempFile(
                        ONE_JAVA_AGENT_SPY_JAR.substring(0, ONE_JAVA_AGENT_SPY_JAR.length() - ".jar".length()),
                        ".jar");
                tempFile.deleteOnExit();
                out = new FileOutputStream(tempFile);
                IOUtils.copy(spyJarInputStream, out);
                logger.info("extract {} to {}", ONE_JAVA_AGENT_SPY_JAR, tempFile.getAbsolutePath());
                instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(tempFile.getAbsoluteFile()));
            } catch (Throwable e) {
                logger.error("try to append agent spy jar to BootstrapClassLoaderSearch error", e);
            } finally {
                IOUtils.close(spyJarInputStream);
                IOUtils.close(out);
            }
        } else {
            logger.error("can not find resource {}", ONE_JAVA_AGENT_SPY_JAR);
        }

        componentManager = new ComponentManagerImpl(inst);
        componentManager.initComponents();

        AgentSpyImpl.initAgentSpy(componentManager);

        String enhanceLoaders = map.get(ONEAGENT_ENHANCELOADERS);
        if (enhanceLoaders == null) {
            enhanceLoaders = ClassLoader.class.getName();
        }

        try {
            this.enhanceClassLoader(enhanceLoaders);
        } catch (Exception e) {
            logger.error("enhanceLoaders error", e);
        }

        Properties properties = new Properties();
        properties.putAll(map);

        logger.debug("PluginManager properties: {}", properties);

        try {

            // extPlugins
            List<URL> extPluginlLoacations = new ArrayList<URL>();
            String extStr = map.get(ONEAGENT_EXTPLUGINS);
            if (extStr != null) {
                String[] strings = extStr.split(",");
                for (String s : strings) {
                    s = s.trim();
                    if (!s.isEmpty()) {
                        extPluginlLoacations.add(new File(s).toURI().toURL());
                    }
                }
            }

            pluginManager = new PluginManagerImpl(inst, componentManager, properties,
                    new File(oneagentHome, "plugins").toURI().toURL(), extPluginlLoacations);

            pluginManager.scanPlugins();

            pluginManager.enablePlugins();

            pluginManager.initPlugins();

            pluginManager.startPlugins();

        } catch (Throwable e) {
            logger.error("PluginManager error", e);
        }
        long currentTime = System.nanoTime();
        logger.info("plugins init completed! cost {}ms", (currentTime - NopAgent.startTime) / (1000 * 1000));
    }

    @Override
    public void destory() {
        try {
            pluginMaanger().stopPlugins();
        } catch (PluginException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                componentManager.stopComponents();
            } catch (Exception e) {
                logger.error("TransformerManager destory error", e);
            }
        }
    }

    private void initLogger(Map<String, String> argsMap) {
        String verboseStr = argsMap.get(ONEAGENT_VERBOSE);
        if (verboseStr == null) {
            verboseStr = System.getProperty(ONEAGENT_VERBOSE);
        }
        boolean verbose = Boolean.parseBoolean(verboseStr);

        logger = LoggerFactory.getLogger(AgentImpl.class);

        if (verbose) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger logbackLogger = (ch.qos.logback.classic.Logger) LoggerFactory
                    .getLogger(Logger.ROOT_LOGGER_NAME);
            logbackLogger.setLevel(Level.TRACE);

            ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<ILoggingEvent>();
            PatternLayoutEncoder encoder = new PatternLayoutEncoder();
            encoder.setPattern("%date %level [%thread] %logger{10} [%file:%line] %msg%n");
            encoder.setContext(loggerContext);
            appender.setEncoder(encoder);
            appender.setContext(loggerContext);
            encoder.start();
            appender.start();
            logbackLogger.addAppender(appender);
        }
    }

    void enhanceClassLoader(String enhanceLoaders) throws IOException, UnmodifiableClassException {
        if (enhanceLoaders == null) {
            return;
        }
        Set<String> loaders = new HashSet<String>();
        for (String s : enhanceLoaders.split(",")) {
            loaders.add(s.trim());
        }

        // 增强 ClassLoader#loadClsss ，提供机制可以加载插件方的类
        byte[] classBytes = IOUtils.getBytes(this.getClass().getClassLoader()
                .getResourceAsStream(ClassLoader_Instrument.class.getName().replace('.', '/') + ".class"));

        SimpleClassMatcher matcher = new SimpleClassMatcher(loaders);
        InstrumentConfig instrumentConfig = new InstrumentConfig(AsmUtils.toClassNode(classBytes), matcher);

        InstrumentParseResult instrumentParseResult = new InstrumentParseResult();
        instrumentParseResult.addInstrumentConfig(instrumentConfig);
        classLoaderInstrumentTransformer = new InstrumentTransformer(instrumentParseResult);
        instrumentation.addTransformer(classLoaderInstrumentTransformer, true);

        if (loaders.size() == 1 && loaders.contains(ClassLoader.class.getName())) {
            // 如果只增强 java.lang.ClassLoader，可以减少查找过程
            instrumentation.retransformClasses(ClassLoader.class);
        } else {
            InstrumentationUtils.trigerRetransformClasses(instrumentation, loaders);
        }
    }

    private static String decodeArg(String arg) {
        if (arg == null) {
            return arg;
        }
        try {
            return URLDecoder.decode(arg, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return arg;
        }
    }

    @Override
    public PluginManager pluginMaanger() {
        return pluginManager;
    }

    @Override
    public ComponentManager componentManager() {
        return componentManager;
    }

}
