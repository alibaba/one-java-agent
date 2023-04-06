package io.oneagent.tunnel;

import io.oneagent.plugin.config.Config;

@Config
public class TunnelConifg {

    /**
     * 显式指定 listen 地址，比如 localhost:9006 。用户可以在 oneagent.properties 里配置，可以本地调用和调试。
     */
    private String listenAddress;

    private String tunnelServerUrl;

    public String getTunnelServerUrl() {
        return tunnelServerUrl;
    }

    public void setTunnelServerUrl(String tunnelServerUrl) {
        this.tunnelServerUrl = tunnelServerUrl;
    }

    public String getListenAddress() {
        return listenAddress;
    }

    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }

}
