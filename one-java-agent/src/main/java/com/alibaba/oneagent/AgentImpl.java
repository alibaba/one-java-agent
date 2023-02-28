package com.alibaba.oneagent;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
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
import com.alibaba.oneagent.utils.InstrumentationWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.JarFile;

/**
 * @author hengyunabc 2020-11-12
 */
public class AgentImpl implements Agent {
    /**
     * print more log to stdout
     */
    private static final String ONEAGENT_VERBOSE = "oneagent.verbose";

    private static final String ONEAGENT_HOME = "oneagent.home";
    public static final String ONEAGENT_EXT_PLUGINS = "oneagent.extPlugins";
    private static final String ONEAGENT_ENHANCE_LOADERS = "oneagent.enhanceLoaders";

    private static final String ONE_JAVA_AGENT_SPY_JAR = "one-java-agent-spy.jar";
    private static final String PLUGINS = "plugins";

    private Instrumentation instrumentation;
    private InstrumentTransformer classLoaderInstrumentTransformer;

    private PluginManager pluginManager;
    private ComponentManager componentManager;
    private static Logger logger;

    @Override
    public void init(String args, Instrumentation inst, boolean premain) {
        inst = new InstrumentationWrapper(inst);
        this.instrumentation = inst;

        Properties config = this.getOneAgentConfigurationProperties(args);

        this.initLogger(config);

        this.findOneAgentHomePath(config);

        logger.debug("PluginManager properties: {}", config);

        boolean appendResult = this.appendSpyJarToBootstrapClassLoaderSearch();
        if (!appendResult) {
            logger.error("init oneagent error,can't append spy jar to bootstrap classLoader");
            return;
        }

        this.initComponents(inst, config);

        this.enhanceClassLoader(config);

        this.initPlugins(inst, config);
        long currentTime = System.nanoTime();
        logger.info("plugins init completed! cost {} ms", (currentTime - NopAgent.startTime) / (1000 * 1000));
    }

    /**
     * init one agent components such as
     * {@linkplain com.alibaba.oneagent.service.TransformerManagerImpl}
     * {@linkplain com.alibaba.oneagent.service.ClassLoaderHandlerManagerImpl}
     *
     * @param inst
     */
    private void initComponents(Instrumentation inst, Properties properties) {
        componentManager = new ComponentManagerImpl(inst);
        componentManager.initComponents(properties);
        AgentSpyImpl.initAgentSpy(componentManager);
    }

    /**
     * Get all configuration information
     *
     * @param args
     * @return
     */
    private Properties getOneAgentConfigurationProperties(String args) {
        args = decodeArg(args);
        Map<String, String> map = FeatureCodec.DEFAULT_COMMANDLINE_CODEC.toMap(args);
        Properties properties = new Properties();
        properties.putAll(map);
        return properties;
    }

    /**
     * init one agent plugins
     *
     * @param inst
     * @param properties
     */
    private void initPlugins(Instrumentation inst, Properties properties) {
        String oneagentHome = properties.getProperty(ONEAGENT_HOME);
        try {
            // extPlugins
            List<URL> extPluginLocations = new ArrayList<URL>();
            String extStr = properties.getProperty(ONEAGENT_EXT_PLUGINS);
            if (extStr != null) {
                String[] strings = extStr.split(",");
                for (String s : strings) {
                    s = s.trim();
                    if (!s.isEmpty()) {
                        extPluginLocations.add(new File(s).toURI().toURL());
                    }
                }
            }

            pluginManager = new PluginManagerImpl(inst, componentManager, properties,
                    new File(oneagentHome, "plugins").toURI().toURL(), extPluginLocations);

            pluginManager.scanPlugins();

            pluginManager.enablePlugins();

            pluginManager.initPlugins();

            pluginManager.startPlugins();

        } catch (Throwable e) {
            logger.error("PluginManager error", e);
        }
    }

    /**
     * append one-java-agent-spy.jar to BootstrapClassLoader
     */
    private boolean appendSpyJarToBootstrapClassLoaderSearch() {
        boolean appendResult = true;
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
                appendResult = false;
                logger.error("try to append agent spy jar to BootstrapClassLoaderSearch error", e);
            } finally {
                IOUtils.close(spyJarInputStream);
                IOUtils.close(out);
            }
        } else {
            appendResult = false;
            logger.error("can not find resource {}", ONE_JAVA_AGENT_SPY_JAR);
        }
        return appendResult;
    }

    /**
     * Search OneAgent Home Path
     *
     * @param config
     * @return
     */
    private void findOneAgentHomePath(Properties config) {
        String oneagentHome = config.getProperty(ONEAGENT_HOME);

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

            config.put(ONEAGENT_HOME, oneagentHome);
        }
        logger.info("oneagent home: " + config.get(ONEAGENT_HOME));
    }

    @Override
    public void destroy() {
        try {
            pluginManager().stopPlugins();
        } catch (PluginException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                componentManager.stopComponents();
            } catch (Exception e) {
                logger.error("TransformerManager destroy error", e);
            }
        }
    }

    /**
     * init log
     *
     * @param config
     */
    private void initLogger(Properties config) {
        String verboseStr = config.getProperty(ONEAGENT_VERBOSE);
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

    private void enhanceClassLoader(Properties config) {
        try {
            String enhanceLoaders = config.getProperty(ONEAGENT_ENHANCE_LOADERS, ClassLoader.class.getName());
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
        } catch (Exception e) {
            logger.error("enhanceLoaders error", e);
        }
    }

    private static String decodeArg(String arg) {
        if (arg == null) {
            return null;
        }
        try {
            return URLDecoder.decode(arg, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return arg;
        }
    }

    @Override
    public PluginManager pluginManager() {
        return pluginManager;
    }

    @Override
    public ComponentManager componentManager() {
        return componentManager;
    }

}
