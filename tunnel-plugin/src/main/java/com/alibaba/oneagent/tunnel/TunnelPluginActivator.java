package com.alibaba.oneagent.tunnel;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.oneagent.plugin.PluginActivator;
import com.alibaba.oneagent.plugin.PluginContext;
import com.alibaba.oneagent.plugin.config.BinderUtils;
import com.alibaba.oneagent.service.OneAgentInfoService;
import com.alibaba.reverse.proxy.client.TunnelClient;

public class TunnelPluginActivator implements PluginActivator {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    private LocalServiceManager localServiceManager;

    private TunnelClient tunnelClient;

    private TunnelConifg tunnelConifg = new TunnelConifg();

    @Override
    public boolean enabled(PluginContext context) {
        System.out.println("enabled " + this.getClass().getName());

        this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();

        return true;
    }

    @Override
    public void init(PluginContext context) throws Exception {
        System.out.println("init " + this.getClass().getName());

        BinderUtils.inject(context.getPlugin().config(), tunnelConifg);

        localServiceManager = new LocalServiceManager();
        localServiceManager.start();

        OneAgentInfoService agentInfoService = context.getComponentManager().getComponent(OneAgentInfoService.class);
        String appName = agentInfoService.appName();
        String version = agentInfoService.version();

        tunnelClient = new TunnelClient();
        tunnelClient.setAppname(appName);
        tunnelClient.setTunnelServerUrl(tunnelConifg.getTunnelServerUrl());
        Map<String, String> meta = new HashMap<>();
        meta.put("version", version);
        meta.put("tunnelVersion", context.getPlugin().config().getVersion());
        tunnelClient.setMeta(meta);

        try {
            // 第一次连接可能网络失败，忽略
            tunnelClient.start();
        } catch (Throwable e) {
            logger.error("tunnelClient start error", e);
        }
    }

    @Override
    public void start(PluginContext context) throws Exception {
        System.out.println("start " + this.getClass().getName());
    }

    @Override
    public void stop(PluginContext context) throws Exception {
        System.out.println("stop " + this.getClass().getName());

        if (localServiceManager != null) {
            localServiceManager.stop();
        }

        if (tunnelClient != null) {
            tunnelClient.stop();
        }
    }

}
