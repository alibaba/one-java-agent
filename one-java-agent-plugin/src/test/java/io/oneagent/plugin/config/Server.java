package io.oneagent.plugin.config;

import java.net.InetAddress;

import io.oneagent.plugin.config.Config;
import io.oneagent.plugin.config.NestedConfig;

@Config(prefix = "server")
public class Server {

	int port;
	String host;

	InetAddress address;

	boolean flag;

	@NestedConfig
	Ssl ssl;

	@NestedConfig
	ErrorProperties error;

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public boolean isFlag() {
		return flag;
	}

	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public String toString() {
		return "Server [port=" + port + ", host=" + host + ", address=" + address + ", flag=" + flag + ", ssl=" + ssl
				+ ", error=" + error + "]";
	}

}