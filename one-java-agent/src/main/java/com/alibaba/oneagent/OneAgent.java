package com.alibaba.oneagent;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLDecoder;
import java.security.CodeSource;
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
 * @author hengyunabc 2020-07-28
 *
 */
public class OneAgent {

	private static volatile OneAgent instance;

	private PluginManager pluginManager;
	private TransformerManager transformerManager;
	private static Logger logger;

	public static void premain(String args, Instrumentation inst) {
		main(args, inst, true);
	}

	public static void agentmain(String args, Instrumentation inst) {
		main(args, inst, false);
	}

    public static void init(String args, Instrumentation inst) {
        main(args, inst, false);
    }

    public static void destory() throws PluginException {
        OneAgent oneAgent = getInstance();
        if (oneAgent != null) {
            oneAgent.pluginMaanger().stopPlugins();
        }
    }

	private static synchronized void main(String args, Instrumentation inst, boolean premain) {
		if (instance == null) {
			synchronized (OneAgent.class) {
				if (instance == null) {
					OneAgent temp = new OneAgent();
					args = decodeArg(args);
					temp.init(args, inst, premain);
					instance = temp;
				}
			}
		}
	}

	private void init(final String args, final Instrumentation inst, boolean premain) {
		initLogger();

		Map<String, String> map = FeatureCodec.DEFAULT_COMMANDLINE_CODEC.toMap(args);

		String oneagentHome = map.get("oneagent.home");

		if (oneagentHome == null) {
			CodeSource codeSource = OneAgent.class.getProtectionDomain().getCodeSource();
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
			pluginManager = new PluginManagerImpl(inst, properties, new File(oneagentHome, "plugins").toURI().toURL());

			pluginManager.scanPlugins();

			pluginManager.enablePlugins();

			pluginManager.initPlugins();

			pluginManager.startPlugins();

		} catch (Exception e) {
			logger.error("PluginManager error", e);
		}
	}

	private void initLogger() {
		logger = LoggerFactory.getLogger(OneAgent.class);
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

	public static OneAgent getInstance() {
		return instance;
	}

	public PluginManager pluginMaanger() {
		return pluginManager;
	}

	public TransformerManager transformerManager() {
		return transformerManager;
	}
}
