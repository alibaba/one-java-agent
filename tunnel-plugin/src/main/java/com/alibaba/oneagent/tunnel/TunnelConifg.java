package com.alibaba.oneagent.tunnel;

import com.alibaba.oneagent.plugin.config.Config;

@Config
public class TunnelConifg {

    private String tunnelServerUrl;

    public String getTunnelServerUrl() {
        return tunnelServerUrl;
    }

    public void setTunnelServerUrl(String tunnelServerUrl) {
        this.tunnelServerUrl = tunnelServerUrl;
    }

}
