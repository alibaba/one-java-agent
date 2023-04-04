package io.oneagent.tunnel;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.reverse.proxy.client.TunnelClient;
import com.google.common.collect.Lists;

import io.oneagent.api.impl.OneAgentInfoImpl;
import io.oneagent.plugin.PluginActivator;
import io.oneagent.plugin.PluginContext;
import io.oneagent.plugin.config.BinderUtils;
import io.oneagent.service.OneAgentInfoService;

public class TunnelPluginActivator implements PluginActivator {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().getName());

    private LocalServiceManager localServiceManager;

    private TunnelClient tunnelClient;

    private TunnelConifg tunnelConifg = new TunnelConifg();

    @Override
    public boolean enabled(PluginContext context) {
        logger.info("plugin endabled");
        return true;
    }

    @Override
    public void init(PluginContext context) throws Exception {
        BinderUtils.inject(context.getPlugin().config(), tunnelConifg);

        OneAgentInfoService oneAgentInfoService = context.getComponentManager().getComponent(OneAgentInfoService.class);
        OneAgentInfoImpl agentInfoImpl = new OneAgentInfoImpl(oneAgentInfoService);

        localServiceManager = new LocalServiceManager();
        localServiceManager.start(Lists.newArrayList(agentInfoImpl));

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
    }

    @Override
    public void stop(PluginContext context) throws Exception {
        if (localServiceManager != null) {
            localServiceManager.stop();
        }

        if (tunnelClient != null) {
            tunnelClient.stop();
        }
    }

}
