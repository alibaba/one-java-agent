package com.alibaba.oneagent;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.oneagent.plugin.PluginException;
import com.alibaba.oneagent.plugin.PluginManager;
import com.alibaba.oneagent.plugin.PluginManagerImpl;
import com.alibaba.oneagent.service.TransformerManager;
import com.alibaba.oneagent.service.TransformerManagerImpl;
import com.alibaba.oneagent.utils.FeatureCodec;

/**
 * 
 * @author hengyunabc 2020-11-12
 *
 */
public class AgentImpl implements Agent {

    private PluginManager pluginManager;
    private TransformerManager transformerManager;
    private static Logger logger;

    @Override
    public void init(String args, final Instrumentation inst, boolean premain) {
        args = decodeArg(args);
        initLogger();

        Map<String, String> map = FeatureCodec.DEFAULT_COMMANDLINE_CODEC.toMap(args);

        String oneagentHome = map.get("oneagent.home");

        if (oneagentHome == null) {
            CodeSource codeSource = this.getClass().getProtectionDomain().getCodeSource();
            URL agentJarLocation = codeSource.getLocation();
            oneagentHome = new File(agentJarLocation.getFile()).getParent();
            map.put("oneagent.home", oneagentHome);
        }

        logger.info("oneagent home: " + map.get("oneagent.home"));

        transformerManager = new TransformerManagerImpl(inst);

        Properties properties = new Properties();
        properties.putAll(map);

        logger.debug("PluginManager properties: {}", properties);

        try {

            // extPlugins
            List<URL> extPluginlLoacations = new ArrayList<URL>();
            String extStr = map.get("oneagent.extPlugins");
            if (extStr != null) {
                String[] strings = extStr.split(",");
                for (String s : strings) {
                    s = s.trim();
                    if (!s.isEmpty()) {
                        extPluginlLoacations.add(new File(s).toURI().toURL());
                    }
                }
            }

            pluginManager = new PluginManagerImpl(inst, transformerManager, properties, new File(oneagentHome, "plugins").toURI().toURL(),
                    extPluginlLoacations);

            pluginManager.scanPlugins();

            pluginManager.enablePlugins();

            pluginManager.initPlugins();

            pluginManager.startPlugins();

        } catch (Exception e) {
            logger.error("PluginManager error", e);
        }
    }

    @Override
    public void destory() {
        try {
            pluginMaanger().stopPlugins();
        } catch (PluginException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                transformerManager.destory();
            } catch (Exception e) {
                logger.error("TransformerManager destory error", e);
            }
        }
    }

    private void initLogger() {
        logger = LoggerFactory.getLogger(AgentImpl.class);
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
    public TransformerManager transformerManager() {
        return transformerManager;
    }

}
