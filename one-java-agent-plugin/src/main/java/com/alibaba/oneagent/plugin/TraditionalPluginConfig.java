package com.alibaba.oneagent.plugin;

import com.alibaba.oneagent.plugin.properties.Config;

/**
 * <pre>
 * 支持的配置项： 
 * agentJarPath=xxx.jar 
 * appendToSystemClassLoaderSearch=
 * agentInitMethod=premain/agentmain
 * agentArgs=
 * enabled=
 * </pre>
 * 
 * @author hengyunabc 2020-07-28
 *
 */

@Config
public class TraditionalPluginConfig {
	private String version;
	private String name;
	private int order = OneAgentPlugin.DEFAULT_ORDER;

	private String agentJarPath;
	private boolean appendToSystemClassLoaderSearch = true;
	private String agentArgs;

	private String agentInitMethod = "premain";

	public String getAgentJarPath() {
		return agentJarPath;
	}

	public void setAgentJarPath(String agentJarPath) {
		this.agentJarPath = agentJarPath;
	}

	public boolean isAppendToSystemClassLoaderSearch() {
		return appendToSystemClassLoaderSearch;
	}

	public void setAppendToSystemClassLoaderSearch(boolean appendToSystemClassLoaderSearch) {
		this.appendToSystemClassLoaderSearch = appendToSystemClassLoaderSearch;
	}

	public String getAgentArgs() {
		return agentArgs;
	}

	public void setAgentArgs(String agentArgs) {
		this.agentArgs = agentArgs;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getAgentInitMethod() {
		return agentInitMethod;
	}

	public void setAgentInitMethod(String agentInitMethod) {
		this.agentInitMethod = agentInitMethod;
	}
}
